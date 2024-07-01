package io.teletronics.storage_app.service.impl;

import io.teletronics.storage_app.document.FileMetadataDocument;
import io.teletronics.storage_app.dto.response.FilesResponse;
import io.teletronics.storage_app.dto.response.UploadedFileResponse;
import io.teletronics.storage_app.handler.FileListHandler;
import io.teletronics.storage_app.handler.FileUploadHandler;
import io.teletronics.storage_app.repository.FileContentHashRepository;
import io.teletronics.storage_app.repository.FileMetadataRepository;
import io.teletronics.storage_app.service.FileService;
import io.teletronics.storage_app.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Service
public class FileServiceImpl implements FileService {
    private final FileUploadHandler fileUploadHandler;
    private final FileListHandler fileListHandler;
    private final Helper helper;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileContentHashRepository fileContentHashRepository;
    private final String fileStoragePath;

    @Autowired
    public FileServiceImpl(FileUploadHandler fileUploadHandler,
                           FileListHandler fileListHandler,
                           Helper helper,
                           ReactiveMongoTemplate reactiveMongoTemplate,
                           FileMetadataRepository fileMetadataRepository,
                           FileContentHashRepository fileContentHashRepository, @Value("${file.storage.path}") String fileStoragePath) {
        this.fileUploadHandler = fileUploadHandler;
        this.fileListHandler = fileListHandler;
        this.helper = helper;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileContentHashRepository = fileContentHashRepository;
        this.fileStoragePath = fileStoragePath;
    }

    @Override
    public Mono<UploadedFileResponse> uploadFile(Part file, String filename, String visibility, Set<String> tags, long contentLength, ServerWebExchange exchange) {
        return this.fileUploadHandler.handle(file, filename, visibility, tags, contentLength, exchange);
    }

    private String getFilePathFully(FileMetadataDocument document, String fileName) {
        return Paths.get(fileStoragePath, document.getFileHashId(), fileName).toString();
    }

    @Override
    public Mono<Void> renameFile(String fileId, String newFilename, ServerWebExchange exchange) {
        return helper.getUsernameFromTokenAsMono(exchange)
                .flatMap(username -> this.fileMetadataRepository.findById(fileId)
                                .flatMap(fileMetadataDocument -> {
                                    if (Objects.isNull(fileMetadataDocument)) {
                                        return Mono.error(FileNotFoundException::new);
                                    }

                                    fileMetadataDocument.setFilename(newFilename);
                                    String oldFilePath = fileMetadataDocument.getFilePath();
                                    String newFilePath = getFilePathFully(fileMetadataDocument, newFilename);
                                    fileMetadataDocument.setFilePath(newFilePath);
                                    fileMetadataDocument.setModifiedAt(LocalDateTime.now());
                                    return fileMetadataRepository.save(fileMetadataDocument)
                                            .flatMap(saved -> renameFileInStorage(oldFilePath, newFilePath));
                                })
                );
    }

    private Mono<Void> renameFileInStorage(String currentFilePath, String newFilePath) {
        return Mono.fromCallable(() -> {
                    Path currentPath = Paths.get(currentFilePath);
                    Path newPath = Paths.get(newFilePath);
                    Files.move(currentPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                    return newPath.toString();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Flux<FilesResponse> listFiles(String visibility, String tag, String sortBy,
                                         boolean isAsc, int page, int pageSize, ServerWebExchange exchange) {
        return this.fileListHandler.handle(visibility, tag, sortBy, isAsc, page, pageSize, exchange);
    }


    @Override
    public Mono<Void> deleteFile(String fileId, ServerWebExchange exchange) {
        return helper.getUsernameFromTokenAsMono(exchange)
                .flatMap(username -> this.fileMetadataRepository.findByIdAndUsername(fileId, username)
                        .switchIfEmpty(Mono.error(new FileNotFoundException("File not found or access denied")))
                        .flatMap(fileMetadataDocument -> deleteFileFromStorage(fileMetadataDocument.getFilePath())
                                .then(fileMetadataRepository.delete(fileMetadataDocument))
                                .then(fileContentHashRepository.deleteById(fileMetadataDocument.getFileHashId())))
                );
    }

    private Mono<Void> deleteFileFromStorage(String filePath) {
        return Mono.fromCallable(() -> Files.deleteIfExists(Paths.get(filePath)))
                .then();
    }

    public Mono<Path> getFileByFileContentId(String fileHashId, ServerWebExchange exchange) {
        return fileMetadataRepository.findByFileHashId(fileHashId)
                .map(fileMetadata -> {
                    String filePath = fileMetadata.getFilePath();
                    return Paths.get(filePath);
                });
    }
}
