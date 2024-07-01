# Teletronics Storage Application
The Teletronics Storage Application is a Spring Boot-based file storage and management system that allows users to upload, rename, list, delete, and download files. This README provides an overview of the application structure, setup instructions, and usage guide.

### Features
* File Upload: Allows users to upload files with specified metadata such as filename, visibility, and tags.
* File Management: Supports operations like renaming files, listing files with filtering and sorting options, and deleting files.
* Security: Uses JWT-based authentication and authorization to secure endpoints.
* Persistence: Stores file metadata in MongoDB and uses Redis for caching and other data operations.
* Reactive Programming: Utilizes Spring WebFlux to handle asynchronous and non-blocking operations.

### Technologies Used
* Spring Boot: Application framework for creating standalone, production-grade Spring-based applications.
* Spring WebFlux: Reactive web framework for building reactive microservices.
* MongoDB: NoSQL database for storing file metadata.
* Redis: In-memory data structure store for caching.
* JUnit 5 & Mockito: Testing frameworks for unit and integration testing.
* Swagger UI: API documentation and testing tool.
* Docker: Containerization technology for packaging the application and its dependencies.

### Prerequisites
Before running the application, ensure you have the following installed:

* JDK 17 or higher
* Docker (optional, for containerized deployment)
* MongoDB (or use Docker to run MongoDB container)
* Redis (or use Docker to run Redis container)

### Setup Instructions
1. Clone the repository:



```
git clone https://github.com/your/repository.git
cd storage-app
```
2. Configure application properties:

Update application.yml or application.properties with MongoDB and Redis connection details as per your environment.

Build and run the application:

```
java -jar build/libs/storage-app.jar
```

3. Alternatively, you can use Docker to build and run the application:

```
docker-compose up --build
```

* This command will build the Docker images and start containers for MongoDB, Redis, and the application.

4. Accessing the application:

`Once the application is running, you can access it at http://localhost:8080.

### Additional Information
**Sorting Options**:
All possible values for sortBy parameter when listing files are:

* FILENAME
* UPLOAD_DATE
* TAG
* CONTENT_TYPE
* FILE_SIZE

**Tag Migration**:
Tags are initially loaded from tags.txt file during application startup and migrated to MongoDB for storage.

### Troubleshooting
* Port conflicts: Ensure that ports 8080 (application), 27017 (MongoDB), and 6379 (Redis) are not used by other applications.
* Database setup: If using Docker, check Docker logs (docker-compose logs) for any initialization errors with MongoDB or Redis containers.
* API issues: If encountering issues with API requests, verify request payloads and ensure required headers (e.g., Authorization) are correctly set.