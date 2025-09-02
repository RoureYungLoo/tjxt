package com.tianji.aigc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.vo.SessionVO;

import java.util.List;

public interface IChatSessionService extends IService<ChatSession> {

  /**
   * 创建会话session
   *
   * @param num 热门问题的数量
   * @return 会话信息
   */
  SessionVO createSession(Integer num);

  /**
   * 查询热门Prompt
   * @param num 数量
   * @return 会话信息
   */
  List<SessionVO.Example> getHotPrompt(Integer num);
}