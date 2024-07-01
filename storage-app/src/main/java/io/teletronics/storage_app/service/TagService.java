package io.teletronics.storage_app.service;

import io.teletronics.storage_app.dto.response.TagResponse;
import reactor.core.publisher.Mono;

public interface TagService {

    Mono<TagResponse> loadTags();

    Mono<Boolean> tagExists(String tag);
}
