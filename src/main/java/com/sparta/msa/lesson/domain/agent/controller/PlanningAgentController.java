package com.sparta.msa.lesson.domain.agent.controller;

import com.sparta.msa.lesson.domain.agent.dto.request.AgentRequest;
import com.sparta.msa.lesson.domain.agent.dto.response.AgentResponse;
import com.sparta.msa.lesson.domain.agent.dto.response.ExecutionResult;
import com.sparta.msa.lesson.domain.agent.service.PlanningAgent;
import com.sparta.msa.lesson.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agent/planning")
public class PlanningAgentController {

  private final PlanningAgent planningAgent;

  /**
   * 1. 기본 Plan-and-Execute
   * 계획을 세우고 통째로 실행합니다.
   */
  @PostMapping
  public ApiResponse<AgentResponse> executeWithPlanning(@RequestBody AgentRequest request) {
    log.info("[API] Basic Plan-and-Execute: {}", request.getGoal());

    String result = planningAgent.executeWithPlanning(request.getGoal());

    return ApiResponse.ok(AgentResponse.builder()
        .mode("Plan-and-Execute")
        .goal(request.getGoal())
        .result(result)
        .build());
  }

  /**
   * 2. 상세 추적형 실행 (Detailed Tracking)
   * 단계별 실행 과정과 결과를 구조화된 객체로 반환합니다.
   */
  @PostMapping("/detailed")
  public ApiResponse<ExecutionResult> executeWithDetailedTracking(@RequestBody AgentRequest request) {
    log.info("[API] Detailed Tracking: {}", request.getGoal());

    ExecutionResult result = planningAgent.executeWithDetailedTracking(request.getGoal());

    return ApiResponse.ok(result);
  }

}
