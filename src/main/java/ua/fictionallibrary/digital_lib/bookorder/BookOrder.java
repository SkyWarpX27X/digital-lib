package ua.fictionallibrary.digital_lib.bookorder;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "book_orders")
public class BookOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID creatorId;
    @Column(nullable = false)
    private String emailForDeliver;
    @Column(nullable = false, unique = true)
    private UUID book;
    @Column
    private boolean isOpen;

    public BookOrder() {}

    public BookOrder(UUID creatorId, String emailForDeliver, UUID book, boolean isOpen) {
        this.creatorId = creatorId;
        this.emailForDeliver = emailForDeliver;
        this.book = book;
        this.isOpen = isOpen;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getEmailForDeliver() {
        return emailForDeliver;
    }

    public void setEmailForDeliver(String emailForDeliver) {
        this.emailForDeliver = emailForDeliver;
    }

    public UUID getBook() {
        return book;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
