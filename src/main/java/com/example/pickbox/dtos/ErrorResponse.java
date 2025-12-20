package com.example.pickbox.dtos;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private String path;
}
