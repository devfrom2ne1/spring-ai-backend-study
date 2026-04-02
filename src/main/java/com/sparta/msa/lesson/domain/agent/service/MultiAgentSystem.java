package com.sparta.msa.lesson.domain.agent.service;
import com.sparta.msa.lesson.domain.agent.dto.response.MultiAgentResult;
import com.sparta.msa.lesson.domain.agent.dto.response.MultiAgentResult.AgentExecution;
import com.sparta.msa.lesson.domain.function.tools.FunctionTools;
import com.sparta.msa.lesson.global.constants.enums.AgentRole;
import com.sparta.msa.lesson.global.exception.DomainException;
import com.sparta.msa.lesson.global.exception.DomainExceptionCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiAgentSystem {
  private final ChatClient.Builder chatClientBuilder;
  private final FunctionTools functionTools;

  private static final String RESEARCHER_PROMPT = """
      당신은 정보 수집 전문 연구원입니다. getWeather, getCurrentTime 도구를 활용하여
      문제 해결에 필요한 팩트와 데이터를 체계적으로 수집하세요.
      """;

  private static final String ANALYST_PROMPT = """
      당신은 데이터 분석 전문가입니다. 수집된 정보를 바탕으로 인사이트를 도출하세요.
      수치 계산이 필요하면 calculator 도구를 사용하고, 논리적인 인과관계를 분석하세요.
      """;

  private static final String WRITER_PROMPT = """
      당신은 전문 리포트 작성자입니다. 분석 내용을 바탕으로 구조화된 보고서를 작성하세요.
      구성: 1.요약, 2.주요발견, 3.상세분석, 4.결론
      """;

  /**
   * 패턴 1: 순차적 협업 (Sequential Chain)
   * Researcher → Analyst → Writer 순서로 데이터가 흐릅니다.
   */
  public MultiAgentResult solveSequential(String problem) {
    log.info("[Sequential] 시작: {}", problem);

    try {
      MultiAgentResult result = MultiAgentResult.builder().problem(problem).build();

      String research = executeResearcher(problem);
      result.addExecution(AgentExecution.builder()
          .agentName("Researcher")
          .taskDescription(problem)
          .output(research)
          .build());

      String analysis = executeAnalyst(research);
      result.addExecution(AgentExecution.builder()
          .agentName("Analyst")
          .taskDescription(research)
          .output(analysis)
          .build());

      String finalOutput = executeWriter(analysis);
      result.addExecution(AgentExecution.builder()
          .agentName("Writer")
          .taskDescription(analysis)
          .output(finalOutput)
          .build());

      return MultiAgentResult.builder()
          .problem(result.getProblem())
          .executions(result.getExecutions())
          .finalOutput(finalOutput)
          .build();
    } catch (DomainException e) {
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
  }

  /**
   * 패턴 2: 동적 파이프라인 (Dynamic Orchestration)
   * 문제 유형을 먼저 분석하고, 그에 맞는 에이전트 조합을 구성합니다.
   */
  public MultiAgentResult solveWithDynamicAgents(String problem) {
    log.info("[Dynamic Orchestration] 시작: {}", problem);

    try {
      String problemType = analyzeProblemType(problem);
      log.info("[Dynamic Orchestration] 분석된 문제 유형: {}", problemType);

      List<AgentRole> pipeline = switch (problemType) {
        case "CALCULATION" -> List.of(AgentRole.ANALYST, AgentRole.WRITER);
        case "DATA_COLLECTION" -> List.of(AgentRole.RESEARCHER, AgentRole.WRITER);
        default -> List.of(AgentRole.RESEARCHER, AgentRole.ANALYST, AgentRole.WRITER);
      };

      MultiAgentResult result = MultiAgentResult.builder().problem(problem).build();
      String currentInput = problem;

      for (AgentRole role : pipeline) {
        String agentName = role.name().charAt(0) + role.name().substring(1).toLowerCase();
        String output = switch (role) {
          case RESEARCHER -> executeResearcher(currentInput);
          case ANALYST -> executeAnalyst(currentInput);
          case WRITER -> executeWriter(currentInput);
        };

        result.addExecution(AgentExecution.builder()
            .agentName(agentName)
            .taskDescription(currentInput)
            .output(output)
            .build());

        currentInput = output;
      }

      return MultiAgentResult.builder()
          .problem(result.getProblem())
          .executions(result.getExecutions())
          .finalOutput(currentInput)
          .build();
    } catch (DomainException e) {
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
  }

  /**
   * 패턴 3: 피드백 루프 (Iterative Review)
   * 분석 결과가 미흡하면 연구 단계로 다시 돌려보내 품질을 높입니다.
   */
  public String solveWithFeedback(String problem, int maxIterations) {
    log.info("[Feedback Loop] 시작: {}", problem);

    try {
      String research = executeResearcher(problem);
      String result = "";

      for (int i = 0; i < maxIterations; i++) {
        log.info("[Feedback Loop] 반복 단계: {}/{}", i + 1, maxIterations);

        String analysis = executeAnalyst(research);

        String feedback = chatClientBuilder.build().prompt()
            .system("분석 결과가 충분한지 평가하세요. 완벽하면 'APPROVED', 부족하면 피드백을 주세요.")
            .user("문제: " + problem + "\n분석결과: " + analysis)
            .call()
            .content();

        if (feedback.contains("APPROVED")) {
          log.info("[Feedback Loop] 품질 승인됨");
          return executeWriter(analysis);
        }

        log.warn("[Feedback Loop] 품질 미달로 재연구 수행 (반복: {}): {}", i + 1, feedback);
        research = executeResearcher("이전 연구: " + research + "\n피드백 반영: " + feedback);
        result = analysis;
      }

      return result;
    } catch (DomainException e) {
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
  }

  private String executeResearcher(String input) {
    return chatClientBuilder.build().prompt()
        .system(RESEARCHER_PROMPT)
        .tools(functionTools)
        .user(input)
        .call()
        .content();
  }

  private String executeAnalyst(String input) {
    return chatClientBuilder.build().prompt()
        .system(ANALYST_PROMPT)
        .tools(functionTools)
        .user(input)
        .call()
        .content();
  }

  private String executeWriter(String input) {
    return chatClientBuilder.build().prompt()
        .system(WRITER_PROMPT)
        .user(input)
        .call()
        .content();
  }
  private String analyzeProblemType(String problem) {
    return chatClientBuilder.build().prompt()
        .system("문제 유형을 하나만 선택하세요: CALCULATION, DATA_COLLECTION, ANALYSIS")
        .user(problem)
        .call()
        .content()
        .trim();
  }
}
