package io.teletronics.storage_app.exception;

public enum ErrorMessages {
    USER_EXIST("error.user.exist"),
    USER_NOT_FOUND("error.user.not_found"),
    INTERNAL_ERROR("error.system.internal_error"),
    INVALID_CREDENTIALS_ERROR("error.user.invalid_credentials_error"),
    FILE_UPLOAD_ERROR("error.file.uploading_error"),
    FILE_NON_UNIQUE_ERROR("error.file.non_unique_error"),
    FILE_SAVING_ERROR("error.file.saving_error"),
    INVALID_VISIBILITY_VALUE("error.file.invalid_visibility_value_error"),
    TAG_SIZE_EXCEED_LIMIT_ERROR("error.file.tag_size_exceed_limit_error"),
    TAG_INVALID_ERROR("error.file.invalid_tag_error"),
    JWT_TOKEN_NOT_FOUND_OR_INVALID("error.user.jwt_token_not_found_or_invalid");

    private final String messageKey;

    ErrorMessages(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public static ErrorMessages fromValue(String value) {
        for (ErrorMessages errorMessages : ErrorMessages.values()) {
            if (errorMessages.getMessageKey().equals(value)) {
                return errorMessages;
            }
        }

        throw new IllegalArgumentException("Invalid error message!");
    }
}
