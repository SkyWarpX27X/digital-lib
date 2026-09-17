package ua.fictionallibrary.digital_lib.common.strategies;

import org.springframework.stereotype.Component;
import ua.fictionallibrary.digital_lib.exception.IllegalResourceTypeException;
import ua.fictionallibrary.digital_lib.model.entity.PhysicalBookEntity;

import java.time.Year;

@Component
public class OldBookValidationStrategy implements ResourceTypeValidationStrategy {
    @Override
    public String getResourceType() {
        return "Стародрук";
    }

    @Override
    public void validateResourceType(PhysicalBookEntity book) {
        if (book.publishingYear().isAfter(Year.of(1830)))
            throw new IllegalResourceTypeException("Book published after 1830 cannot be an early printed book. This books year: " + book.publishingYear());
    }
}