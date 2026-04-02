package com.sparta.msa.lesson.domain.agent.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReflectionRequest {

  String problem;
  int maxIterations = 3;

}