package ua.fictionallibrary.digital_lib.physicalbook;

import java.util.UUID;

public record PhysicalBookAddedEvent(
        UUID id,
        String bookName,
        String resourceType
) {
}
