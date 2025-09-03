package com.tianji.aigc.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.tianji.aigc.memory.RedisChatMemory;
import com.tianji.aigc.properties.SessionProperties;
import com.tianji.aigc.tools.CourseTool;
import com.tianji.aigc.tools.OrderTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringAI配置类
 *
 * @Author mr.wu
 * @Date 2025-8-17 15:01
 */
@Configuration
public class SpringAiConfig {

  @Autowired
  private SessionProperties sessionProperties;

  @Bean
  public ChatClient chatClient(DashScopeChatModel chatModel, RedisChatMemory redisChatMemory, CourseTool courseTool, OrderTool orderTool) {
    List<Advisor> advisors = new ArrayList<>();
    // 日志记录器
    advisors.add(new SimpleLoggerAdvisor());
    // 基于 Redis 的会话记忆
    advisors.add(new MessageChatMemoryAdvisor(redisChatMemory));
    return ChatClient
        //对话模型对象
        .builder(chatModel)
        .defaultSystem(sessionProperties.getSystem())
        .defaultAdvisors(advisors)
        // function / tool calling
        .defaultTools(courseTool, orderTool)
        .build();
  }
}