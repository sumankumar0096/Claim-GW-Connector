# GWConnector

Spring Boot application that integrates with Guidewire GW APIs for policy and claim operations.

Quick setup

1. Create a GitHub repository and push the code:

```bash
git init
git add .
git commit -m "Initial commit"
# create remote repo via GitHub UI or CLI then:
git remote add origin git@github.com:<your-org>/<repo>.git
git branch -M main
git push -u origin main
```

2. Deploy to Render (two options):

- Option A — Render (Service, no Docker):
  - Create a new Web Service in Render and connect your GitHub repo.
  - Environment: `Service`
  - Build Command: `mvn -DskipTests package`
  - Start Command: `java -jar target/*.jar`
  - Branch: `main`
  - Add environment variables in Render dashboard: `admin-api.url`, `policy-api.username`, `policy-api.password`, `claim-api.url`, etc.

- Option B — Render (Docker):
  - Create a new Web Service and choose `Docker` as the environment. Render will build the included `Dockerfile`.
  - Set environment variables as above.

Notes
- The app reads `server.port` from environment variable `PORT` (Render sets `PORT`), so it will bind correctly.
- Secure credentials via Render's environment variables (do NOT commit secrets).

CI

A GitHub Actions workflow for building on push is included in `.github/workflows/maven.yml`.

## API documentation

With the service running, use the interactive Swagger UI at:

```
http://localhost:8081/swagger-ui/index.html
```

The machine-readable OpenAPI 3 specification is available at:

```
http://localhost:8081/v3/api-docs
```-

The UI documents the policy, claim, FNOL, and claim-adjustor endpoints and their request/response schemas. The connector's Guidewire credentials are configured on the server; do not enter them in Swagger requests.
