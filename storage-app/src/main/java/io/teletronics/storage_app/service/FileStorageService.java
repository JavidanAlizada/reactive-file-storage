package io.teletronics.storage_app.service;

import org.springframework.http.codec.multipart.Part;
import reactor.core.publisher.Mono;

public interface FileStorageService {


    Mono<String> storeFile(Part file, String username, String savedFileContentHashDocId);
}
