package io.teletronics.storage_app.validator;

import io.teletronics.storage_app.constants.FileVisibility;
import io.teletronics.storage_app.exception.TagNotExistException;
import io.teletronics.storage_app.exception.TagSizeExceedLimitException;
import io.teletronics.storage_app.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class FileUploadValidator {
    private static final int MAX_TAG_COUNT = 5;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final TagService tagService;

    @Autowired
    public FileUploadValidator(TagService tagService) {
        this.tagService = tagService;
    }

    public Mono<Void> validate(String filename, String visibility, String tags) {
        return Mono.fromFuture(this.validateTags(tags))
                .then(Mono.fromFuture(this.validateVisibility(visibility)))
                .then();
    }

    private CompletableFuture<?> validateTags(String tags) {
        Set<String> tagsSet = Arrays.stream(tags.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        List<Mono<Boolean>> tagExistenceChecks = tagsSet.stream()
                .map(tag -> tagService.tagExists(tag).map(exists -> {
                    if (!exists) {
                        throw new TagNotExistException();
                    }
                    return true;
                }))
                .collect(Collectors.toList());

        return Mono.when(tagExistenceChecks)
                .toFuture()
                .thenApply(result -> {
                    if (tagsSet.size() > MAX_TAG_COUNT) {
                        throw new TagSizeExceedLimitException();
                    }
                    return null; // Return null or any other completion signal
                })
                .toCompletableFuture();
    }

    private CompletableFuture<?> validateVisibility(String visibility) {
        return CompletableFuture.runAsync(() -> {
            FileVisibility.checkValidVisibility(visibility);
        }, executor);
    }
}
