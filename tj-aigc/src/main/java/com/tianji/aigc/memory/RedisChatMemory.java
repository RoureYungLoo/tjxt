package com.tianji.aigc.memory;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tianji.aigc.entity.MyAssitantMessage;
import com.tianji.aigc.entity.RedisMessage;
import com.tianji.aigc.tools.constant.ToolConstant;
import com.tianji.aigc.tools.result.ToolResultHolder;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
    // List<String> msgStrList = messages.stream().map(message -> JSONUtil.toJsonStr(message)).collect(Collectors.toList());
    List<String> msgStrList = messages.stream().map(message -> {
      // 构建 redisMessage 对象
      RedisMessage redisMessage = BeanUtil.toBean(message, RedisMessage.class);
      // 设置消息内容
      redisMessage.setTextContent(message.getText());
      // 设置 Tool Calling 相关
      if (message instanceof AssistantMessage assistantMessage) {
        String messageId = message.getMetadata().get("id").toString();
        String requestId = ToolResultHolder.get(messageId, ToolConstant.REQUEST_ID).toString();
        if (StrUtil.isNotBlank(requestId)) {
          // 设置 卡片 信息
          Map<String, Object> params = ToolResultHolder.get(requestId);
          redisMessage.setParams(params);
        }
        redisMessage.setToolCalls(assistantMessage.getToolCalls());
      } else if (message instanceof ToolResponseMessage toolResponseMessage) {
        redisMessage.setToolResponses(toolResponseMessage.getResponses());
      }
      return JSONUtil.toJsonStr(redisMessage);
    }).collect(Collectors.toList());
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

    //List<Message> messageList = msgStrList.stream().map(msg -> JSONUtil.toBean(msg, Message.class)).collect(Collectors.toList());
    List<Message> messageList = msgStrList.stream().map(msgStr -> {
      // 把RedisMessage字符串转为对象
      RedisMessage redisMessage = JSONUtil.toBean(msgStr, RedisMessage.class);
      // 根据不同的MessageType，创建不同的Message对象
      Message message = null;
      MessageType type = redisMessage.getMessageType();
      switch (type) {
        case USER ->
            message = new UserMessage(type, redisMessage.getTextContent(), redisMessage.getMedia(), redisMessage.getMetadata());
        case ASSISTANT ->
          // message = new AssistantMessage(redisMessage.getTextContent(), redisMessage.getMetadata(), redisMessage.getToolCalls(), redisMessage.getMedia());
            message = new MyAssitantMessage(
                redisMessage.getTextContent(),
                redisMessage.getMetadata(),
                redisMessage.getToolCalls(),
                redisMessage.getParams());
        case TOOL -> message = new ToolResponseMessage(redisMessage.getToolResponses(), redisMessage.getMetadata());
        case SYSTEM -> message = new SystemMessage(redisMessage.getTextContent());
      }
      return message;
    }).collect(Collectors.toList());
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