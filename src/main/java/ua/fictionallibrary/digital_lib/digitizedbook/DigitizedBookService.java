package ua.fictionallibrary.digital_lib.digitizedbook;

import ua.fictionallibrary.digital_lib.digitizedbook.model.DigitizedBookEntity;
import ua.fictionallibrary.digital_lib.digitizedbook.model.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookResponse;

import java.util.List;
import java.util.UUID;

public interface DigitizedBookService {
    DigitizedBookResponse getDigitizedBook(UUID id);
    List<DigitizedBookResponse> getAllDigitizedBooks();
    DigitizedBookResponse addDigitizedBook(DigitizedBookEntity book, UUID physicalBookId);
    DigitizedBookResponse updateDigitizedBook(UUID id, DigitizedBookEntity book);
    void deleteDigitizedBook(UUID id);
    boolean exists(UUID id);
}
