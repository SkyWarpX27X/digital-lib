package ua.fictionallibrary.digital_lib.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ua.fictionallibrary.digital_lib.model.entity.LibraryCardEntity;

import java.time.Year;
import java.util.UUID;

public record LibraryCardResponse(
        UUID ownerId,
        String email,
        String postcode,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
        Year birthYear,
        String livingAddress,
        String workOrStudyAddress,
        String organisation,
        String workPosition
) {
    public LibraryCardResponse(LibraryCardEntity card){
        this(card.ownerId(), card.email(), card.postcode(), card.birthYear(), card.livingAddress(),
                card.workOrStudyAddress(), card.organisation(), card.workPosition());
    }
}
