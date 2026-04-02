package com.sparta.msa.lesson.domain.agent.service;

import com.sparta.msa.lesson.domain.function.tools.FunctionTools;
import com.sparta.msa.lesson.global.exception.DomainException;
import com.sparta.msa.lesson.global.exception.DomainExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

  private final ChatClient.Builder chatClientBuilder;
  private final FunctionTools functionTools;

  @Transactional
  public String solveWithAgent(String goal){
    log.info("[ReAct Agent] 실행: {}", goal);
    String systemPrompt = """
        당신은 자율적인 AI 에이전트입니다. 목표 달성을 위해 다음 단계를 반복하세요:
        1. Thought: 현재 상황을 분석하고 필요한 행동을 생각합니다.
        2. Action: 적절한 도구를 선택하여 실행합니다.
        3. Observation: 도구의 결과를 확인하고 지식을 업데이트합니다.
        4. Answer: 모든 정보가 모이면 최종 답변을 작성합니다.
        """;

    return chatClientBuilder.build().prompt()
        .system(systemPrompt)
        .user(goal)
        .call()
        .content();
  }

  /**
   * 패턴 2: Plan-and-Execute (계획 후 실행) 작업을 바로 시작하지 않고, 먼저 전체 계획을 세운 뒤 실행합니다.
   */
  public String planAndExecute(String goal) {
    log.info("[Plan-and-Execute] 시작: {}", goal);

    try {
      // 1. 계획 수립 (Planner)
      String plan = chatClientBuilder.build().prompt()
          .system("당신은 복잡한 목표를 위한 논리적인 단계를 수립하는 전략가입니다.")
          .user("목표: " + goal + "\n이 목표를 달성하기 위한 상세 계획을 번호를 매겨 세워주세요.")
          .call()
          .content();

      log.info("[Plan-and-Execute] 생성된 계획:\n{}", plan);

      // 2. 계획 실행 (Executor)
      String execute = chatClientBuilder.build().prompt()
          .system("당신은 주어진 계획을 정확히 이행하는 실행 전문가입니다. 제공된 도구를 활용하세요.")
          .tools(functionTools)
          .user("수립된 계획: " + plan + "\n위 계획을 실행하고 최종 결과를 보고하세요.")
          .call()
          .content();
      log.info("[Plan-and-Execute] 최종 결과:\n{}", execute);
      return execute;

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
  }

  /**
   * 패턴 3: Self-Reflection (자기 반성)
   * 답변을 내놓은 후, 스스로 오류를 검증하고 수정 과정을 거칩니다.
   */
  public String solveWithReflection(String problem, int maxIterations) {
    log.info("Self-Reflection Agent 시작: {}", problem);

    ChatClient client = chatClientBuilder.build();
    String currentResponse = "";
    String feedback = "초기 시도";

    for (int i = 0; i < maxIterations; i++) {
      log.info("[Self-Reflection] 반복 단계: {}/{}", i + 1, maxIterations);

      // 1. 해결 시도 또는 개선
      currentResponse = client.prompt()
          .system("도구를 사용하여 문제를 해결하세요. 이전 피드백이 있다면 반영하여 답변을 개선하세요.")
          .tools(functionTools)
          .user("문제: " + problem + "\n이전 피드백: " + feedback)
          .call()
          .content();

      // 2. 검증 (Critique)
      feedback = client.prompt()
          .system("제시된 검토 대상 답변이 사용자가 원하는 요구사항을 전부 명확하게 찾아냈는지 검토하세요. 모든 요구사항이 충족됐다면 'APPROVED'라고 답하세요.")
          .user("검토 대상 답변: " + currentResponse)
          .call()
          .content();

      log.warn("[Self-Reflection] 개선 필요 사항 발견: {}", feedback);

      if (feedback.contains("APPROVED")) {
        log.info("[Self-Reflection] 답변 최종 승인됨");
        break;
      }
    }
    return currentResponse;
  }
}
