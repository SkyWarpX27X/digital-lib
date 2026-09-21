package ua.fictionallibrary.digital_lib.physicalbook.model;

import ua.fictionallibrary.digital_lib.physicalbook.model.dto.PhysicalBookRequest;

import java.time.Year;
import java.util.List;
import java.util.UUID;

public record PhysicalBookEntity (
        UUID id,
        String name,
        List<String> authors,
        String description,
        String topic,
        Year publishingYear,
        String language,
        String resourceType
) {
    public PhysicalBookEntity(UUID id, PhysicalBookRequest request){
        this(id, request.name(), request.authors(), request.description(), request.topic(),
                request.publishingYear(), request.language(), request.resourceType());
    }
}