package ua.fictionallibrary.digital_lib.digitizedbook.model.dto;

import java.time.Year;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;


public record DigitizedBookRequest(
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
        String resourceType,

        boolean isCopyrighted,

        @NotBlank(message = "Cover URL must be specified")
        String coverUrl,

        @NotBlank(message = "File URL must be specified")
        String fileUrl
) {
        public DigitizedBookRequest {
                authors = (authors != null) ? List.copyOf(authors) : List.of();
        }
}
