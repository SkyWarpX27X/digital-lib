package ua.fictionallibrary.digital_lib.bookorder;

import jakarta.persistence.*;
import ua.fictionallibrary.digital_lib.physicalbook.PhysicalBook;
import ua.fictionallibrary.digital_lib.user.User;

import java.util.UUID;

@Entity
@Table(name = "book_orders")
public class BookOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String emailForDeliver;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private PhysicalBook book;
    @Column
    private boolean isOpen;

    public BookOrder() {}

    public BookOrder(User user, String emailForDeliver, PhysicalBook book, boolean isOpen) {
        this.user = user;
        this.emailForDeliver = emailForDeliver;
        this.book = book;
        this.isOpen = isOpen;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getEmailForDeliver() {
        return emailForDeliver;
    }

    public void setEmailForDeliver(String emailForDeliver) {
        this.emailForDeliver = emailForDeliver;
    }

    public PhysicalBook getBook() {
        return book;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
