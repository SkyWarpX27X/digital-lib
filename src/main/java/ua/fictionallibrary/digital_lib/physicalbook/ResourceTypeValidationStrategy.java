package ua.fictionallibrary.digital_lib.physicalbook;

import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBookEntity;

public interface ResourceTypeValidationStrategy {
    String getResourceType();
    void validateResourceType(PhysicalBookEntity book);
}
