package com.tianji.aigc.controller;

import com.tianji.aigc.dto.ChatDTO;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import com.tianji.aigc.vo.TemplateVO;
import com.tianji.common.annotations.NoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 对话控制器类
 *
 * @Author mr.wu
 * @Date 2025-8-17 15:06
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

  @Autowired
  private ChatService chatService;

  /**
   * 聊天接口（流式输出）
   *
   * @param dto
   * @return
   */
  @NoWrapper //标记结果不进行包装
  @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE /* text/event-stream */)
  public Flux<ChatEventVO> chat(@RequestBody ChatDTO dto) {
    return chatService.chat(dto);
  }

  /**
   * 停止输出
   *
   * @param sessionId
   */
  @PostMapping("/stop")
  public void stop(String sessionId) {
    chatService.stop(sessionId);
  }

  /**
   * 文本聊天接口
   *
   * @param question
   * @return
   */
  @PostMapping("/text")
  public String chatText(@RequestBody String question) {
    return chatService.chatText(question);
  }

  private static final TemplateVO TEMPLATE_VO = new TemplateVO();

  @GetMapping("/templates")
  public TemplateVO getTemplates() {
    return TEMPLATE_VO;
  }
}