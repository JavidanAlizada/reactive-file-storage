package io.teletronics.storage_app.controller;

import io.teletronics.storage_app.dto.request.FileRenameRequest;
import io.teletronics.storage_app.dto.response.FilesResponse;
import io.teletronics.storage_app.dto.response.UploadedFileResponse;
import io.teletronics.storage_app.service.FileService;
import io.teletronics.storage_app.validator.FileUploadValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RequestMapping("/api/files")
@RestController
public class FileController {

    private final FileService fileService;
    private final FileUploadValidator fileUploadValidator;

    @Autowired
    public FileController(FileService fileService, FileUploadValidator fileUploadValidator) {
        this.fileService = fileService;
        this.fileUploadValidator = fileUploadValidator;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<UploadedFileResponse>> uploadFile(@RequestPart("file") Mono<Part> file,
                                                                 @RequestPart("filename") String filename,
                                                                 @RequestPart("visibility") String visibility,
                                                                 @RequestPart("tags") String tags,
                                                                 @RequestHeader("Content-Length") long contentLength,
                                                                 ServerWebExchange exchange) {
        return fileUploadValidator.validate(filename, visibility, tags)
                .then(file.flatMap(f -> {
                    Set<String> tagsSet = Arrays.stream(tags.split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet());
                    return fileService.uploadFile(f, filename, visibility, tagsSet, contentLength, exchange);
                }))
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{fileId}/rename")
    public Mono<ResponseEntity<Void>> renameFile(@PathVariable String fileId,
                                                 @RequestBody FileRenameRequest fileRenameRequest,
                                                 ServerWebExchange exchange) {
        if (StringUtils.isBlank(fileRenameRequest.getNewFilename())) {
            return Mono.error(new IllegalArgumentException("Updated file name cannot be null"));
        }

        return fileService.renameFile(fileId, fileRenameRequest.getNewFilename(), exchange)
                .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build()));
    }

    @GetMapping("/list")
    public Flux<FilesResponse> listFiles(
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "sortBy", defaultValue = "FILENAME") String sortBy,
            @RequestParam(defaultValue = "true") boolean isAsc,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            ServerWebExchange exchange) {
        return fileService.listFiles(visibility, tag, sortBy, isAsc, page, pageSize, exchange);
    }

    @DeleteMapping("/{fileId}")
    public Mono<ResponseEntity<Void>> deleteFile(@PathVariable String fileId, ServerWebExchange exchange) {
        return fileService.deleteFile(fileId, exchange)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/download/by-link/{fileContentId}")
    public Mono<ResponseEntity<Resource>> downloadFileByFileContentId(@PathVariable String fileContentId, ServerWebExchange exchange) {
        return fileService.getFileByFileContentId(fileContentId, exchange)
                .map(filePath -> {
                    Resource resource = new FileSystemResource(filePath);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentDispositionFormData("attachment", filePath.getFileName().toString());
                    return new ResponseEntity<>(resource, headers, HttpStatus.OK);
                });
    }
}
