package com.tianji.aigc.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringAI配置类
 *
 * @Author mr.wu
 * @Date 2025-8-17 15:01
 */
@Configuration
public class SpringAiConfig {

    @Bean
    public ChatClient chatClient(DashScopeChatModel chatModel) {
        return ChatClient
                .builder(chatModel) //对话模型对象
                .defaultAdvisors(new SimpleLoggerAdvisor()) //日志记录器
                .build();
    }
}