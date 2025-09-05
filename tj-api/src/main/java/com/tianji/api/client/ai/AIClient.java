package com.tianji.api.client.ai;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI服务Feign接口
 *
 * @Author mr.wu
 * @Date 2025-8-31 23:21
 */
@FeignClient(contextId = "agic", value = "aigc-service", path = "/chat")
public interface AIClient {
  @PostMapping("/text")
  public String chatText(@RequestBody String question);
}