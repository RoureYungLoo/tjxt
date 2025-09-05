package com.tianji.aigc.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.aigc.constant.TimeStrConstant;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.entity.MyAssitantMessage;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.properties.SessionProperties;
import com.tianji.aigc.service.IChatSessionService;
import com.tianji.aigc.vo.ChatHistoryVO;
import com.tianji.aigc.vo.MessageVO;
import com.tianji.aigc.vo.SessionVO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.UserContext;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 对话session 服务实现类
 * </p>
 *
 * @author mr.wu
 * @since 2025-08-17
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

  @Autowired
  private SessionProperties sessionProperties;

  /**
   * 新建会话
   *
   * @param num
   * @return
   */
  @Override
  public SessionVO createSession(Integer num) {
    //1.生成会话ID
    String sessionId = IdUtil.fastSimpleUUID();

    //2.保存会话历史
    ChatSession chatSession = ChatSession.builder().sessionId(sessionId)
        .userId(UserContext.getUser()).build();
    this.save(chatSession);

    //3.随机取出3个热门问题
    List<SessionVO.Example> examples = sessionProperties.getExamples();
    Collections.shuffle(examples);

    //4.封装响应结果
    SessionVO sessionVO = SessionVO.builder()
        .sessionId(sessionId)
        .describe(sessionProperties.getDescribe())
        .examples(examples.stream().limit(num).toList())
        .build();

    return sessionVO;
  }

  @Override
  public List<SessionVO.Example> getHotPrompt(Integer num) {
    //3.随机取出3个热门问题
    List<SessionVO.Example> examples = sessionProperties.getExamples();
    Collections.shuffle(examples);

    //4.封装响应结果
    List<SessionVO.Example> exampleList = examples.stream().limit(3).collect(Collectors.toList());
    return exampleList;
  }

  @Autowired
  private ChatMemory chatMemory;

  /**
   * 获取会话详情
   */
  @Override
  public List<MessageVO> messageList(String sessionId) {
    // 判断会话ID是否存在
    ChatSession chatSession = lambdaQuery().eq(ChatSession::getSessionId, sessionId).one();
    if (chatSession == null) {
      throw new BizIllegalException("会话不存在");
    }

    // 获取历史1000条消息
    List<Message> messageList = chatMemory.get(UserContext.getUser() + ":" + sessionId, 1000);
    return messageList.stream()
        .filter(message -> message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.ASSISTANT)
        .map(message -> {
          MessageVO.MessageVOBuilder builder = MessageVO.builder();
          builder
              .type(message.getMessageType()) // 消息类型
              .content(message.getText()); // 消息内容
          // 兼容卡片消息
          if (message instanceof MyAssitantMessage myAssitantMessage) {
            builder.param(myAssitantMessage.getParams()); // 卡片参数
          }
          return builder.build();
        })
        .collect(Collectors.toList());
  }

  @Async
  @Override
  public void updateSessionTitle(String question, String sessionId, Long userId) {
    // 查询会话是否存在
    ChatSession chatSession = lambdaQuery().eq(ChatSession::getUserId, userId)
        .eq(ChatSession::getSessionId, sessionId).one();
    if (chatSession == null) {
      return;
    }

    // 更新会话title
    String title = chatSession.getTitle();
    lambdaUpdate()
        .eq(ChatSession::getUserId, userId)
        .eq(ChatSession::getSessionId, sessionId)
        // title 不存在
        .set(StrUtil.isBlank(title), ChatSession::getTitle, question)
        // title 已存在
        .set(StrUtil.isNotBlank(title), ChatSession::getUpdateTime, LocalDateTime.now())
        .update();
  }

  /**
   * 查询历史会话列表
   *
   * @return
   */
  @Override
  public Map<String, List<ChatHistoryVO>> getChatHistoryList() {
    // 查询用户最近30条历史会话数据
    List<ChatSession> latestChatSessionList = this.lambdaQuery()
        .eq(ChatSession::getUserId, UserContext.getUser())
        .orderByDesc(ChatSession::getUpdateTime)
        .last("limit 30")
        .list();

    if (CollectionUtil.isEmpty(latestChatSessionList)) {
      return Map.of();
    }

    // 类型转换
    List<ChatHistoryVO> chatHistoryVOS = BeanUtil.copyToList(latestChatSessionList, ChatHistoryVO.class);

    // 根据日期分组
    Map<String, List<ChatHistoryVO>> res = CollStreamUtil.groupByKey(chatHistoryVOS, vo -> {
      long between = Math.abs(ChronoUnit.DAYS.between(vo.getUpdateTime().toLocalDate(), LocalDate.now()));
      if (between <= 0) {
        return TimeStrConstant.TODAY.getValue();
      } else if (between <= 30) {
        return TimeStrConstant.THIS_MONTH.getValue();
      } else if (between <= 365) {
        return TimeStrConstant.THIS_YEAR.getValue();
      } else {
        return TimeStrConstant.OVER_YEAR.getValue();
      }
    });
    return res;
  }
}