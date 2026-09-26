package com.example.demo.todo.dto;

import java.time.Instant;
import com.example.demo.todo.Todo;

public record TodoResponse(Long id, String title, boolean completed, Instant createdAt) {

    public static TodoResponse from(Todo todo) {
        return new TodoResponse(todo.getId(), todo.getTitle(), todo.isCompleted(), todo.getCreatedAt());
    }
}
