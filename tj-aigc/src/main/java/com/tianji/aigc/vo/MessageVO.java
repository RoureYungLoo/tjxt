package com.tianji.aigc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.MessageType;

import java.util.Map;

/**
 * @author luruoyang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {
  private MessageType type;
  private String content;
  private Map<String, Object> param;
}
