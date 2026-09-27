package ua.fictionallibrary.digital_lib.physicalbook;

import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBook;

public interface ResourceTypeValidationStrategy {
    String getResourceType();
    void validateResourceType(PhysicalBook book);
}
