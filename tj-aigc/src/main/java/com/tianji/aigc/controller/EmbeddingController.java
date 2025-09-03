package com.tianji.aigc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luruoyang
 */
@RestController
@RequestMapping("/embedding")
@Slf4j
public class EmbeddingController {

  @Autowired
  private VectorStore vectorStore;

  @Autowired
  private EmbeddingModel embeddingModel;

  /**
   * 添加文档到ES索引库
   *
   * @param messages
   */
  @PostMapping
  public void addDocumentToVector(
      @RequestParam("messages") List<String> messages
  ) {
    // 类型转换
    List<Document> documentList = messages.stream().map(message -> {
      return Document.builder().text(message).build();
    }).collect(Collectors.toList());
    // 导入到ES
    vectorStore.add(documentList);

    log.info("[向量库添加数据]添加完毕，添加成功{}条", messages.size());
  }
}
