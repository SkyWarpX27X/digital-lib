package ua.fictionallibrary.digital_lib.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import ua.fictionallibrary.digital_lib.common.validation.OnCreate;
import ua.fictionallibrary.digital_lib.common.validation.OnUpdate;

import java.time.Year;
import java.util.List;
import java.util.UUID;

public record PhysicalBookRequest(
        @Null(groups = OnCreate.class, message = "ID must be null when creating")
        @NotNull(groups = OnUpdate.class, message = "ID must be specified when updating")
        UUID id,
        @NotBlank(message = "Name must not be empty")
        String name,
        @NotEmpty(message = "List of authors must not be empty")
        List<String> authors,
        @NotBlank(message = "Description must not be empty")
        String description,
        @NotBlank(message = "Topic must be specified")
        String topic,
        @NotNull(message = "Year of publishing must be specified")
        @PastOrPresent(message = "Year of publishing can not be in future")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
        Year publishingYear,
        @NotBlank(message = "Language of book must be specified")
        @Size(min = 3, max = 30, message = "Language field length must be at least 3 and maximum 30")
        String language,
        @NotBlank(message = "Type of resource must be specified")
        String resourceType
) {
}