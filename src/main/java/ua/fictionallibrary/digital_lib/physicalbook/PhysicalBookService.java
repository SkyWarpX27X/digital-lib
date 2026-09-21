package ua.fictionallibrary.digital_lib.physicalbook;

import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookResponse;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBookEntity;

import java.util.List;
import java.util.UUID;

public interface PhysicalBookService {
    PhysicalBookResponse addPhysicalBook(PhysicalBookEntity book);
    PhysicalBookResponse updatePhysicalBook(UUID id, PhysicalBookEntity book);
    PhysicalBookResponse getPhysicalBook(UUID id);
    List<PhysicalBookResponse> getAllPhysicalBooks();
    void deletePhysicalBook(UUID id);
    boolean exists(UUID id);
}