package com.tianji.aigc.service;

import com.tianji.aigc.dto.ChatDTO;
import com.tianji.aigc.vo.ChatEventVO;
import reactor.core.publisher.Flux;

/**
 * 对话业务接口类
 *
 * @Author mr.wu
 * @Date 2025-8-17 15:27
 */
public interface ChatService {

    /**
     * 聊天对话
     * @param dto
     * @return
     */
    Flux<ChatEventVO> chat(ChatDTO dto);
}