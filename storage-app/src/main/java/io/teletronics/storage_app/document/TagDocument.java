package io.teletronics.storage_app.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TagDocument {
    @Id
    private String id;
    private String tag;

    public TagDocument(String tag) {
        this.tag = tag;
    }
}
