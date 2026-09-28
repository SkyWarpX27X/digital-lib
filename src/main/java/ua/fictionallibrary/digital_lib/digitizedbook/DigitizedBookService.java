package ua.fictionallibrary.digital_lib.digitizedbook;

import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;

import java.util.List;
import java.util.UUID;

public interface DigitizedBookService {
    DigitizedBookResponse getDigitizedBook(UUID id);
    List<DigitizedBookResponse> getAllDigitizedBooks();
    DigitizedBookResponse addDigitizedBook(DigitizedBook book, UUID physicalBookId);
    DigitizedBookResponse updateDigitizedBook(UUID id, DigitizedBook book);
    void deleteDigitizedBook(UUID id);
    boolean exists(UUID id);
}
