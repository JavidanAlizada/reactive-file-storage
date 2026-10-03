# Reactive File Storage

A fully **non-blocking file storage and management service** built with **Spring WebFlux**, **reactive MongoDB** and **reactive Redis**, secured with **JWT**.

Users can upload, list, filter, sort, rename, tag, download and delete files through a REST API. Everything is handled end to end on a reactive (Project Reactor) pipeline, from the HTTP layer to the database.

## Highlights
- **Reactive end to end:** WebFlux + `spring-data-mongodb-reactive` + `spring-data-redis-reactive`, with no blocking calls on request threads
- **JWT authentication & authorization** with Spring Security
- **File metadata & content hashing** in MongoDB (duplicate detection via content hash)
- **Visibility control** (public / private files) and **tagging**
- **Filtering, sorting and pagination** (by filename, upload date, tag, content type, size)
- **Redis** for caching
- **OpenAPI / Swagger UI** documentation
- **Docker Compose** setup for the app, MongoDB and Redis, plus a GitLab CI pipeline
- Unit tests with **JUnit 5 & Mockito**

## Tech stack
`Java 17` `Spring Boot 3.3` `Spring WebFlux` `Spring Security` `JWT` `MongoDB (reactive)` `Redis (reactive)` `SpringDoc OpenAPI` `Docker` `Maven` `JUnit 5` `Mockito`

## Architecture
```
Client ──► WebFlux Router/Controllers ──► Services (Reactor Mono/Flux)
                 │                              │
           JWT Security filter          ┌───────┴────────┐
                                        ▼                ▼
                               MongoDB (metadata,     Redis
                               users, tags, hashes)   (cache)
```

## Quick start
```bash
git clone https://github.com/JavidanAlizada/reactive-file-storage.git
cd reactive-file-storage/storage-app
docker-compose up --build
```
- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/ · API docs: http://localhost:8080/api-docs

## Repository layout
```
storage-app/
├── src/main/java/.../config       # MongoDB, Redis, OpenAPI, messages
├── src/main/java/.../controller   # Auth, File, Tag endpoints + global error handling
├── src/main/java/.../document     # MongoDB documents
├── src/main/java/.../dto          # Request / response models
├── src/main/java/.../service      # Business logic
├── Dockerfile, docker-compose.yml
└── gitlab-ci.yml
```

📖 Full documentation (setup, API, sorting options, troubleshooting): [`storage-app/README.md`](storage-app/README.md)
