package com.sparta.msa.lesson.domain.agent.controller;

import com.sparta.msa.lesson.domain.agent.dto.request.AgentRequest;
import com.sparta.msa.lesson.domain.agent.dto.request.FeedbackRequest;
import com.sparta.msa.lesson.domain.agent.dto.response.AgentResponse;
import com.sparta.msa.lesson.domain.agent.dto.response.MultiAgentResult;
import com.sparta.msa.lesson.domain.agent.service.MultiAgentSystem;
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
@RequestMapping("/api/agent/multi")
public class MultiAgentController {
  private final MultiAgentSystem multiAgentSystem;

  /**
   * 1. 순차적 협업 (Sequential Chain)
   * Researcher → Analyst → Writer 순서로 실행하며 각 단계 결과를 반환합니다.
   */
  @PostMapping("/sequential")
  public ApiResponse<MultiAgentResult> solveSequential(@RequestBody AgentRequest request) {
    log.info("[API] Sequential Multi-Agent: {}", request.getGoal());

    MultiAgentResult result = multiAgentSystem.solveSequential(request.getGoal());

    return ApiResponse.ok(result);
  }

  /**
   * 2. 동적 파이프라인 (Dynamic Orchestration)
   * 문제 유형을 분석하여 최적의 에이전트 조합을 구성합니다.
   */
  @PostMapping("/dynamic")
  public ApiResponse<MultiAgentResult> solveWithDynamicAgents(@RequestBody AgentRequest request) {
    log.info("[API] Dynamic Multi-Agent: {}", request.getGoal());

    MultiAgentResult result = multiAgentSystem.solveWithDynamicAgents(request.getGoal());

    return ApiResponse.ok(result);
  }

  /**
   * 3. 피드백 루프 (Iterative Review)
   * 분석 품질이 미흡하면 재연구를 수행하여 결과를 개선합니다.
   */
  @PostMapping("/feedback")
  public ApiResponse<AgentResponse> solveWithFeedback(@RequestBody FeedbackRequest request) {
    log.info("[API] Feedback Loop: {}, maxIterations: {}", request.getProblem(), request.getMaxIterations());

    String result = multiAgentSystem.solveWithFeedback(request.getProblem(), request.getMaxIterations());

    return ApiResponse.ok(AgentResponse.builder()
        .mode("Feedback Loop")
        .problem(request.getProblem())
        .iterations(request.getMaxIterations())
        .result(result)
        .build());
  }
}
