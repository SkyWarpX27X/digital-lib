package ua.fictionallibrary.digital_lib.physicalbook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    public PhysicalBookResponse{
        authors = (authors != null) ? List.copyOf(authors) : List.of();
    }
}
