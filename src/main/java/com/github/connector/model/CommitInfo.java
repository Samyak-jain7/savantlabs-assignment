package com.github.connector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitInfo {
    private String message;
    private String author;
    private Instant timestamp;
}
