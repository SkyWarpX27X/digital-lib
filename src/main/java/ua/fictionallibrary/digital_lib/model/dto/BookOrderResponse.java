package ua.fictionallibrary.digital_lib.model.dto;

import ua.fictionallibrary.digital_lib.model.entity.BookOrderEntity;

import java.util.UUID;

public record BookOrderResponse(
        UUID id,
        UUID creatorId,
        String emailForBook,
        UUID book,
        boolean isOpen
) {
    public BookOrderResponse(BookOrderEntity book){
        this(book.id(), book.creatorId(), book.emailForBook(), book.book(), book.isOpen());
    }
}
