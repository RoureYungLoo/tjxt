package com.tianji.learning.service;

import com.tianji.learning.domain.po.InteractionQuestion;

/**
 * @author luruoyang
 */
public interface AIService {

  void autoReply(InteractionQuestion interactionQuestion);
}
