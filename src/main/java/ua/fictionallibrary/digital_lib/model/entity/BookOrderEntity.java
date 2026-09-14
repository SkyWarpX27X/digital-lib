package ua.fictionallibrary.digital_lib.model.entity;

import ua.fictionallibrary.digital_lib.model.dto.BookOrderRequest;

import java.util.UUID;

public record BookOrderEntity(
        UUID id,
        UUID creatorId,
        String emailForBook,
        UUID book,
        boolean isOpen
) {
    public BookOrderEntity(UUID id, BookOrderRequest request){
        this(id, request.creatorId(), request.emailForBook(), request.book(), request.isOpen());
    }
}
