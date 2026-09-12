package ua.fictionallibrary.digital_lib.model.entity;

import ua.fictionallibrary.digital_lib.model.dto.PhysicalBookRequest;

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
    public PhysicalBookEntity(PhysicalBookRequest request){
        this(request.id(), request.name(), request.authors(), request.description(), request.topic(),
                request.publishingYear(), request.language(), request.resourceType());
    }
}