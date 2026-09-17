package ua.fictionallibrary.digital_lib.common.strategies;

import ua.fictionallibrary.digital_lib.model.entity.PhysicalBookEntity;

public interface ResourceTypeValidationStrategy {
    String getResourceType();
    void validateResourceType(PhysicalBookEntity book);
}
