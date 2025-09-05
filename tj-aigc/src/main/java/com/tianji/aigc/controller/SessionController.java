package com.tianji.aigc.controller;

import com.tianji.aigc.service.IChatSessionService;
import com.tianji.aigc.vo.ChatHistoryVO;
import com.tianji.aigc.vo.MessageVO;
import com.tianji.aigc.vo.SessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

  private final IChatSessionService chatSessionService;

  /**
   * 新建会话
   */
  @PostMapping
  public SessionVO createSession(@RequestParam(value = "n", defaultValue = "3") Integer num) {
    return this.chatSessionService.createSession(num);
  }

  /**
   * 热门问题
   */
  @GetMapping("/hot")
  public List<SessionVO.Example> getHotPrompt(@RequestParam(value = "n", defaultValue = "3") Integer num) {
    return this.chatSessionService.getHotPrompt(num);
  }

  /**
   * 查询会话详情
   */
  @GetMapping("/{sessionId}")
  public List<MessageVO> messageList(@PathVariable("sessionId") String sessionId) {
    return chatSessionService.messageList(sessionId);
  }

  /**
   * 查询历史会话列表
   * @return
   */
  @GetMapping("/history")
  public Map<String,List<ChatHistoryVO>> getChatHistoryList() {
    return chatSessionService.getChatHistoryList();
  }

}