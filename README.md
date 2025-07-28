# GitHub Repository Activity Connector

This project is a Spring Boot application that provides a REST endpoint to fetch public repositories for a given GitHub user or organization, along with the last 20 commits for each repository.

## Tech Stack

* Java 17
* Spring Boot 3.1.0
* Spring WebFlux
* Maven
* Lombok
* JUnit 5
* Mockito

## Steps to Run the Application

1. **Clone the repository:**

   ```bash
   git clone https://github.com/your-username/savantlabs-assignment.git
   cd savantlabs-assignment
   ```

2. **Set up GitHub Token:**

   You need to provide a GitHub personal access token. You can do this in one of two ways:

   *   **Environment Variable (Recommended):**

       Set the `GITHUB_TOKEN` environment variable:

       ```bash
       export GITHUB_TOKEN=ghp_your_token_here
       ```

   *   **application.properties:**

       Open `src/main/resources/application.properties` and add your token:

       ```properties
       github.token=ghp_your_token_here
       ```

3. **Build and Run the application:**

   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`.

## Running with Docker

You can also run this application using Docker:

1. **Build the Docker image:**

   ```bash
   docker build -t github-connector .
   ```

2. **Run the Docker container:**

   You need to provide your GitHub token as an environment variable:

   ```bash
   docker run -e GITHUB_TOKEN=ghp_your_token_here -p 8080:8080 github-connector
   ```

   The application will be available at `http://localhost:8080`.

## Example curl Command

```bash
curl http://localhost:8080/api/github/octocat
```

### Sample JSON Output

```json
[
  {
    "name": "boysenberry-repo-1",
    "commits": [
      {
        "message": "Initial commit",
        "author": "octocat",
        "timestamp": "2011-01-26T19:01:12Z"
      }
    ]
  },
  {
    "name": "git-consortium",
    "commits": [
      {
        "message": "Merge pull request #6 from spraints/add-js-to-vendor-in-gitignore\n\nAdd js to vendor in gitignore",
        "author": "Monalisa Octocat",
        "timestamp": "2011-04-14T16:00:49Z"
      }
    ]
  }
]
```

## Rate Limiting and Error Handling

The application handles GitHub API rate limits gracefully. It logs the rate limit status after each API call. If the rate limit is exceeded, it returns a `403 Forbidden` error. It also handles other errors, such as `404 Not Found` for users or organizations that don't exist, and retries on `5xx` server errors.

```