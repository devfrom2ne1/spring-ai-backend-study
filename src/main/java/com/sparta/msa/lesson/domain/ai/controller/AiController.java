package com.sparta.msa.lesson.domain.ai.controller;

import com.sparta.msa.lesson.domain.ai.dto.response.ProductAnalysisResponse;
import com.sparta.msa.lesson.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
  private final ChatClient chatClient;

  // 생성자 주입을 통한 ChatClient 빌드
  public AiController(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @PostMapping("/chat")
  public ApiResponse<String> chat(@RequestBody String message) {
    String response = chatClient.prompt()
        .user(message)
        .call()
        .content(); // 문자열로 결과 반환

    return ApiResponse.ok(response);
  }

  @GetMapping("/marketing")
  public ApiResponse<String> generateMarketing(
      @RequestParam(value = "productName") String productName,
      @RequestParam(value = "features") String features
  ) {
    String template = """
        제품명 {productName}의 마케팅 문구를 작성하세요.
        주요 특징: {features}
        조건: emoji를 사용해서 젠지감성이고 20자 이내로 작성할 것.
        """;

    String response = chatClient.prompt()
        .user(u -> u.text(template)
            .param("productName", productName)
            .param("features", features))
        .call()
        .content();

    return ApiResponse.ok(response);
  }

  @GetMapping("/translate")
  public ApiResponse<String> translate(
      @RequestParam(value = "text") String text,
      @RequestParam(value = "targetLanguage", defaultValue = "영어") String targetLanguage
  ) {
    String response = chatClient.prompt()
        // 1. AI의 페르소나 설정 (System Message)
        .system("당신은 전문 번역가입니다. 주어진 텍스트를 문맥에 맞게 자연스럽게 번역해주세요.")

        // 2. 동적 파라미터 주입 (Prompt Template)
        .user(u -> u.text("다음 텍스트를 {lang}로 번역해주세요: {text}")
            .param("lang", targetLanguage)
            .param("text", text))
        .call()
        .content();

    return ApiResponse.ok(response);
  }


  @GetMapping("/analyze")
  public ApiResponse<ProductAnalysisResponse> analyzeReview(@RequestParam(value = "review") String review) {

    String promptText = """
        다음 제품 리뷰를 분석해주세요:
        
        리뷰 내용: {review}
        
        요구사항:
        1. sentiment는 positive, neutral, negative 중 하나로 응답하세요.
        2. score는 1점에서 10점 사이의 정수로 응답하세요.
        3. summary는 분석 내용을 한 문장으로 요약하세요.
        """;

    ProductAnalysisResponse response =  chatClient.prompt()
        .user(u -> u.text(promptText).param("review", review))
        .call()
        .entity(ProductAnalysisResponse.class); // 핵심: 객체로 자동 변환

    return ApiResponse.ok(response);
  }


}
