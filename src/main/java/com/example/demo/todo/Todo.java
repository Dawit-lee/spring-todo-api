package com.example.demo.todo;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "todos")
public class Todo {

    public static final int MAX_TITLE_LENGTH = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "제목은 비어 있거나 공백뿐일 수 없습니다.")
    @Size(max = MAX_TITLE_LENGTH, message = "제목은 200자 이하여야 합니다.")
    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Todo() {
    }

    public Todo(String title) {
        this.title = title;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void changeCompletion(boolean completed) {
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
