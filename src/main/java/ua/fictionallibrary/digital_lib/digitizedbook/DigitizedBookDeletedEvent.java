package ua.fictionallibrary.digital_lib.digitizedbook;

import java.util.UUID;

public record DigitizedBookDeletedEvent(
        UUID digitizedId
) {
}
