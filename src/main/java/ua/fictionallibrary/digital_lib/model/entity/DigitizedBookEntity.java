package ua.fictionallibrary.digital_lib.model.entity;

import ua.fictionallibrary.digital_lib.model.dto.DigitizedBookRequest;
import ua.fictionallibrary.digital_lib.model.dto.PhysicalBookRequest;

import java.time.Year;
import java.util.List;
import java.util.UUID;

public record DigitizedBookEntity (
        UUID id,
        String name,
        List<String> authors,
        String description,
        String topic,
        Year publishingYear,
        String language,
        String resourceType,
        boolean isCopyrighted,
        String coverUrl,
        String fileUrl

) {
    public DigitizedBookEntity(DigitizedBookRequest request){
        this(request.id(), request.name(), request.authors(), request.description(), request.topic(),
                request.publishingYear(), request.language(), request.resourceType(), request.isCopyrighted(),
                request.coverUrl(), request.fileUrl());
    }
}
