package com.github.connector.service;

import com.github.connector.model.CommitInfo;
import com.github.connector.model.RepositoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);
    private final WebClient webClient;

    public GitHubService(WebClient webClient) {
        this.webClient = webClient;
    }

    

    public Flux<RepositoryInfo> getRepositories(String username) {
        return fetchRepositoriesRecursive(username, "/users/" + username + "/repos?per_page=100");
    }

    private Flux<RepositoryInfo> fetchRepositoriesRecursive(String username, String url) {
        return webClient.get()
                .uri(url)
                .exchangeToFlux(response -> {
                    logRateLimit(response);
                    if (response.statusCode().is2xxSuccessful()) {
                        String linkHeader = response.headers().header("Link").stream().findFirst().orElse(null);
                        String nextPageUrl = extractNextPageUrl(linkHeader);

                        Flux<RepositoryInfo> currentRepos = response.bodyToFlux(Repository.class)
                                .doOnNext(repo -> log.info("Fetched repo: {}", repo.name()))
                                .flatMap(repo -> getCommits(username, repo.name())
                                        .map(commits -> new RepositoryInfo(repo.name(), commits)));

                        if (nextPageUrl != null) {
                            return currentRepos.concatWith(fetchRepositoriesRecursive(username, nextPageUrl));
                        } else {
                            return currentRepos;
                        }
                    } else {
                        return this.<RepositoryInfo>handleError(response, "repositories for user " + username).flux();
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).filter(this::is5xxServerError));
    }

    private String extractNextPageUrl(String linkHeader) {
        if (linkHeader == null || linkHeader.isEmpty()) {
            return null;
        }
        String[] links = linkHeader.split(", ");
        for (String link : links) {
            if (link.contains("rel=\"next\"")) {
                int uriStart = link.indexOf('<');
                int uriEnd = link.indexOf('>');
                if (uriStart > -1 && uriEnd > -1) {
                    return link.substring(uriStart + 1, uriEnd);
                }
            }
        }
        return null;
    }

    private Mono<List<CommitInfo>> getCommits(String username, String repoName) {
        return webClient.get()
                .uri("/repos/{username}/{repoName}/commits?per_page=20", username, repoName)
                .exchangeToMono(response -> {
                    logRateLimit(response);
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToFlux(Commit.class)
                                .map(commit -> new CommitInfo(commit.commit().message(), commit.commit().author().name(), commit.commit().author().date()))
                                .doOnNext(commitInfo -> log.info("Fetched commit for {}: {}", repoName, commitInfo.getMessage()))
                                .collectList();
                    } else {
                        return handleError(response, "commits for repo " + repoName);
                    }
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).filter(this::is5xxServerError))
                .flatMap(repoInfo -> {
                    // This flatMap is now outside the exchangeToFlux, so repoInfo is already RepositoryInfo
                    // No need to fetch commits again here, as it's already done in the inner flatMap
                    return Mono.just(repoInfo);
                });
    }

    private void logRateLimit(ClientResponse response) {
        response.headers().header("X-RateLimit-Limit").stream().findFirst().ifPresent(limit ->
                response.headers().header("X-RateLimit-Remaining").stream().findFirst().ifPresent(remaining ->
                        response.headers().header("X-RateLimit-Reset").stream().findFirst().ifPresent(reset ->
                                log.info("Rate Limit: {}/{}, Resets at: {}", remaining, limit, Instant.ofEpochSecond(Long.parseLong(reset))))));
    }

    private <T> Mono<T> handleError(ClientResponse response, String context) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    String errorMessage = String.format("Failed to fetch %s. Status: %s, Body: %s", context, response.statusCode(), body);
                    log.error(errorMessage);
                    if (response.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new UserNotFoundException(errorMessage));
                    }
                    if (response.statusCode() == HttpStatus.FORBIDDEN) {
                        return Mono.error(new RateLimitException(errorMessage));
                    }
                    return Mono.error(new GitHubApiException(errorMessage));
                });
    }
    
    private boolean is5xxServerError(Throwable throwable) {
        if (throwable instanceof GitHubApiException) {
            String message = throwable.getMessage();
            return message != null && message.contains("Status: 5");
        }
        return false;
    }


    // Custom Exceptions
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) {
            super(message);
        }
    }

    public static class GitHubApiException extends RuntimeException {
        public GitHubApiException(String message) {
            super(message);
        }
    }


    private record Repository(String name) {}
    private record Commit(CommitDetails commit) {}
    private record CommitDetails(String message, Author author) {}
    private record Author(String name, Instant date) {}
}
