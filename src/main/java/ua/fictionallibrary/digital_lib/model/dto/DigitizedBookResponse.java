package ua.fictionallibrary.digital_lib.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ua.fictionallibrary.digital_lib.model.entity.DigitizedBookEntity;

import java.time.Year;
import java.util.List;
import java.util.UUID;

public record DigitizedBookResponse(
        UUID id,
        String name,
        List<String> authors,
        String description,
        String topic,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
        Year publishingYear,
        String language,
        String resourceType,
        boolean isCopyrighted,
        String coverUrl,
        String fileUrl
) {
    public DigitizedBookResponse(DigitizedBookEntity book){
        this(book.id(), book.name(), book.authors(), book.description(), book.topic(),
                book.publishingYear(), book.language(), book.resourceType(), book.isCopyrighted(),
                book.coverUrl(), book.fileUrl());
    }
    public DigitizedBookResponse(DigitizedBookEntity book, UUID assignedId){
        this(assignedId, book.name(), book.authors(), book.description(), book.topic(),
                book.publishingYear(), book.language(), book.resourceType(), book.isCopyrighted(),
                book.coverUrl(), book.fileUrl());
    }
    public DigitizedBookResponse {
        authors = (authors != null) ? List.copyOf(authors) : List.of();
    }
}
