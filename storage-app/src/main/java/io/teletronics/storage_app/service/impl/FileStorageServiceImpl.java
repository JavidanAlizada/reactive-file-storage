package io.teletronics.storage_app.service.impl;

import io.teletronics.storage_app.exception.FileUploadException;
import io.teletronics.storage_app.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private final String fileStoragePath;

    public FileStorageServiceImpl(@Value("${file.storage.path}") String fileStoragePath) {
        this.fileStoragePath = fileStoragePath;
    }

    @Override
    public Mono<String> storeFile(Part file, String username, String savedFileContentHashDocId) {
        String storageDirectory = fileStoragePath + "/" + savedFileContentHashDocId;
        String filename = StringUtils.cleanPath(file.headers().getContentDisposition().getFilename());

        return Mono.fromCallable(() -> {
                    Path directory = Paths.get(storageDirectory);
                    Files.createDirectories(directory);
                    return directory;
                })
                .flatMap(directory -> {
                    Path filePath = directory.resolve(filename);
                    return DataBufferUtils.write(file.content(), filePath)
                            .then(Mono.just(filePath.toString()));
                })
                .onErrorMap(IOException.class, e -> new FileUploadException())
                .subscribeOn(Schedulers.boundedElastic());
    }
}
