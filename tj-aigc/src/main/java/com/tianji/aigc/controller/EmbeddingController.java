package com.tianji.aigc.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

  /**
   * 删除向量数据库中的文档
   *
   * @param ids
   */
  @DeleteMapping
  public void deleteVectorStore(@RequestParam("ids") List<String> ids) {
    vectorStore.delete(ids);
  }

  /**
   * 内容搜索
   *
   * @param keyword
   * @return
   */
  @GetMapping("/search")
  public List<Document> search(@RequestParam("message") String keyword) {
    SearchRequest request = SearchRequest.builder()
        .query(keyword)
        .topK(999)
        .build();
    List<Document> documentList = vectorStore.similaritySearch(request);
    return documentList;
  }

  /**
   * 搜索全部向量数据库
   *
   * @return
   */
  @GetMapping("/search/all")
  public List<Document> searchAll() {
    SearchRequest request = SearchRequest.builder()
        .query("")
        .topK(999)
        .build();
    List<Document> documentList = vectorStore.similaritySearch(request);
    return documentList;
  }
}
