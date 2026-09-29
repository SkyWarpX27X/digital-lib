package ua.fictionallibrary.digital_lib.physicalbook.implementations;

import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.exception.IllegalResourceTypeException;
import ua.fictionallibrary.digital_lib.physicalbook.ResourceTypeValidationStrategy;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;

import java.time.Year;

@Component
public class ModernBookValidationStrategy implements ResourceTypeValidationStrategy {
    @Override
    public String getResourceType() {
        return "Книга";
    }

    @Override
    public void validateResourceType(PhysicalBook book) {
        if (book.getPublishingYear().isBefore(Year.of(1830)) || book.getPublishingYear().equals(Year.of(1830)))
            throw new IllegalResourceTypeException("Books published before or in 1830 must be classified as early printed books. This books year: " + book.getPublishingYear());
    }
}