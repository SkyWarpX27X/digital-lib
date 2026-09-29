package ua.fictionallibrary.digital_lib.physicalbook;

import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookResponse;

import java.util.List;
import java.util.UUID;

public interface PhysicalBookService {
    PhysicalBookResponse addPhysicalBook(PhysicalBookRequest request);
    PhysicalBookResponse updatePhysicalBook(UUID id, PhysicalBookRequest request);
    PhysicalBookResponse getPhysicalBook(UUID id);
    List<PhysicalBookResponse> getAllPhysicalBooks();
    void deletePhysicalBook(UUID id);
    boolean exists(UUID id);
}