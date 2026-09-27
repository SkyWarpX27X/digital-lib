package ua.fictionallibrary.digital_lib.physicalbook.implementations;

import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.exception.IllegalResourceTypeException;
import ua.fictionallibrary.digital_lib.physicalbook.ResourceTypeValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.model.PhysicalBook;

import java.time.Year;

@Component
public class OldBookValidationStrategy implements ResourceTypeValidationStrategy {
    @Override
    public String getResourceType() {
        return "Стародрук";
    }

    @Override
    public void validateResourceType(PhysicalBook book) {
        if (book.getPublishingYear().isAfter(Year.of(1830)))
            throw new IllegalResourceTypeException("Book published after 1830 cannot be an early printed book. This books year: " + book.getPublishingYear());
    }
}