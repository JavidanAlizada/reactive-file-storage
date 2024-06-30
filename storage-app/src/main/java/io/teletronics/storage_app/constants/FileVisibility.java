package io.teletronics.storage_app.constants;

import lombok.Getter;

@Getter
public enum FileVisibility {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE");

    private final String value;

    FileVisibility(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "FileVisibility{" +
                "value='" + value + '\'' +
                '}';
    }

    public static void checkValidVisibility(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid VISIBILITY");
        }
        for (FileVisibility fileVisibility : FileVisibility.values()) {
            if (fileVisibility.getValue().equalsIgnoreCase(value.toUpperCase())) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid VISIBILITY");
    }
}
