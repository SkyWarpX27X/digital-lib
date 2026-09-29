package ua.fictionallibrary.digital_lib.author;

import jakarta.persistence.*;
import ua.fictionallibrary.digital_lib.bookmark.BookmarkId;
import ua.fictionallibrary.digital_lib.digitizedbook.DigitizedBook;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID authorId;
    @Column(nullable = false)
    private String name;

    protected Author() {}
    public Author(UUID id, String name) {
        this.authorId = id;
        this.name = name;
    }

    public Author(String name) {
        this.authorId = UUID.randomUUID();
        this.name = name;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

