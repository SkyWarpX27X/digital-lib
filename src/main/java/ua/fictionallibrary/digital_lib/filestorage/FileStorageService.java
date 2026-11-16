package ua.fictionallibrary.digital_lib.filestorage;

import java.io.InputStream;

public interface FileStorageService {
    void store(String key, InputStream data, long size, String contentType);
    InputStream load(String key);
    void delete(String key);
}
