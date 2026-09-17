package ua.fictionallibrary.digital_lib.common.strategies;

import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.exception.IllegalResourceTypeException;
import ua.fictionallibrary.digital_lib.model.entity.PhysicalBookEntity;

import java.time.Year;

@Component
public class ModernBookValidationStrategy implements ResourceTypeValidationStrategy {
    @Override
    public String getResourceType() {
        return "Книга";
    }

    @Override
    public void validateResourceType(PhysicalBookEntity book) {
        if (book.publishingYear().isBefore(Year.of(1830)) || book.publishingYear().equals(Year.of(1830)))
            throw new IllegalResourceTypeException("Books published before or in 1830 must be classified as early printed books. This books year: " + book.publishingYear());
    }
}