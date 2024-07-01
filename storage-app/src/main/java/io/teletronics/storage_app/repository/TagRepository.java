package io.teletronics.storage_app.repository;

import io.teletronics.storage_app.document.TagDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends ReactiveMongoRepository<TagDocument, String> {


}
