package com.sparta.msa.lesson.domain.agent.controller;

import com.sparta.msa.lesson.domain.agent.dto.request.AgentRequest;
import com.sparta.msa.lesson.domain.agent.dto.request.ReflectionRequest;
import com.sparta.msa.lesson.domain.agent.dto.response.AgentResponse;
import com.sparta.msa.lesson.domain.agent.service.AgentService;
import com.sparta.msa.lesson.global.response.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agent")
public class AgentController {

  private final AgentService agentService;

  /**
   * 1. 기본 ReAct Agent 실행 (자율적 문제 해결)
   * 상황에 따라 필요한 도구를 스스로 선택해 결과를 도출합니다.
   */
  @PostMapping("/solve")
  public ApiResponse<AgentResponse> solve(@RequestBody AgentRequest request) {
    log.info("[API] ReAct Agent: {}", request.getGoal());

    String result = agentService.solveWithAgent(request.getGoal());

    return ApiResponse.ok(AgentResponse.builder()
        .mode("ReAct")
        .goal(request.getGoal())
        .result(result)
        .build());
  }

  /**
   * 2. 계획-실행(Plan-and-Execute) Agent
   * 작업을 바로 실행하지 않고 전체 로드맵을 먼저 설계한 뒤 수행합니다.
   */
  @PostMapping("/plan-execute")
  public ApiResponse<AgentResponse> planAndExecute(@RequestBody AgentRequest request) {
    log.info("[API] Plan & Execute: {}", request.getGoal());

    String result = agentService.planAndExecute(request.getGoal());

    return ApiResponse.ok(AgentResponse.builder()
        .mode("Plan-and-Execute")
        .goal(request.getGoal())
        .result(result)
        .build());
  }

  /**
   * 3. 자기 반성(Self-Reflection) Agent
   * 답변을 생성한 후 스스로 검토하고 수정하는 과정을 거칩니다.
   */
  @PostMapping("/reflect")
  public ApiResponse<AgentResponse> solveWithReflection(@RequestBody ReflectionRequest request) {
    log.info("[API] Reflection: {}, Max: {}", request.getProblem(), request.getMaxIterations());

    String result = agentService.solveWithReflection(request.getProblem(), request.getMaxIterations());

    return ApiResponse.ok(AgentResponse.builder()
        .mode("Self-Reflection")
        .problem(request.getProblem())
        .iterations(request.getMaxIterations())
        .result(result)
        .build());
  }
}
