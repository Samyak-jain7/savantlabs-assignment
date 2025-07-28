package com.github.connector;

import com.github.connector.controller.GitHubController;
import com.github.connector.model.RepositoryInfo;
import com.github.connector.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Collections;

import static org.mockito.Mockito.when;

@WebFluxTest(GitHubController.class)
class GitHubControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GitHubService gitHubService;

    @Test
    void getRepositories() {
        RepositoryInfo repositoryInfo = new RepositoryInfo("test-repo", Collections.emptyList());
        when(gitHubService.getRepositories("octocat")).thenReturn(Flux.just(repositoryInfo));

        webTestClient.get().uri("/api/github/octocat")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RepositoryInfo.class)
                .hasSize(1)
                .contains(repositoryInfo);
    }
}
