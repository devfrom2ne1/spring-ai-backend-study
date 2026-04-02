package com.sparta.msa.lesson.domain.agent.dto.response;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultiAgentResult {

  String problem;

  @Builder.Default
  List<AgentExecution> executions = new ArrayList<>();

  String finalOutput;

  public void addExecution(AgentExecution execution) {
    this.executions.add(execution);
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class AgentExecution {

    String agentName;
    String taskDescription;
    String output;
  }

}