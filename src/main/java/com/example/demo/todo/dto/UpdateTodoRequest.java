package com.example.demo.todo.dto;

import com.example.demo.todo.Todo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTodoRequest(
        @NotBlank(message = "제목은 비어 있거나 공백뿐일 수 없습니다.")
        @Size(max = Todo.MAX_TITLE_LENGTH, message = "제목은 200자 이하여야 합니다.")
        String title,
        @NotNull(message = "완료 여부는 필수입니다.") Boolean completed) {
}
