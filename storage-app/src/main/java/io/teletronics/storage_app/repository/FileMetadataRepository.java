package io.teletronics.storage_app.repository;

import io.teletronics.storage_app.document.FileMetadataDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileMetadataRepository extends ReactiveMongoRepository<FileMetadataDocument, String> {

    Flux<FileMetadataDocument> findByVisibility(String visibility);

    Mono<Boolean> existsByFilename(String filename);

    Mono<FileMetadataDocument> findByIdAndUsername(String fileId, String username);

    Mono<FileMetadataDocument> findByFileHashId(String fileHashId);
}
