package ua.fictionallibrary.digital_lib.bookorder.model.dto;

import ua.fictionallibrary.digital_lib.bookorder.model.BookOrderEntity;

import java.util.UUID;

public record BookOrderResponse(
        UUID id,
        UUID creatorId,
        String emailForDelivery,
        UUID book,
        boolean isOpen
) {
    public BookOrderResponse(BookOrderEntity book){
        this(book.id(), book.creatorId(), book.emailForDelivery(), book.book(), book.isOpen());
    }
}
