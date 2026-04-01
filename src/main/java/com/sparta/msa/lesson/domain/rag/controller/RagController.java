package com.sparta.msa.lesson.domain.rag.controller;

import com.sparta.msa.lesson.domain.rag.dto.request.QuestionRequest;
import com.sparta.msa.lesson.domain.rag.dto.response.AnswerResponse;
import com.sparta.msa.lesson.domain.rag.dto.response.RagResponse;
import com.sparta.msa.lesson.domain.rag.dto.response.SearchSummaryResponse;
import com.sparta.msa.lesson.domain.rag.service.RagService;
import com.sparta.msa.lesson.domain.vector.dto.response.SimilaritySearchResponse;
import com.sparta.msa.lesson.global.response.ApiResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rag")
public class RagController {

  private final RagService ragService;

  /**
   * 질문 → 벡터 검색 → LLM 답변
   * 가장 기본적인 RAG. 답변만 반환
   * @param request
   * @return
   */
  @PostMapping("/ask")
  public ApiResponse<AnswerResponse> ask(@RequestBody QuestionRequest request){
    return ApiResponse.ok( ragService.ask(request.getQuestion()));
  }

  /**
   * 특정 문서 안에서만 검색해서 답변
   * ex) documentId = "계약서A" → 그 문서 내용만 참고해서 답변
   * @param documentId
   * @param request
   * @return
   */
  @PostMapping("/ask-in-document/{documentId}")
  public ApiResponse<AnswerResponse> askInDocument(@PathVariable String documentId,
      @RequestBody QuestionRequest request) {
    return ApiResponse.ok(ragService.askInDocument(request.getQuestion(), documentId));
  }

  /**
   * 질문 → 벡터 검색 → LLM 답변
   * + 어떤 문서를 참고했는지 출처도 같이 반환
   * @param request
   * @return
   */
  @PostMapping("/ask-in-source")
  public ApiResponse<RagResponse> askWithSource(@RequestBody QuestionRequest request) {
    return ApiResponse.ok(ragService.askWithSource(request.getQuestion()));
  }

  /**
   *
   * @param query
   * @param topK
   * @return
   */
  @GetMapping("/search")
  public ApiResponse<SimilaritySearchResponse> search(
      @RequestParam String query,
      @RequestParam(defaultValue = "5") int topK) {
    List<Document> docs = ragService.searchDocuments(query, topK, 0.0);
    return ApiResponse.ok(ragService.toSearchResponse(query, docs));
  }

  /**
   * 검색 결과를 요약해서 반환
   * @param request
   * @return
   */
  @PostMapping("/search-summary")
  public ApiResponse<SearchSummaryResponse> searchSummary(@RequestBody QuestionRequest request){
    return ApiResponse.ok(ragService.getSearchSummary(request.getQuestion()));
  }



}
