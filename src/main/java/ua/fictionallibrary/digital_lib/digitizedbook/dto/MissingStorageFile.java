package ua.fictionallibrary.digital_lib.digitizedbook.dto;

import java.util.UUID;

public record MissingStorageFile(UUID bookId, String bookName, String key) {
}
