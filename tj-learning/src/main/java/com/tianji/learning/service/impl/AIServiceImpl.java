package com.tianji.learning.service.impl;

import com.tianji.api.client.ai.AIClient;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.service.AIService;
import com.tianji.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author luruoyang
 */
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

  private final AIClient aiClient;

  private final IInteractionReplyService interactionReplyService;

  @Override
  @Async
  public void autoReply(InteractionQuestion interactionQuestion) {
    // 准备问题
    String question = String.format("""
        请从专业角度回答用户提问：
        1. 提问的标题是：%s
        2. 提问的问题描述是：%s
        """, interactionQuestion.getTitle(), interactionQuestion.getDescription());
    // 设置用户ID
    UserContext.setUser(interactionQuestion.getUserId());

    // 获取AI的回答
    String reply = aiClient.chatText(question);

    // 准备ReplyDTO
    ReplyDTO replyDTO = ReplyDTO.builder()
        .userId(9999L)
        .questionId(interactionQuestion.getId())
        .content(reply)
        .isStudent(false)
        .anonymity(false)
        .build();
    // 插入表
    interactionReplyService.saveReply(replyDTO);
  }
}
