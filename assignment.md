Problem Statement:
You are tasked with building a GitHub Repository Activity Connector. This connector should authenticate using a personal access token and retrieve a list of public repositories for a given GitHub user or organization. For each repository, fetch the last 20 commits along with the commit message, author, and timestamp.


Requirements:
Accept a GitHub username as input.
Paginate through repositories if needed.
For each repo, fetch the 20 most recent commits (handle pagination if required).
Handle rate limits and errors gracefully.
Output the data as structured Java objects (POJOs).
Bonus: Provide a basic CLI or REST endpoint to trigger the fetch.

Deliverables:
Open GitHub repo with working code
Updated README with setup and usage instructions
