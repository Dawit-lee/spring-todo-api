package com.example.demo.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TodoApiTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoRepository repository;

    @BeforeEach
    void clearData() {
        repository.deleteAll();
    }

    @Test
    void createsListsReadsUpdatesAndDeletesTodo() throws Exception {
        mvc.perform(get("/api/todos")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());

        String body = mvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Spring 공부\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Spring 공부"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.createdAt").isString())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(body);
        long id = created.get("id").asLong();
        String path = "/api/todos/" + id;

        mvc.perform(get("/api/todos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));

        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Spring 복습\",\"completed\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Spring 복습"))
                .andExpect(jsonPath("$.completed").value(true));
        String updatedBody = mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andReturn().getResponse().getContentAsString();
        // H2의 타임스탬프 정밀도 차이를 고려해 생성 시각을 비교합니다.
        assertThat(java.time.Instant.parse(objectMapper.readTree(updatedBody).get("createdAt").asText()))
                .isCloseTo(java.time.Instant.parse(created.get("createdAt").asText()),
                        org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.MILLIS));

        mvc.perform(put(path).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Spring 복습\",\"completed\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(false));
        mvc.perform(get(path)).andExpect(jsonPath("$.completed").value(false));
        mvc.perform(delete(path)).andExpect(status().isNoContent())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEmpty());
        mvc.perform(get(path)).andExpect(status().isNotFound());
        mvc.perform(get("/api/todos")).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createsMaximumLengthTitleWithLocationHeader() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", "가".repeat(200)));
        var result = mvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertThat(result.getResponse().getHeader("Location")).isEqualTo("/api/todos/" + id);
    }

    static Stream<String> invalidTitles() {
        return Stream.of("{}", "{\"title\":null}", "{\"title\":\"\"}",
                "{\"title\":\"   \"}", "{\"title\":\"" + "가".repeat(201) + "\"}");
    }

    @ParameterizedTest
    @MethodSource("invalidTitles")
    void rejectsInvalidCreateTitle(String body) throws Exception {
        mvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].message").isString());
        assertThat(repository.count()).isZero();
    }

    @ParameterizedTest
    @MethodSource("invalidTitles")
    void rejectsInvalidUpdateTitleWithoutChangingStoredTodo(String body) throws Exception {
        Todo todo = repository.saveAndFlush(new Todo("원래 제목"));
        var json = (com.fasterxml.jackson.databind.node.ObjectNode) objectMapper.readTree(body);
        json.put("completed", true);
        mvc.perform(put("/api/todos/{id}", todo.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(json)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("title"));
        Todo unchanged = repository.findById(todo.getId()).orElseThrow();
        assertThat(unchanged.getTitle()).isEqualTo("원래 제목");
        assertThat(unchanged.isCompleted()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"title\":\"수정\"}", "{\"title\":\"수정\",\"completed\":null}"})
    void requiresCompletionOnUpdate(String body) throws Exception {
        Todo todo = repository.saveAndFlush(new Todo("할 일"));
        mvc.perform(put("/api/todos/{id}", todo.getId()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("completed"));
    }

    @Test
    void missingTodoReturnsConsistent404ForReadUpdateAndDelete() throws Exception {
        for (var request : java.util.List.of(get("/api/todos/999999"),
                put("/api/todos/999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"수정\",\"completed\":true}"), delete("/api/todos/999999"))) {
            mvc.perform(request).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("할 일을 찾을 수 없습니다. id=999999"))
                    .andExpect(jsonPath("$.errors").isEmpty());
        }
    }

    @Test
    void frameworkErrorsUseSameShape() throws Exception {
        mvc.perform(post("/api/todos").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
        mvc.perform(get("/api/todos/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.errors").isArray());
        mvc.perform(get("/missing")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404)).andExpect(jsonPath("$.errors").isArray());
        mvc.perform(post("/api/todos/1")).andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status").value(405)).andExpect(jsonPath("$.errors").isArray());
        mvc.perform(post("/api/todos").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415)).andExpect(jsonPath("$.errors").isArray());
    }
}
