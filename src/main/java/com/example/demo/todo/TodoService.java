package com.example.demo.todo;

import java.util.List;

import com.example.demo.todo.dto.CreateTodoRequest;
import com.example.demo.todo.dto.TodoResponse;
import com.example.demo.todo.dto.UpdateTodoRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional
    public TodoResponse create(CreateTodoRequest request) {
        return TodoResponse.from(todoRepository.save(new Todo(request.title())));
    }

    public List<TodoResponse> findAll() {
        return todoRepository.findAll(Sort.by("id").ascending()).stream()
                .map(TodoResponse::from)
                .toList();
    }

    public TodoResponse findById(Long id) {
        return TodoResponse.from(getTodo(id));
    }

    @Transactional
    public TodoResponse update(Long id, UpdateTodoRequest request) {
        Todo todo = getTodo(id);
        todo.updateTitle(request.title());
        todo.changeCompletion(request.completed());
        return TodoResponse.from(todo);
    }

    @Transactional
    public void delete(Long id) {
        todoRepository.delete(getTodo(id));
    }

    private Todo getTodo(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
}
