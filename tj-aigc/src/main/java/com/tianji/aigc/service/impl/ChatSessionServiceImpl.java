package com.tianji.aigc.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.aigc.entity.ChatSession;
import com.tianji.aigc.mapper.ChatSessionMapper;
import com.tianji.aigc.properties.SessionProperties;
import com.tianji.aigc.service.IChatSessionService;
import com.tianji.aigc.vo.MessageVO;
import com.tianji.aigc.vo.SessionVO;
import com.tianji.common.utils.UserContext;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 对话session 服务实现类
 * </p>
 *
 * @author mr.wu
 * @since 2025-08-17
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

  @Autowired
  private SessionProperties sessionProperties;

  /**
   * 新建会话
   *
   * @param num
   * @return
   */
  @Override
  public SessionVO createSession(Integer num) {
    //1.生成会话ID
    String sessionId = IdUtil.fastSimpleUUID();

    //2.保存会话历史
    ChatSession chatSession = ChatSession.builder().sessionId(sessionId)
        .userId(UserContext.getUser()).build();
    this.save(chatSession);

    //3.随机取出3个热门问题
    List<SessionVO.Example> examples = sessionProperties.getExamples();
    Collections.shuffle(examples);

    //4.封装响应结果
    SessionVO sessionVO = SessionVO.builder()
        .sessionId(sessionId)
        .describe(sessionProperties.getDescribe())
        .examples(examples.stream().limit(num).toList())
        .build();

    return sessionVO;
  }

  @Override
  public List<SessionVO.Example> getHotPrompt(Integer num) {
    //3.随机取出3个热门问题
    List<SessionVO.Example> examples = sessionProperties.getExamples();
    Collections.shuffle(examples);

    //4.封装响应结果
    List<SessionVO.Example> exampleList = examples.stream().limit(3).collect(Collectors.toList());

    return exampleList;
  }

  @Autowired
  private ChatMemory chatMemory;

  /**
   * 获取会话详情
   */
  @Override
  public List<MessageVO> messageList(String sessionId) {
    return null;
  }
}