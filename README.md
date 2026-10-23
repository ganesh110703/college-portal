# CampusHub — College Portal (React + Java API + MySQL)

## Requirements
Docker Engine/Desktop and Docker Compose v2.

## Run
From this folder:
```bash
docker compose up --build -d
docker compose ps
```
Open http://localhost:3001. The app lists seeded students and lets you add records. Health endpoint: `curl http://localhost:3001/api/health`.

## Ports / networking
- **Public/published:** host port `3001` -> Nginx frontend container port `80`.
- **Private/internal:** Java API port `8080` and MySQL port `3306` are only exposed to the Compose network; no host ports are published.
- Nginx proxies `/api/*` to `backend:8080`; Java connects to MySQL using `mysql:3306`.
- Java runtime image is Ubuntu 22.04 with OpenJDK 17 installed. Maven builds the runnable JAR in a separate build stage.

## Credentials
The compose file provides development-only defaults for local use. Override `DB_PASSWORD` and `MYSQL_ROOT_PASSWORD` in your shell or a local `.env` file before use beyond a demo. Do not commit production credentials.

## Stop
`docker compose down`; database data persists in the named volume. `docker compose down -v` deletes the volume.
