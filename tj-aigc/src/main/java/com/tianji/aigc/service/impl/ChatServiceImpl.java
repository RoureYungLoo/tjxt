package com.tianji.aigc.service.impl;

import com.tianji.aigc.dto.ChatDTO;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.service.IChatSessionService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.common.exceptions.BizIllegalException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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

    //调用大模型进行对话
    return chatClient.prompt()
        .user(dto.getQuestion())
        .stream()
        .content()
        .map(x ->
            ChatEventVO.builder().eventData(x).eventType(ChatEventTypeEnum.DATA.getValue())
                .build())
        .concatWith(Flux.just(
            ChatEventVO.builder()  // 标记输出结束
                .eventType(ChatEventTypeEnum.STOP.getValue())
                .build()));
  }
}