# User Guide

This guide provides an example of how a user can interact with the GitHub Repository Activity Connector API.

## Running the System with Docker

You can use Docker to build and run the application without installing Java or Maven locally.

1. **Build the Docker image:**

   ```bash
   docker build -t github-connector .
   ```

2. **Run the Docker container:**

   Provide your GitHub token as an environment variable:

   ```bash
   docker run -e GITHUB_TOKEN=$GITHUB_TOKEN -p 8080:8080 github-connector
   ```

   The API will be available at `http://localhost:8080`.

## Fetching Repositories

To fetch the repositories for a GitHub user, you can use the following `curl` command:

```bash
curl http://localhost:8080/api/github/{username}
```

Replace `{username}` with the GitHub username you want to fetch repositories for. For example, to fetch repositories for the user `octocat`, you would use the following command:

```bash
curl http://localhost:8080/api/github/octocat
```

### Example Response

The API will return a JSON array of repositories, with each repository containing a list of the last 20 commits.

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

## Error Handling

If an error occurs, the API will return an appropriate HTTP status code and a descriptive error message.

*   **404 Not Found:** If the user does not exist.
*   **403 Forbidden:** If the GitHub API rate limit is exceeded.
*   **500 Internal Server Error:** For any other server-side errors.
