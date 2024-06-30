package io.teletronics.storage_app.dto.response;

import io.teletronics.storage_app.constants.FileVisibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {
    private String id;
    private String filename;
    private String filePath;
    private String contentType;
    private long size;
    private Set<String> tags;
    private FileVisibility visibility;
    private LocalDateTime uploadedAt;
}
