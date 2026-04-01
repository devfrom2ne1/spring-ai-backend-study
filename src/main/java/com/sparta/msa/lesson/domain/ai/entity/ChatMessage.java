package com.sparta.msa.lesson.domain.ai.entity;

import com.sparta.msa.lesson.global.constants.enums.ChatMessageType;
import com.sparta.msa.lesson.global.constants.enums.StatusType;
import io.micrometer.core.annotation.Counted;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_messages")
public class ChatMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "conversation_id", nullable = false)
  ChatConversation conversation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  ChatMessageType role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  StatusType status;

  @Column(nullable = false, columnDefinition = "TEXT")
  String message;

  @Column
  Integer promptTokens;

  @Column
  Integer completionTokens;

  @Column
  Integer totalTokens;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  LocalDateTime createdAt;

  @Column(nullable = false)
  @UpdateTimestamp
  LocalDateTime updatedAt;

  @Builder
  public ChatMessage(
      ChatConversation conversation,
      ChatMessageType role,
      StatusType status,
      String message,
      Integer promptTokens,
      Integer completionTokens,
      Integer totalTokens
  ) {
    this.conversation = conversation;
    this.role = role;
    this.status = status;
    this.message = message;
    this.promptTokens = promptTokens;
    this.completionTokens = completionTokens;
    this.totalTokens = totalTokens;
  }
}
