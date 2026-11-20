package ua.fictionallibrary.digital_lib.common;

import io.swagger.v3.oas.annotations.Parameter;

import java.time.Year;

public record BookFilter(
        @Parameter(description = "Частина назви, без урахування регістру") String name,
        @Parameter(description = "Частина імені автора, без урахування регістру") String author,
        @Parameter(description = "Частина теми, без урахування регістру") String topic,
        @Parameter(description = "Рік публікації від (включно)", example = "1900") Year yearFrom,
        @Parameter(description = "Рік публікації до (включно)", example = "2000") Year yearTo,
        @Parameter(description = "Частина назви мови, без урахування регістру") String language,
        @Parameter(description = "Вид ресурсу, точний збіг", example = "Книга") String resourceType
) {
    public static final BookFilter NONE = new BookFilter(null, null, null, null, null, null, null);

    public BookFilter {
        name = blankToNull(name);
        author = blankToNull(author);
        topic = blankToNull(topic);
        language = blankToNull(language);
        resourceType = blankToNull(resourceType);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}