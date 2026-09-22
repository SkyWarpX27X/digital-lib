package ua.fictionallibrary.digital_lib.digitizedbook;

import ua.fictionallibrary.digital_lib.digitizedbook.model.DigitizedBookEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DigitizedBookRepository {
    DigitizedBookEntity saveDigitizedBook(DigitizedBookEntity digitizedBookEntity);
    Optional<DigitizedBookEntity> getDigitizedBook(UUID id);
    List<DigitizedBookEntity> getAllDigitizedBooks();
    void deleteDigitizedBook(UUID id);
    boolean exists(UUID id);
}
