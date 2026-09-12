package ua.fictionallibrary.digital_lib.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ua.fictionallibrary.digital_lib.model.entity.PhysicalBookEntity;

import java.time.Year;
import java.util.List;
import java.util.UUID;

public record PhysicalBookResponse(
        UUID id,
        String name,
        List<String> authors,
        String description,
        String topic,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
        Year publishingYear,
        String language,
        String resourceType
) {
    public PhysicalBookResponse(PhysicalBookEntity book){
        this(book.id(), book.name(), book.authors(), book.description(), book.topic(),
                book.publishingYear(), book.language(), book.resourceType());
    }
    public PhysicalBookResponse(PhysicalBookEntity book, UUID assignedId){
        this(assignedId, book.name(), book.authors(), book.description(), book.topic(),
                book.publishingYear(), book.language(), book.resourceType());
    }
}
