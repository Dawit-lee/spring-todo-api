package com.example.demo.todo.dto;

import java.util.List;
import com.example.demo.todo.Todo;
import org.springframework.data.domain.Page;

public record TodoPageResponse(
        List<TodoResponse> content, int page, int size, long totalElements, int totalPages) {

    public static TodoPageResponse from(Page<Todo> result) {
        return new TodoPageResponse(result.getContent().stream().map(TodoResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
