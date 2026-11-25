package ua.fictionallibrary.digital_lib.favorite.implementations;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBookService;
import ua.fictionallibrary.digital_lib.digitizedbook.dto.DigitizedBookResponse;
import ua.fictionallibrary.digital_lib.favorite.Favorite;
import ua.fictionallibrary.digital_lib.favorite.FavoriteId;
import ua.fictionallibrary.digital_lib.favorite.FavoriteRepository;
import ua.fictionallibrary.digital_lib.favorite.FavoriteService;
import ua.fictionallibrary.digital_lib.starter.exception.DataNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository repository;
    private final DigitizedBookService digitizedBookService;

    public FavoriteServiceImpl(FavoriteRepository repository, DigitizedBookService digitizedBookService) {
        this.repository = repository;
        this.digitizedBookService = digitizedBookService;
    }

    @Override
    @Transactional
    public void addFavorite(UUID userId, UUID bookId) {
        if (!digitizedBookService.exists(bookId))
            throw new DataNotFoundException("Not found digitized book with id " + bookId);
        if (!repository.existsById(new FavoriteId(userId, bookId)))
            repository.save(new Favorite(userId, bookId));
    }

    @Override
    @Transactional
    public void removeFavorite(UUID userId, UUID bookId) {
        repository.deleteById(new FavoriteId(userId, bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DigitizedBookResponse> getFavorites(UUID userId, boolean canReadCopyrighted) {
        List<UUID> ids = repository.findByUserId(userId).stream().map(Favorite::getBookId).toList();
        return digitizedBookService.getDigitizedBooks(ids, canReadCopyrighted);
    }

    @Override
    @Transactional
    public void removeBookFromFavorites(UUID bookId) {
        repository.deleteByBookId(bookId);
    }
}
