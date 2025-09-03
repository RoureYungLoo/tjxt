package com.tianji.aigc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.tianji.aigc.dto.ChatDTO;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.IChatSessionService;
import com.tianji.aigc.tools.constant.ToolConstant;
import com.tianji.aigc.tools.result.ToolResultHolder;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.UserContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话业务实现类
 *
 * @Author mr.wu
 * @Date 2025-8-17 15:28
 */
@Service
public class ChatServiceImpl implements ChatService {

  @Autowired
  private ChatClient chatClient;

  @Autowired
  private IChatSessionService chatSessionService;

  @Autowired
  private VectorStore vectorStore;

  @Autowired
  private ChatMemory chatMemory;

  // 会话id, true/false, 通过true/false控制是否继续向前端输出
  private static Map<String, Boolean> SESSION_MAP = new ConcurrentHashMap<>();

  /**
   * 聊天对话
   *
   * @param dto
   * @return
   */
  @Override
  public Flux<ChatEventVO> chat(ChatDTO dto) {
    //判断是会话ID是否存在
    ChatSession chatSession = chatSessionService.lambdaQuery().eq(ChatSession::getSessionId, dto.getSessionId()).one();
    if (chatSession == null) {
      throw new BizIllegalException("会话不存在");
    }

    String conversationId = UserContext.getUser() + ":" + dto.getSessionId();

    String reqeust_id = IdUtil.fastSimpleUUID();

    ChatEventVO STOP_EVENT = ChatEventVO.builder()  // 标记输出结束
        .eventType(ChatEventTypeEnum.STOP.getValue())
        .build();

    // bufgix: 前端点击停止按钮, 已渲染的输出没有持久化
    StringBuffer assistantContent = new StringBuffer();

    String sessionId = dto.getSessionId();
    //调用大模型进行对话
    return chatClient.prompt()
        .user(dto.getQuestion())
        // RAG 增强检索生成
        .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.builder().query("").topK(999).build()))
        .advisors(advisorSpec -> advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
        // 把请求ID传给 ToolCalling 上下文
        .toolContext(MapUtil.<String, Object>builder()
            .put(ToolConstant.REQUEST_ID, reqeust_id)
            .put(ToolConstant.USER_ID, UserContext.getUser())
            .build())
        .stream()
        .content()
        // 开始输出
        .doFirst(() -> SESSION_MAP.put(sessionId, true))
        // 输出完成
        .doOnComplete(() -> SESSION_MAP.remove(sessionId))
        // 输出过程出现error
        .doOnError(throwable -> SESSION_MAP.remove(sessionId))
        //
        .doOnCancel(() -> {
          saveToRedis(conversationId, assistantContent.toString());
        })
        // 是否继续输出, 前端终止按钮
        .takeWhile(s -> SESSION_MAP.getOrDefault(sessionId, false))
        .map(x -> {
          ChatEventVO chatEventVO = ChatEventVO.builder().eventData(x).eventType(ChatEventTypeEnum.DATA.getValue()).build();
          // 大模型输出一点, 就添加一点
          assistantContent.append(x);
          return chatEventVO;
        })
        .concatWith(Flux.defer(() -> {
              // 从toolContext中获取courseInfo
              Map<String, Object> resultMap = ToolResultHolder.get(reqeust_id);
              if (CollUtil.isNotEmpty(resultMap)) {
                // tool calling 有返回值
                ChatEventVO chatEventVO = ChatEventVO.builder()
                    .eventData(resultMap)
                    .eventType(ChatEventTypeEnum.PARAM.getValue())
                    .build();
                // 用完后清除, 防止OOM
                ToolResultHolder.remove(reqeust_id);
                return Flux.just(chatEventVO, STOP_EVENT);
              }
              return Flux.just(STOP_EVENT);
            })
        );
  }

  // 记录被打断的会话内容
  private void saveToRedis(String conversationId, String content) {
    chatMemory.add(conversationId, new AssistantMessage(content));
  }

  @Override
  public void stop(String sessionId) {
    SESSION_MAP.put(sessionId, false);
  }
}