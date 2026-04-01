package com.sparta.msa.lesson.domain.ai.service;
import com.sparta.msa.lesson.domain.ai.dto.response.ChatResponse;
import com.sparta.msa.lesson.global.exception.DomainException;
import com.sparta.msa.lesson.global.exception.DomainExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
  private final ChatClient.Builder clientBuilder;

  public ChatResponse chat(String message) {
    try {
      String response = clientBuilder.build()
          .prompt()
          .user(message)
          .call()
          .content();

      return ChatResponse.builder().message(response).build();
    } catch (Exception e) {
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
  }

  public ChatResponse chatWithContext(String userMessage) {
    try {
      String response = clientBuilder.build()
          .prompt()
          .system("""
              당신은 '스파르타 몰'의 친절하고 전문적인 쇼핑 어시스턴트입니다.
              당신의 목표는 고객이 최적의 상품을 찾도록 돕고, 쇼핑 과정의 궁금증을 해결해 주는 것입니다.
              
              지침:
              1. 항상 밝고 친절한 말투를 사용하세요. (예: ~해드릴까요?, ~입니다!)
              2. 상품 추천 시에는 사용자의 니즈를 다시 한번 확인하고 제안하세요.
              3. 배송이나 결제 문의에는 신중하고 정확하게 답변하세요.
              4. 모든 답변은 한국어로 작성하세요.
              5. 모든 말에 시작점에는 "안녕하세요 쿠팡입니다."로 시작하세요.
              """)
          .user(userMessage)
          .call()
          .content();

      return ChatResponse.builder().message(response).build();
    } catch (Exception e) {
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
  }
}
