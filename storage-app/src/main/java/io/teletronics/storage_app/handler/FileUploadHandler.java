package io.teletronics.storage_app.handler;

import io.teletronics.storage_app.document.FileContentHashDocument;
import io.teletronics.storage_app.document.FileMetadataDocument;
import io.teletronics.storage_app.dto.response.UploadedFileResponse;
import io.teletronics.storage_app.exception.FileNonUniqueException;
import io.teletronics.storage_app.exception.FileSavingException;
import io.teletronics.storage_app.exception.FileUploadException;
import io.teletronics.storage_app.query.FileContentHashQuery;
import io.teletronics.storage_app.query.FileMetadataQuery;
import io.teletronics.storage_app.query.builder.FileContentHashQueryBuilder;
import io.teletronics.storage_app.query.builder.FileMetadataQueryBuilder;
import io.teletronics.storage_app.repository.FileContentHashRepository;
import io.teletronics.storage_app.repository.FileMetadataRepository;
import io.teletronics.storage_app.service.FileStorageService;
import io.teletronics.storage_app.util.FileUtil;
import io.teletronics.storage_app.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileUploadHandler {
    private static final String FILE_DOWNLOAD_LINK = "download/by-link/";
    private final Helper helper;
    private final FileMetadataRepository fileMetadataRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final FileMetadataQueryBuilder fileMetadataQueryBuilder;
    private final FileContentHashQueryBuilder fileContentHashQueryBuilder;
    private final FileStorageService fileStorageService;
    private final FileContentHashRepository fileContentHashRepository;

    @Autowired
    public FileUploadHandler(Helper helper,
                             FileMetadataRepository fileMetadataRepository,
                             ReactiveMongoTemplate reactiveMongoTemplate,
                             FileMetadataQueryBuilder fileMetadataQueryBuilder,
                             FileContentHashQueryBuilder fileContentHashQueryBuilder,
                             FileStorageService fileStorageService,
                             FileContentHashRepository fileContentHashRepository) {
        this.helper = helper;
        this.fileMetadataRepository = fileMetadataRepository;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.fileMetadataQueryBuilder = fileMetadataQueryBuilder;
        this.fileContentHashQueryBuilder = fileContentHashQueryBuilder;
        this.fileStorageService = fileStorageService;
        this.fileContentHashRepository = fileContentHashRepository;
    }

    public Mono<UploadedFileResponse> handle(Part file, String filename, String visibility, Set<String> tags,  long contentLength, ServerWebExchange exchange) {
        return helper.getUsernameFromTokenAsMono(exchange)
                .flatMap(username -> checkFileContentAndNameUniquenessAcrossFilesOfCurrentUser(file, filename, username)
                        .switchIfEmpty(Mono.error(FileNonUniqueException::new))
                        .flatMap(savedFileContentHashDocId -> uploadFileToFileStorage(file, username, savedFileContentHashDocId)
                                .flatMap(filePath -> insertFileMetadataToMongoDocument(filePath, filename, visibility, tags, file, savedFileContentHashDocId, contentLength, exchange)
                                        .doOnNext(fileMetadata -> {
                                            if (file.headers().getContentType().getType() == null || file.headers().getContentType().getType().isEmpty()) {
                                                identifyFileTypeAndUpdateDocument(fileMetadata);
                                            }
                                        })
                                )
                                .flatMap(fileMetadata -> {
                                    String fileId = fileMetadata.getId();
                                    String downloadLink = getFileDownloadLink(exchange, fileMetadata.getFullPathInResponse(), savedFileContentHashDocId);
                                    return Mono.just(new UploadedFileResponse(fileId, downloadLink, fileMetadata.getFilename()));
                                }))
                )
                .onErrorMap(throwable -> {
                    if (throwable instanceof IOException) {
                        return new FileUploadException();
                    }
                    return throwable;
                });
    }

    private String getFileDownloadLink(ServerWebExchange exchange, String filePathInResponse, String savedFileContentHashDocId) {
        String downloadEndpoint = FILE_DOWNLOAD_LINK + savedFileContentHashDocId;
        String downloadLink = exchange.getRequest().getURI().resolve(downloadEndpoint)
                .toString();

        URI uri = UriComponentsBuilder.fromUriString(downloadLink)
                .build()
                .toUri();
        return uri.toString();
    }

    public void identifyFileTypeAndUpdateDocument(FileMetadataDocument fileMetadata) {
        String filePath = fileMetadata.getFilePath();
        Path path = Paths.get(filePath);

        try {
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            final String finalContentType = contentType;
            fileMetadataRepository.findById(fileMetadata.getId())
                    .doOnNext(doc -> {
                        doc.setContentType(finalContentType);
                        fileMetadataRepository.save(doc).subscribe();
                    })
                    .onErrorResume(e -> {
                        throw new RuntimeException("Error identifying content type: " + e.getMessage());
                    })
                    .subscribe();
        } catch (IOException e) {
            throw new RuntimeException("Error identifying content type: " + e.getMessage());
        }
    }

    private FileMetadataQuery getFileMetadataQueryBuilder(String username) {
        return FileMetadataQuery.builder().username(username).build();
    }

    private FileContentHashQuery getFileContentHashQuery(String hash, Set<String> ids) {
        return FileContentHashQuery.builder().fileContentHash(hash)
                .fileContentHashIds(ids)
                .build();
    }

    private Set<String> getFileHashIds(List<FileMetadataDocument> fileMetadataDocuments) {
        return fileMetadataDocuments.stream()
                .map(FileMetadataDocument::getFileHashId)
                .collect(Collectors.toSet());
    }

    private Mono<String> saveAndGetId(String fileHash) {
        return fileContentHashRepository.save(new FileContentHashDocument(null, fileHash))
                .map(FileContentHashDocument::getId);
    }


    public Mono<String> checkFileContentAndNameUniquenessAcrossFilesOfCurrentUser(Part file, String filename, String usernameFromToken) {
        Query searchingByUsernameQuery = fileMetadataQueryBuilder.createQueryForSearchingByUsername(getFileMetadataQueryBuilder(usernameFromToken));

        return reactiveMongoTemplate.find(searchingByUsernameQuery, FileMetadataDocument.class)
                .collectList()
                .flatMap(fileMetadataDocuments -> getFileContentHash(file)
                        .flatMap(fileHash -> {
                            boolean filenameExists = fileMetadataDocuments.stream()
                                    .anyMatch(fileMetadataDocument -> fileMetadataDocument.getFilename().equals(filename));

                            if (filenameExists) {
                                return Mono.error(FileNonUniqueException::new);
                            }

                            Query contentHashQuery = fileContentHashQueryBuilder.createQuery(getFileContentHashQuery(fileHash, getFileHashIds(fileMetadataDocuments)));
                            return reactiveMongoTemplate.exists(contentHashQuery, FileContentHashDocument.class)
                                    .flatMap(exists -> {
                                        if (exists) {
                                            return Mono.error(FileNonUniqueException::new);
                                        } else {
                                            return saveAndGetId(fileHash);
                                        }
                                    });
                        }))
                .onErrorMap(throwable -> {
                    if (throwable instanceof IOException) {
                        return new RuntimeException("Error reading file content: " + throwable.getMessage());
                    }
                    return throwable;
                });
    }

    private Mono<String> getFileContentHash(Part file) {
        return DataBufferUtils.join(file.content())
                .map(dataBuffer -> {
                    byte[] fileContent = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(fileContent);
                    DataBufferUtils.release(dataBuffer);
                    try {
                        return FileUtil.calculateFileHash(fileContent);
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException("Error calculating file hash: " + e.getMessage());
                    }
                })
                .onErrorMap(e -> new RuntimeException("Error reading file content: " + e.getMessage()));
    }


    private Mono<String> uploadFileToFileStorage(Part file, String username, String savedFileContentHashDocId) {
        return fileStorageService.storeFile(file, username, savedFileContentHashDocId).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<FileMetadataDocument> insertFileMetadataToMongoDocument(String filePath, String filename, String visibility,
                                                                         Set<String> tags, Part file,
                                                                         String savedFileContentHashDocId,
                                                                         long contentLength,
                                                                         ServerWebExchange exchange) {
        FileMetadataDocument fileMetadata = new FileMetadataDocument();
        fileMetadata.setFilePath(filePath);
        fileMetadata.setFilename(filename);
        fileMetadata.setVisibility(visibility);
        fileMetadata.setContentType(file.headers().getContentType().getType());
        fileMetadata.setTags(tags);
        fileMetadata.setFileHashId(savedFileContentHashDocId);
        fileMetadata.setUsername(helper.getUsernameFromToken(exchange));
        fileMetadata.setUploadedAt(LocalDateTime.now());
        fileMetadata.setModifiedAt(LocalDateTime.now());
        fileMetadata.setSize(contentLength);
        return fileMetadataRepository.save(fileMetadata)
                .onErrorMap(throwable -> new FileSavingException());
    }

}
