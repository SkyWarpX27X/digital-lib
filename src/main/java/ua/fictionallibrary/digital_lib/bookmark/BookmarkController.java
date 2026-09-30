package ua.fictionallibrary.digital_lib.bookmark;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkRequest;
import ua.fictionallibrary.digital_lib.bookmark.dto.BookmarkResponse;
import ua.fictionallibrary.digital_lib.common.OnCreate;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookmark")
public class BookmarkController {
    private final BookmarkService service;

    public BookmarkController(BookmarkService service){
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BookmarkResponse> createBookmark(@Validated(OnCreate.class) @RequestBody BookmarkRequest request){
        BookmarkResponse response = service.addBookmark(new Bookmark(request.userId(), request.bookId(), request.pageNumber()));
        UUID userId = request.userId();
        UUID bookId = request.bookId();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{userId}/{bookId}").buildAndExpand(userId, bookId).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{userId}/{bookId}")
    public ResponseEntity<BookmarkResponse> getBookmark(@PathVariable UUID userId, @PathVariable UUID bookId){
        return ResponseEntity.ok(service.getBookmark(userId, bookId));
    }

    @PatchMapping("/{userId}/{bookId}")
    public ResponseEntity<BookmarkResponse> updatePageNumber(@Validated(OnCreate.class) @PathVariable UUID userId, @PathVariable UUID bookId,
                                                             @RequestBody UpdatePageNumberCommand command){
        BookmarkResponse response = service.updatePageNumber(userId, bookId, command);
        return ResponseEntity.ok(response);
    }
}
