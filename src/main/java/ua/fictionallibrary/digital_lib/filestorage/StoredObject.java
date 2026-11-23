package ua.fictionallibrary.digital_lib.filestorage;

import java.time.Instant;

public record StoredObject(String key, Instant lastModified) {
}
