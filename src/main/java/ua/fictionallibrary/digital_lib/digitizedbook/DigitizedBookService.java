package ua.fictionallibrary.digital_lib.digitizedbook;

import org.springframework.web.multipart.MultipartFile;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.StoredFile;

import java.util.List;
import java.util.UUID;

public interface DigitizedBookService {
    DigitizedBookResponse getDigitizedBook(UUID id);
    List<DigitizedBookResponse> getAllDigitizedBooks();
    List<DigitizedBookResponse> findDigitizedBooksByName(String name);
    DigitizedBookResponse addDigitizedBook(DigitizedBookRequest request, UUID physicalBookId, MultipartFile pdf,
                                           MultipartFile cover);
    DigitizedBookResponse updateDigitizedBook(UUID id, DigitizedBookRequest request);
    void deleteDigitizedBook(UUID id);
    StoredFile getPdf(UUID id);
    StoredFile getCover(UUID id);
    boolean exists(UUID id);
}
