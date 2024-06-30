package io.teletronics.storage_app.repository;

import io.teletronics.storage_app.document.FileContentHashDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface FileContentHashRepository extends ReactiveMongoRepository<FileContentHashDocument, String> {

    Mono<String> findByFileHash(String fileHash);
}
