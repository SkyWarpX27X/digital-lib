package ua.fictionallibrary.digital_lib.digitizedbook.dto;

import java.io.InputStream;

public record StoredFile(InputStream data, String contentType) {
}