package com.example.demo.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.core.converter.ModelConverters;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todoOpenApi() {
        return new OpenAPI().info(new Info().title("할 일 API").version("1.0")
                .description("회원 기능 없는 할 일 관리 REST API"));
    }

    @Bean
    public OpenApiCustomizer errorResponses() {
        return api -> {
            ModelConverters.getInstance().read(ApiError.class).forEach(api.getComponents()::addSchemas);
            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                operation.getResponses().addApiResponse("400", error("잘못된 입력 또는 요청 형식"));
                operation.getResponses().addApiResponse("500", error("서버 내부 오류"));
                if (path.contains("{id}")) {
                    operation.getResponses().addApiResponse("404", error("없는 할 일"));
                }
                if (method.name().equals("POST") || method.name().equals("PUT")) {
                    operation.getResponses().addApiResponse("415", error("지원하지 않는 Content-Type"));
                }
            }));
        };
    }

    private ApiResponse error(String description) {
        return new ApiResponse().description(description).content(new Content()
                .addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ApiError"))));
    }
}
