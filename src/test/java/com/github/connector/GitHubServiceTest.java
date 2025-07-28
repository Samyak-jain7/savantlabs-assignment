package com.github.connector;

import com.github.connector.model.CommitInfo;
import com.github.connector.model.RepositoryInfo;
import com.github.connector.service.GitHubService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

class GitHubServiceTest {

    private static MockWebServer mockWebServer;
    private GitHubService gitHubService;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.create(baseUrl);
        gitHubService = new GitHubService(webClient);
    }

    @Test
    void getRepositories() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[{\"name\":\"test-repo\"}]")
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("[{\"commit\":{\"message\":\"test-commit\",\"author\":{\"name\":\"test-author\",\"date\":\"2025-07-28T12:34:56Z\"}}}]")
                .addHeader("Content-Type", "application/json"));

        Flux<RepositoryInfo> repositoryInfoFlux = gitHubService.getRepositories("octocat");

        StepVerifier.create(repositoryInfoFlux)
                .expectNextMatches(repositoryInfo -> {
                    return repositoryInfo.getName().equals("test-repo") &&
                            repositoryInfo.getCommits().get(0).getMessage().equals("test-commit") &&
                            repositoryInfo.getCommits().get(0).getAuthor().equals("test-author") &&
                            repositoryInfo.getCommits().get(0).getTimestamp().equals(Instant.parse("2025-07-28T12:34:56Z"));
                })
                .verifyComplete();
    }
}
