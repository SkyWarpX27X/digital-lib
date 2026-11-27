package ua.fictionallibrary.digital_lib.physicalbook;

import ua.fictionallibrary.digital_lib.common.BookFilter;
import ua.fictionallibrary.digital_lib.common.BookFilterOptions;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookRequest;
import ua.fictionallibrary.digital_lib.physicalbook.dto.PhysicalBookResponse;

import java.util.List;
import java.util.UUID;

public interface PhysicalBookService {
    PhysicalBookResponse addPhysicalBook(PhysicalBookRequest request);
    PhysicalBookResponse updatePhysicalBook(UUID id, PhysicalBookRequest request);
    PhysicalBookResponse getPhysicalBook(UUID id);
    List<PhysicalBookResponse> getAllPhysicalBooks(BookFilter filter);
    void deletePhysicalBook(UUID id);
    BookFilterOptions getFilterOptions();
    boolean exists(UUID id);
}