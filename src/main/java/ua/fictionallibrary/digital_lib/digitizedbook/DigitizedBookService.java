package ua.fictionallibrary.digital_lib.digitizedbook;

import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;

import java.util.List;
import java.util.UUID;

public interface DigitizedBookService {
    DigitizedBookResponse getDigitizedBook(UUID id);
    List<DigitizedBookResponse> getAllDigitizedBooks();
    List<DigitizedBookResponse> findDigitizedBooksByName(String name);
    DigitizedBookResponse addDigitizedBook(DigitizedBookRequest request, UUID physicalBookId);
    DigitizedBookResponse updateDigitizedBook(UUID id, DigitizedBookRequest request);
    void deleteDigitizedBook(UUID id);
    boolean exists(UUID id);
}
