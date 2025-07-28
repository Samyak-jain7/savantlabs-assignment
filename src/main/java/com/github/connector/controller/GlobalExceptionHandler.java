package com.github.connector.controller;

import com.github.connector.service.GitHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GitHubService.UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(GitHubService.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(GitHubService.RateLimitException.class)
    public ResponseEntity<String> handleRateLimitException(GitHubService.RateLimitException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(GitHubService.GitHubApiException.class)
    public ResponseEntity<String> handleGitHubApiException(GitHubService.GitHubApiException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
