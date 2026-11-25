package ua.fictionallibrary.digital_lib.digitizedbook;

import org.springframework.web.multipart.MultipartFile;
import ua.fictionallibrary.digital_lib.common.BookFilter;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.MissingStorageFile;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.StoredFile;

import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DigitizedBookService {
    DigitizedBookResponse getDigitizedBook(UUID id, boolean canReadCopyrighted);
    List<DigitizedBookResponse> getAllDigitizedBooks(BookFilter filter, boolean canReadCopyrighted);
    List<DigitizedBookResponse> getDigitizedBooks(Collection<UUID> ids, boolean canReadCopyrighted);
    DigitizedBookResponse addDigitizedBook(DigitizedBookRequest request, UUID physicalBookId, MultipartFile pdf,
                                           MultipartFile cover);
    DigitizedBookResponse updateDigitizedBook(UUID id, DigitizedBookRequest request);
    void deleteDigitizedBook(UUID id);
    StoredFile getPdf(UUID id);
    StoredFile getCover(UUID id);
    URL getPdfDownloadUrl(UUID id, Duration ttl);
    boolean exists(UUID id);
    void cleanupOrphanedFiles(Duration minAge);
    List<MissingStorageFile> checkStorageIntegrity();
}
