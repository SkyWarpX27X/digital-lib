package ua.fictionallibrary.digital_lib.physicalbook;

import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBookEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhysicalBookRepository {
    PhysicalBookEntity savePhysicalBook(PhysicalBookEntity physicalBookEntity);
    Optional<PhysicalBookEntity> getPhysicalBook(UUID id);
    List<PhysicalBookEntity> getAllPhysicalBooks();
    void deletePhysicalBook(UUID id);
    boolean exists(UUID id);
}
