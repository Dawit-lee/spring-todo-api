package com.example.demo.common;

import java.util.List;

public record ApiError(int status, String message, List<FieldError> errors) {

    public record FieldError(String field, String message) {
    }
}
