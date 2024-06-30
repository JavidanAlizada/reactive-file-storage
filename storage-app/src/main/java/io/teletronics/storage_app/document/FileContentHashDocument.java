package io.teletronics.storage_app.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "file_content_hashes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileContentHashDocument {
    @Id
    private String id;
    private String fileHash;
}
