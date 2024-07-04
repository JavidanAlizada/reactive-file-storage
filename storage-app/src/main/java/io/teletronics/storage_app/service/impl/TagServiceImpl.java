package io.teletronics.storage_app.service.impl;

import io.teletronics.storage_app.document.TagDocument;
import io.teletronics.storage_app.dto.response.TagResponse;
import io.teletronics.storage_app.exception.TagNotExistException;
import io.teletronics.storage_app.repository.TagRepository;
import io.teletronics.storage_app.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private static final String CACHE_KEY_TAG = "tags";
    private static final String TAG_FILE_PATH = "tags.txt";

    @Autowired
    public TagServiceImpl(TagRepository tagRepository,
                          ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.tagRepository = tagRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<TagResponse> loadTags() {
        return reactiveRedisTemplate.opsForSet().members(CACHE_KEY_TAG)
                .collectList()
                .flatMap(redisTags -> {
                    if (!redisTags.isEmpty()) {
                        return Mono.just(new TagResponse(redisTags));
                    } else {
                        return fetchTagsFromMongoAndCache();
                    }
                })
                .onErrorMap(throwable -> new TagNotExistException());
    }

    private Mono<TagResponse> fetchTagsFromMongo() {
        return this.tagRepository.findAll()
                .map(TagDocument::getTag)
                .flatMap(tag ->
                        reactiveRedisTemplate.opsForSet().add(CACHE_KEY_TAG, tag)
                                .thenReturn(tag)
                )
                .collectList()
                .map(TagResponse::new);
    }

    private Mono<TagResponse> fetchTagsFromMongoAndCache() {
        return this.tagRepository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        return loadTagsToMongo().then(fetchTagsFromMongo());
                    } else {
                        return fetchTagsFromMongo();
                    }
                });
    }

    private Mono<Void> loadTagsToMongo() {
        List<TagDocument> tagDocumentList = readTagsFromTxtFile();
        return tagRepository.insert(tagDocumentList).then();
    }

    private List<TagDocument> readTagsFromTxtFile() {
        Resource resource = new ClassPathResource(TAG_FILE_PATH);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(TagDocument::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read tags.txt file", e);
        }
    }


    @Override
    public Mono<Boolean> tagExists(String tag) {
        return reactiveRedisTemplate.hasKey(CACHE_KEY_TAG)
                .flatMap(cacheKeyExist -> {
                    if (cacheKeyExist) {
                        return reactiveRedisTemplate.opsForSet().isMember(CACHE_KEY_TAG, tag);
                    } else {
                        return loadTags()
                                .then(reactiveRedisTemplate.opsForSet().isMember(CACHE_KEY_TAG, tag));
                    }
                })
                .onErrorResume(throwable -> Mono.error(new IllegalArgumentException("Failed to check tag existence", throwable)));
    }
}
