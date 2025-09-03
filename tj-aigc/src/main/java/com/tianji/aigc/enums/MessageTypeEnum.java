package com.tianji.aigc.enums;

import org.springframework.ai.chat.messages.*;

/**
 * @author luruoyang
 */
public enum MessageTypeEnum {
  USER("user"),
  ASSISTANT("assistant"),
  SYSTEM("system"),
  TOOL("tool");

  private final String value;

  MessageTypeEnum(String value) {
    this.value = value;
  }

  public static MessageTypeEnum fromValue(String value) {
    for (MessageTypeEnum messageType : MessageTypeEnum.values()) {
      if (messageType.getValue().equals(value)) {
        return messageType;
      }
    }
    throw new IllegalArgumentException("Invalid MessageType value: " + value);
  }

  public String getValue() {
    return this.value;
  }

}
