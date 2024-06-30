package io.teletronics.storage_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFileResponse {

    private String fileId;
    private String fileDownloadLink;
    private String fileName;
}
