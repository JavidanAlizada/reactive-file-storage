package io.teletronics.storage_app.service;

import io.teletronics.storage_app.dto.response.FilesResponse;
import io.teletronics.storage_app.dto.response.UploadedFileResponse;
import org.springframework.http.codec.multipart.Part;
import org.springframework.http.codec.multipart.PartEvent;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public interface FileService {
    Mono<UploadedFileResponse> uploadFile(Part file, String filename, String visibility, Set<String> tags, ServerWebExchange exchange);

    Mono<Void> renameFile(String fileId, String newFilename, ServerWebExchange exchange);

    Flux<FilesResponse> listFiles(String visibility, String tag, String sortBy, boolean isAsc, int page, int pageSize, ServerWebExchange exchange);

    Mono<Void> deleteFile(String fileId, ServerWebExchange exchange);

    Mono<Path> getFileByFileContentId(String fileContentId, ServerWebExchange exchange);
}
