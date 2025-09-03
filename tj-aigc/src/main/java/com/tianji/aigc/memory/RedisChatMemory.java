package com.tianji.aigc.memory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Chat Memory in Redis
 *
 * @author luruoyang
 */
@Component
@AllArgsConstructor
public class RedisChatMemory implements ChatMemory {

  private final StringRedisTemplate redisTemplate;

  /**
   * 添加对话
   *
   * @param conversationId
   * @param messages
   */
  @Override
  public void add(String conversationId, List<Message> messages) {
    if (CollUtil.isEmpty(messages)) {
      return;
    }
    List<String> msgStrList = messages.stream().map(message -> JSONUtil.toJsonStr(message)).collect(Collectors.toList());
    redisTemplate.opsForList().leftPushAll(getRedisKey(conversationId), msgStrList);
  }

  /**
   * 查询对话
   *
   * @param conversationId
   * @param lastN
   * @return
   */
  @Override
  public List<Message> get(String conversationId, int lastN) {
    if (lastN <= 0) {
      return List.of();
    }
    String redisKey = getRedisKey(conversationId);

    List<String> msgStrList = redisTemplate.opsForList().range(redisKey, 0, lastN);
    if (CollUtil.isEmpty(msgStrList)) {
      return List.of();
    }

    List<Message> messageList = msgStrList.stream().map(msg -> JSONUtil.toBean(msg, Message.class)).collect(Collectors.toList());
    return messageList;
  }

  /**
   * 清除对话
   *
   * @param conversationId
   */
  @Override
  public void clear(String conversationId) {
    redisTemplate.delete(getRedisKey(conversationId));
  }

  /**
   * 获取 redis key, 格式 CHAT:userId:conversationId
   *
   * @param conversationId
   * @return
   */
  private String getRedisKey(String conversationId) {
    return String.format("%s:%s", "CHAT", conversationId);
  }
}
