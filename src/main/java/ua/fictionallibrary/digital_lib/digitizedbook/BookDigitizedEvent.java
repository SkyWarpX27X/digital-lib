package ua.fictionallibrary.digital_lib.digitizedbook;

import java.util.UUID;

public record BookDigitizedEvent(
    UUID digitizedId,
    UUID physicalId
) {
}
