package com.tianji.aigc.entity;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.model.Media;

import java.util.List;
import java.util.Map;

public class MyAssitantMessage extends AssistantMessage {
  private Map<String, Object> params;

  public MyAssitantMessage(String content) {
    super(content);
  }

  public MyAssitantMessage(String content, Map<String, Object> properties) {
    super(content, properties);
  }

  public MyAssitantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls) {
    super(content, properties, toolCalls);
  }

  public MyAssitantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls, List<Media> media) {
    super(content, properties, toolCalls, media);
  }

  public MyAssitantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls, Map<String, Object> params) {
    super(content, properties, toolCalls);
    this.params = params;
  }


  @Override
  public MessageType getMessageType() {
    return this.messageType;
  }

  @Override
  public String getText() {
    return this.textContent;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }
}