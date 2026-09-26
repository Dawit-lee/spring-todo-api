package com.example.demo.todo;

import java.net.URI;
import com.example.demo.todo.dto.TodoPageResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.demo.todo.dto.CreateTodoRequest;
import com.example.demo.todo.dto.TodoResponse;
import com.example.demo.todo.dto.UpdateTodoRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "할 일", description = "할 일 CRUD 및 완료 여부 관리")
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @Operation(summary = "할 일 생성")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse response = todoService.create(request);
        return ResponseEntity.created(URI.create("/api/todos/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "할 일 목록", description = "ID 오름차순, 0부터 시작하는 페이지. completed 생략 시 전체 조회")
    public TodoPageResponse findAll(
            @Parameter(description = "페이지 번호 (0 이상)")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @Parameter(description = "페이지 크기 (1~100)")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") int size,
            @Parameter(description = "true: 완료, false: 미완료, 생략: 전체")
            @RequestParam(required = false) Boolean completed) {
        return todoService.findAll(page, size, completed);
    }

    @Operation(summary = "할 일 단건 조회")
    @GetMapping("/{id}")
    public TodoResponse findById(@PathVariable Long id) {
        return todoService.findById(id);
    }

    @Operation(summary = "제목 및 완료 여부 수정")
    @PutMapping("/{id}")
    public TodoResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTodoRequest request) {
        return todoService.update(id, request);
    }

    @Operation(summary = "할 일 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @io.swagger.v3.oas.annotations.media.Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
