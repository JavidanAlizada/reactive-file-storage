package io.teletronics.storage_app.repository;

import io.teletronics.storage_app.document.UserDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;


public interface UserRepository extends ReactiveMongoRepository<UserDocument, String> {

    Mono<UserDocument> findByUsername(String username);
}
