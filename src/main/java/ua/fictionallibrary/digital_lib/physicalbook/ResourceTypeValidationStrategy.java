package ua.fictionallibrary.digital_lib.physicalbook;

public interface ResourceTypeValidationStrategy {
    String getResourceType();
    void validateResourceType(PhysicalBook book);
}
