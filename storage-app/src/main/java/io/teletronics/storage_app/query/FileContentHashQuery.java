package io.teletronics.storage_app.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileContentHashQuery {

    private String username;
    private String fileContentHash;
    private Set<String> fileContentHashIds;


    private FileContentHashQuery(FileContentHashQuery.Builder builder) {
        this.username = builder.username;
        this.fileContentHash = builder.fileContentHash;
        this.fileContentHashIds = builder.fileContentHashIds;
    }

    public static class Builder {
        private String username;
        private String fileContentHash;
        private Set<String> fileContentHashIds;

        public Builder() {
            this.username = "";
            this.fileContentHash = "";
            this.fileContentHashIds = new HashSet<>();
        }

        public FileContentHashQuery.Builder username(String username) {
            this.username = username;
            return this;
        }

        public FileContentHashQuery.Builder fileContentHash(String fileContentHash) {
            this.fileContentHash = fileContentHash;
            return this;
        }

        public FileContentHashQuery.Builder fileContentHashIds(Set<String> fileContentHashIds) {
            this.fileContentHashIds = fileContentHashIds;
            return this;
        }

        public FileContentHashQuery build() {
            return new FileContentHashQuery(this);
        }
    }

    public static FileContentHashQuery.Builder builder() {
        return new FileContentHashQuery.Builder();
    }
}
