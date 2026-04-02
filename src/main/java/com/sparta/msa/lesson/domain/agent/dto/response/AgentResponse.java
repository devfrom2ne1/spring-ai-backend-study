package com.sparta.msa.lesson.domain.agent.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AgentResponse {

  String mode;
  String goal;
  String problem;
  List<String> usedTools;
  Integer iterations;
  String result;

}