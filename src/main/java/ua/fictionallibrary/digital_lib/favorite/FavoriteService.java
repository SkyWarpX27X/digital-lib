package ua.fictionallibrary.digital_lib.favorite;

import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;

import java.util.List;
import java.util.UUID;

public interface FavoriteService {
    void addFavorite(UUID userId, UUID bookId);
    void removeFavorite(UUID userId, UUID bookId);
    List<DigitizedBookResponse> getFavorites(UUID userId, boolean canReadCopyrighted);
    void removeBookFromFavorites(UUID bookId);
}
