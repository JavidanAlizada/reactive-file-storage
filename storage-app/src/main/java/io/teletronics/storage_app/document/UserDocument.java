package io.teletronics.storage_app.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String password;
}
