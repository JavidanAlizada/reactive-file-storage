package io.teletronics.storage_app.constants;

public enum FileSortByConstant {
    FILENAME("FILENAME", "filename"),
    UPLOAD_DATE("UPLOAD_DATE", "uploaded_at"),
    TAG("TAG", "tags"),
    CONTENT_TYPE("CONTENT_TYPE", "content_type"),
    FILE_SIZE("FILE_SIZE", "size");

    private final String sortBy;
    private final String columnName;

    FileSortByConstant(String sortBy, String columnName) {
        this.sortBy = sortBy;
        this.columnName = columnName;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getColumnName() {
        return columnName;
    }

    public static String getColumnNameBySortBy(String sortBy) {
        for (FileSortByConstant constant : FileSortByConstant.values()) {
            if (constant.getSortBy().equalsIgnoreCase(sortBy)) {
                return constant.getColumnName();
            }
        }
        throw new IllegalArgumentException("Column not found by sort by field !");
    }
}
