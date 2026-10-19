package ua.fictionallibrary.digital_lib.physicalbook.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Year;

public record AddPhysicalBookForm (
        @NotBlank(message = "Ім'я має бути вказане")
        String name,
        @NotBlank(message = "Автори мають бути вказані")
        String authors,
        @NotBlank(message = "Опис має бути вказаний")
        String description,
        @NotBlank(message = "Тема має бути вказана")
        String topic,
        @NotNull(message = "Рік має бути вказаний")
        @PastOrPresent(message = "Рік публікації не може бути у майбутньому")
        Year publishingYear,
        @NotBlank(message = "Мова має бути вказана")
        @Size(min = 3, max = 30, message = "Довжина має бути від 3 до 30 символів")
        String language,
        @NotBlank(message = "Вид має бути вказано")
        String resourceType
) {}
