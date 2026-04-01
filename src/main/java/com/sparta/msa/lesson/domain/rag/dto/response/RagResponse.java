package com.sparta.msa.lesson.domain.rag.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RagResponse {
  String answer;
  List<DocumentSource> sources;

  @Getter
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class DocumentSource {
    String fileName;
    String documentId;
    String preview; // 본문
  }
}