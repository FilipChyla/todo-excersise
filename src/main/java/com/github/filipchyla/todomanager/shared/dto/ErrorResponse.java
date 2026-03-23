package com.github.filipchyla.todomanager.shared.dto;

public record ErrorResponse(
        int status,
        String message,
        long timestamp
) {}
