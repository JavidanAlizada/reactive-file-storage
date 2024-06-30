package io.teletronics.storage_app.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileMetadataQuery {
    private String username;
    private String tag;
    private String sortBy;
    private boolean isAsc;
    private int page;
    private int pageSize;
    private String visibility;

    private FileMetadataQuery(Builder builder) {
        this.username = builder.username;
        this.tag = builder.tag;
        this.sortBy = builder.sortBy;
        this.isAsc = builder.isAsc;
        this.page = builder.page;
        this.pageSize = builder.pageSize;
        this.visibility = builder.visibility;
    }

    public static class Builder {
        private String username;
        private String tag;
        private String sortBy;
        private boolean isAsc;
        private int page;
        private int pageSize;
        private String visibility;

        public Builder() {
            this.username = "";
            this.tag = "";
            this.sortBy = "";
            this.isAsc = true;
            this.page = 0;
            this.pageSize = 10;
            this.visibility = "";
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder isAsc(boolean isAsc) {
            this.isAsc = isAsc;
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public FileMetadataQuery build() {
            return new FileMetadataQuery(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
