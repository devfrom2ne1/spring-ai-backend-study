package com.sparta.msa.lesson.domain.agent.service;

import com.sparta.msa.lesson.domain.agent.dto.response.ExecutionResult;
import com.sparta.msa.lesson.domain.agent.dto.response.ExecutionResult.StepResult;
import com.sparta.msa.lesson.domain.function.tools.FunctionTools;
import com.sparta.msa.lesson.global.exception.DomainException;
import com.sparta.msa.lesson.global.exception.DomainExceptionCode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanningAgent {
  private final ChatClient.Builder chatClientBuilder;
  private final FunctionTools functionTools;

  private static final String PLANNER_SYSTEM_PROMPT = """
      당신은 효율적인 계획을 수립하는 전문가입니다. 목표 달성을 위해 구체적이고 실행 가능한 계획을 세우세요.
      계획 작성 형식:
      1. [단계명]: [작업 내용] - 도구: [도구명]
      2. [단계명]: [작업 내용] - 도구: [도구명]

      사용 가능 도구: getWeather, calculator, getCurrentTime
      각 단계는 이전 단계의 결과를 활용하도록 설계하세요.
      """;

  /**
   * 1. 기본 Plan-and-Execute
   * 계획을 세우고 통째로 실행합니다.
   */
  public String executeWithPlanning(String goal) {
    log.info("[Basic Plan-and-Execute] 시작: {}", goal);

    String plan = chatClientBuilder.build().prompt()
        .system(PLANNER_SYSTEM_PROMPT)
        .user(goal)
        .call()
        .content();

    log.info("[Basic Plan-and-Execute] 수립된 계획:\n{}", plan);

    String executionPrompt = """
          다음 계획을 단계별로 실행하고 최종 결과를 요약하세요.
          계획: %s
          원래 목표: %s
          """.formatted(plan, goal);

    return chatClientBuilder.build().prompt()
        .system("당신은 계획을 정확하게 실행하는 AI 에이전트입니다.")
        .tools(functionTools)
        .user(executionPrompt)
        .call()
        .content();

  }
  /**
   * 2. 상세 추적형 실행 (Detailed Tracking)
   * 계획을 파싱하여 한 단계씩 개별적으로 실행하고 기록합니다.
   */
  public ExecutionResult executeWithDetailedTracking(String goal) {
    log.info("[Detailed Tracking] 시작: {}", goal);

    try {
      String plan = chatClientBuilder.build().prompt()
          .system(PLANNER_SYSTEM_PROMPT)
          .user(goal)
          .call()
          .content();

      List<String> steps = parseSteps(plan);
      List<StepResult> stepResults = new ArrayList<>();
      String context = "";

      for (int i = 0; i < steps.size(); i++) {
        String currentStep = steps.get(i);
        int stepNum = i + 1;
        log.info("[Detailed Tracking] 단계 {}/{} 실행: {}", stepNum, steps.size(), currentStep);

        String stepResult = chatClientBuilder.build().prompt()
            .system("당신은 주어진 단계를 실행하는 전문가입니다. 이전 문맥을 참고하세요.")
            .tools(functionTools)
            .user("전체목표: %s\n현재단계: %s\n이전까지의 문맥: %s".formatted(goal, currentStep, context))
            .call()
            .content();

        stepResults.add(StepResult.builder()
            .stepNumber(stepNum)
            .description(currentStep)
            .result(stepResult)
            .build());

        log.info("[Detailed Tracking] 단계 {}/{} 결과: {}", stepNum, steps.size(), stepResults);
        context += "\n[단계 " + stepNum + " 결과]: " + stepResult;
      }

      String finalSummary = summarizeResults(goal, stepResults);

      return ExecutionResult.builder()
          .goal(goal)
          .plan(plan)
          .stepResults(stepResults)
          .finalResult(finalSummary)
          .build();
    } catch (DomainException e) {
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
  }
  /**
   * 3. 적응형 계획 (Adaptive Planning)
   * 실행 결과를 검증하고, 실패 시 계획을 재수립(Replanning)합니다.
   */
  public String executeWithAdaptivePlanning(String goal, int maxReplanning) {
    log.info("[Adaptive Planning] 시작: {}", goal);

    try {
      String currentPlan = chatClientBuilder.build().prompt()
          .system(PLANNER_SYSTEM_PROMPT)
          .user(goal)
          .call()
          .content();

      String lastResult = "";

      for (int i = 0; i <= maxReplanning; i++) {
        lastResult = executeWithPlanning(currentPlan);

        String validation = chatClientBuilder.build().prompt()
            .system("결과가 목표를 달성했는지 평가하세요. 달성했다면 'SUCCESS', 아니면 'INCOMPLETE: 이유'라고 답하세요.")
            .user("목표: %s\n결과: %s".formatted(goal, lastResult))
            .call()
            .content();

        if (validation.startsWith("SUCCESS")) {
          log.info("[Adaptive Planning] 목표 달성 성공 (시도: {})", i + 1);
          return lastResult;
        }

        log.warn("[Adaptive Planning] 계획 재수립 필요 (사유: {})", validation);
        currentPlan = chatClientBuilder.build().prompt()
            .system("피드백을 바탕으로 계획을 수정하세요.")
            .user("기존계획: %s\n실패결과: %s\n피드백: %s".formatted(currentPlan, lastResult, validation))
            .call()
            .content();
      }

      return lastResult;
    } catch (DomainException e) {
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
  }

  private List<String> parseSteps(String plan) {
    return plan.lines()
        .map(String::trim)
        .filter(line -> line.matches("^\\d+\\..*"))
        .collect(Collectors.toList());
  }
  private String summarizeResults(String goal, List<StepResult> stepResults) {
    String combined = stepResults.stream()
        .map(s -> "단계 " + s.getStepNumber() + ": " + s.getResult())
        .collect(Collectors.joining("\n"));

    return chatClientBuilder.build().prompt()
        .system("실행 결과들을 종합하여 사용자에게 최종 보고서를 작성하세요.")
        .user("목표: " + goal + "\n결과들:\n" + combined)
        .call()
        .content();
  }
}
