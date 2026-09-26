package com.example.demo.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class TodoRepositoryTests {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesAndReadsTodoWithGeneratedIdAndDefaults() {
        Todo saved = todoRepository.saveAndFlush(new Todo("Spring 공부하기"));
        Long id = saved.getId();
        assertThat(id).isPositive();
        entityManager.clear();

        Todo found = todoRepository.findById(id).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("Spring 공부하기");
        assertThat(found.isCompleted()).isFalse();
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void updatesTitleAndCompletionWithoutChangingCreatedAt() {
        Todo saved = todoRepository.saveAndFlush(new Todo("변경 전"));
        Long id = saved.getId();
        entityManager.clear();

        Todo todo = todoRepository.findById(id).orElseThrow();
        Instant createdAt = todo.getCreatedAt();
        todo.updateTitle("변경 후");
        todo.changeCompletion(true);
        todoRepository.flush();
        entityManager.clear();

        Todo updated = todoRepository.findById(id).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("변경 후");
        assertThat(updated.isCompleted()).isTrue();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);

        updated.changeCompletion(false);
        todoRepository.flush();
        entityManager.clear();
        assertThat(todoRepository.findById(id).orElseThrow().isCompleted()).isFalse();
    }

    @Test
    void deletesTodo() {
        Todo saved = todoRepository.saveAndFlush(new Todo("삭제할 일"));
        Long id = saved.getId();
        todoRepository.deleteById(id);
        todoRepository.flush();
        entityManager.clear();

        assertThat(todoRepository.findById(id)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsMissingOrBlankTitle(String title) {
        assertThatThrownBy(() -> todoRepository.saveAndFlush(new Todo(title)))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void acceptsTitleAtMaximumLength() {
        String title = "가".repeat(Todo.MAX_TITLE_LENGTH);
        Long id = todoRepository.saveAndFlush(new Todo(title)).getId();
        entityManager.clear();

        assertThat(todoRepository.findById(id).orElseThrow().getTitle()).isEqualTo(title);
    }

    @Test
    void rejectsTitleOverMaximumLength() {
        String title = "가".repeat(Todo.MAX_TITLE_LENGTH + 1);

        assertThatThrownBy(() -> todoRepository.saveAndFlush(new Todo(title)))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
