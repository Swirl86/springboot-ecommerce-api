package com.swirl.ecomengine.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        String path,
        Map<String, String> errors
) {}
