package ua.fictionallibrary.digital_lib.digitizedbook.dto;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ua.fictionallibrary.digital_lib.common.OnCreate;
import ua.fictionallibrary.digital_lib.common.OnUpdate;


public record DigitizedBookRequest(
        @Schema(description = "Назва оцифрованої книги", example = "Тигролови")
        @NotBlank(message = "Name must not be empty")
        String name,

        @Schema(description = "Список авторів оцифрованої книги", example = "Іван Багряний")
        @NotEmpty(message = "List of authors must not be empty")
        List<String> authors,

        @Schema(description = "Опис оцифрованої книги", example = "Пригодницький роман з автобіографічними елементами " +
                "Івана Багряного, написаний та виданий 1944 року як «Звіролови» у краківському журналі «Вечірня година».")
        @NotBlank(message = "Description must not be empty")
        String description,

        @Schema(description = "Тематика або жанр книги", example = "Історичний пригодницький роман")
        @NotBlank(message = "Topic must be specified")
        String topic,

        @Schema(description = "Рік першого видання книги", example = "1944")
        @NotNull(message = "Year of publishing must be specified")
        @PastOrPresent(message = "Year of publishing can not be in future")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
        Year publishingYear,

        @Schema(description = "Повна назва мови книги українською мовою", example = "Українська")
        @NotBlank(message = "Language of book must be specified")
        @Size(min = 3, max = 30, message = "Language field length must be at least 3 and maximum 30")
        String language,

        @Schema(description = "Ресурс книги, базований на віці (Стародрук або Книга)", example = "Стародрук")
        @NotBlank(message = "Type of resource must be specified")
        String resourceType,

        @Schema(description = "Чи накладається авторське право на книгу?", example = "True")
        boolean isCopyrighted,

        @Schema(description = "Посилання на зображення з обкладинкою книги", example = "https://digital-lib.com/image")
        @NotBlank(message = "Cover URL must be specified")
        String coverUrl,

        @Schema(description = "Посилання на файл з PDF книги", example = "https://digital-lib.com/file")
        @NotBlank(message = "File URL must be specified")
        String fileUrl,

        @Schema(description = "UUID фізичної книги, яка оцифрувалась", example = "01324319-0853-4021-ab5d-3cd459469af7")
        @NotNull(groups = OnCreate.class, message = "Physical book ID must not be null when creating")
        @Null(groups = OnUpdate.class, message = "Physical book ID can only be null when updating")
        UUID physicalBookId

) {
        public DigitizedBookRequest {
                authors = (authors != null) ? List.copyOf(authors) : List.of();
        }
}
