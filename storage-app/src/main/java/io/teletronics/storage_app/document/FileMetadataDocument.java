package io.teletronics.storage_app.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Document(collection = "files")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String filename;
    @Field("file_path")
    private String filePath;
    @Field("content_type")
    private String contentType;
    private String fileHashId;
    private long size;
    private Set<String> tags;
    @Indexed
    private String visibility;
    @Indexed
    @Field("username")
    private String username;
    private Boolean deleted;
    @Indexed
    @Field("uploaded_at")
    private LocalDateTime uploadedAt;
    @Field("modified_at")
    private LocalDateTime modifiedAt;
    @Field("deletedAt")
    private LocalDateTime deletedAt;
    @Field("full_path_in_response")
    private String fullPathInResponse;

}
