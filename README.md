# 🤖 Spring AI Agentic Workflow Infrastructure

이 프로젝트는 **Spring AI**를 기반으로 기업용 데이터를 활용한 **고성능 RAG(Retrieval-Augmented Generation)** 인프라와 스스로 판단하고 행동하는 **지능형 에이전트 구조**를 구현한 레퍼런스 모델입니다.

## 🌟 Key Features

### 1. 고성능 RAG 인프라 구축
* **Vector DB 연동**: 실시간 기업 데이터를 벡터화하여 저장하고, 시맨틱 검색을 통해 정확도 높은 답변을 생성합니다. 
* **Spring AI 인터페이스**: 다양한 LLM(OpenAI, Claude 등)과 Vector Store(Pinecone, Milvus 등)를 유연하게 교체 가능한 구조를 설계했습니다.

### 2. 지능형 Advisor & Function Calling
* **Contextual Awareness**: Advisor를 활용해 대화 맥락을 유지하고 정제된 프롬프트를 에이전트에게 전달합니다.
* **Tool Orchestration**: LLM이 스스로 필요한 외부 도구(API, DB 조회 등)를 판단하여 호출하는 **Function Calling** 구조를 구현했습니다. 

### 3. 에이전틱 워크플로우 (ReAct & Multi-Agent)
* **ReAct Pattern**: 'Reasoning'과 'Acting'을 반복하여 복잡한 추론 문제를 단계적으로 해결합니다.
* **Multi-Agent Collaboration**: 역할이 분담된 여러 에이전트가 협업하여 비즈니스 로직을 완수하는 멀티 에이전트 패턴을 통합했습니다.

---

## 🛠 Tech Stack
* **Framework**: Spring Boot 3.3.11, Spring AI
* **Language**: Java 21
* **Vector DB**: PGVector
* **LLM**: Gemini
* **Build Tool**: Gradle

---

## 🚀 Getting Started

### Installation
```bash
# 레포지토리 클론
git clone https://github.com/사용자이름/레포지토리명.git

# 설정 파일(application-sample.yml)에 API 키 입력
# spring.ai.openai.api-key: ${YOUR_API_KEY}
# 설정 파일(application-sample.yml) 파일명  `application-local.yml` 로 변경
```

---

## 📖 구현 상세 (Implementation Details)

### Advisor 설정 예시
`QuestionAnswerAdvisor`를 사용하여 Vector DB의 지식을 답변 생성에 결합하는 로직을 포함하고 있습니다.

### Function Calling 구조
금융 데이터 조회 등 외부 시스템과의 인터페이스를 `java.util.function.Function` 인터페이스로 정의하여 LLM이 이를 인지하고 사용할 수 있도록 설계했습니다.

---
