package io.teletronics.storage_app.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teletronics.storage_app.constants.FileSortByConstant;
import io.teletronics.storage_app.constants.FileVisibility;
import io.teletronics.storage_app.document.FileMetadataDocument;
import io.teletronics.storage_app.dto.response.FileResponse;
import io.teletronics.storage_app.dto.response.FilesResponse;
import io.teletronics.storage_app.query.FileMetadataQuery;
import io.teletronics.storage_app.query.builder.FileMetadataQueryBuilder;
import io.teletronics.storage_app.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileListHandler {
    private final Helper helper;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final FileMetadataQueryBuilder fileMetadataQueryBuilder;
    private final ObjectMapper objectMapper;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public FileListHandler(Helper helper,
                           ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
                           FileMetadataQueryBuilder fileMetadataQueryBuilder,
                           ObjectMapper objectMapper,
                           ReactiveMongoTemplate reactiveMongoTemplate) {
        this.helper = helper;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.fileMetadataQueryBuilder = fileMetadataQueryBuilder;
        this.objectMapper = objectMapper;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Flux<FilesResponse> handle(String visibility, String tag, String sortBy,
                                      boolean isAsc, int page, int pageSize, ServerWebExchange exchange) {
        return helper.getUsernameFromTokenAsMono(exchange)
                .flatMapMany(username -> {
                    String redisKey = generateCacheKey(username, visibility, tag, sortBy, page, pageSize);

                    return getCachedFileMetadata(redisKey)
                            .switchIfEmpty(fetchAndCacheFiles(username, tag, sortBy, isAsc, page, pageSize, visibility, redisKey))
                            .map(this::convertToFilesResponseSet)
                            .map(FilesResponse::new);
                });

    }

    private Mono<List<FileMetadataDocument>> fetchFilesFromMongo(String username, String
            tag, String sortBy, boolean isAsc, int page, int pageSize, String visibility) {
        return fileMetadataQueryBuilder.createQueryForListingFilesWithFilters(
                        FileMetadataQuery.builder()
                                .username(username)
                                .tag(tag)
                                .sortBy(FileSortByConstant.getColumnNameBySortBy(sortBy))
                                .isAsc(isAsc)
                                .visibility(visibility)
                                .page(page).pageSize(pageSize).visibility(visibility).build()
                )
                .flatMapMany(query -> reactiveMongoTemplate.find(query, FileMetadataDocument.class))
                .collectList();
    }

    private Set<FileResponse> convertToFilesResponseSet
            (List<FileMetadataDocument> documents) {
        return documents.stream()
                .map(this::convertToFileResponse)
                .collect(Collectors.toSet());
    }

    private FileResponse convertToFileResponse(FileMetadataDocument document) {
        return new FileResponse(
                document.getId(),
                document.getFilename(),
                document.getFilePath(),
                document.getContentType(),
                document.getSize(),
                document.getTags(),
                FileVisibility.valueOf(document.getVisibility().toUpperCase(Locale.ROOT)), // Assuming FileVisibility is an enum
                document.getUploadedAt()
        );
    }

    private String generateCacheKey(String username, String visibility, String tag, String
            sortBy, int page, int pageSize) {
        return String.format("files:%s:%s:%s:%s:%d:%d", username, visibility, tag, sortBy, page, pageSize);
    }


    public Mono<Void> cacheFileMetadata(String cacheKey, List<FileMetadataDocument> fileMetadataList) {
        String metadata;
        try {
            metadata = objectMapper.writeValueAsString(fileMetadataList);
        } catch (Exception e) {
            return Mono.error(e);
        }
        return reactiveRedisTemplate.opsForValue()
                .set(cacheKey, metadata)
                .then();
    }

    private Mono<List<FileMetadataDocument>> fetchAndCacheFiles(String username, String
            tag, String sortBy, boolean isAsc, int page, int pageSize, String visibility, String redisKey) {
        return fetchFilesFromMongo(username, tag, sortBy, isAsc, page, pageSize, visibility)
                .flatMap(files -> cacheFileMetadata(redisKey, files).thenReturn(files));
    }

    public Mono<List<FileMetadataDocument>> getCachedFileMetadata(String cacheKey) {
        return reactiveRedisTemplate.opsForValue().get(cacheKey)
                .flatMap(cachedData -> {
                    try {
                        List<FileMetadataDocument> fileMetadataList = objectMapper.readValue(cachedData, new TypeReference<List<FileMetadataDocument>>() {
                        });
                        return Mono.just(fileMetadataList);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }

}
