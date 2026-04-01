package com.sparta.msa.lesson.domain.rag.dto.response;

import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimilaritySearchResponse {
  String query;
  int resultCount;
  List<SearchResult> results;

  @Getter
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class SearchResult {
    String id;
    String content;
    Map<String, Object> metadata;
  }
}