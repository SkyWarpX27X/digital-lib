package ua.fictionallibrary.digital_lib.bookorder.model;

import ua.fictionallibrary.digital_lib.bookorder.model.dto.BookOrderRequest;

import java.util.UUID;

public record BookOrderEntity(
        UUID id,
        UUID creatorId,
        String emailForDelivery,
        UUID book,
        boolean isOpen
) {
    public BookOrderEntity(UUID id, BookOrderRequest request){
        this(id, request.creatorId(), request.emailForDelivery(), request.book(), request.isOpen());
    }
}
