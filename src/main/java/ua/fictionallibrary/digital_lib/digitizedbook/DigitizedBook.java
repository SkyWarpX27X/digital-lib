package ua.fictionallibrary.digital_lib.digitizedbook;

import jakarta.persistence.*;
import ua.fictionallibrary.digital_lib.author.Author;

import java.time.Year;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "digitized_books")
public class DigitizedBook {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @ManyToMany
    @JoinTable(
            name = "digitized_book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors;
    @Column(nullable = false, length = 250)
    private String description;
    @Column(nullable = false, length = 100)
    private String topic;
    @Column(nullable = false)
    private Year publishingYear;
    @Column(nullable = false, length = 30)
    private String language;
    @Column(nullable = false, length = 20)
    private String resourceType;
    @Column(nullable = false)
    private boolean isCopyrighted;
    @Column
    private String coverKey;
    @Column(nullable = false)
    private String pdfKey;
    @Column
    private String coverContentType;


    protected DigitizedBook() {}

    public DigitizedBook(String name, Set<Author> authors, String description, String topic, Year publishingYear,
                         String language, String resourceType, boolean isCopyrighted) {
        this.name = name;
        this.authors = authors;
        this.description = description;
        this.topic = topic;
        this.publishingYear = publishingYear;
        this.language = language;
        this.resourceType = resourceType;
        this.isCopyrighted = isCopyrighted;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Year getPublishingYear() {
        return publishingYear;
    }

    public void setPublishingYear(Year publishingYear) {
        this.publishingYear = publishingYear;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public boolean isCopyrighted() {
        return isCopyrighted;
    }

    public void setCopyrighted(boolean copyrighted) {
        isCopyrighted = copyrighted;
    }

    public String getCoverKey() {
        return coverKey;
    }

    public void setCoverKey(String coverKey) {
        this.coverKey = coverKey;
    }

    public String getPdfKey() {
        return pdfKey;
    }

    public void setPdfKey(String pdfKey) {
        this.pdfKey = pdfKey;
    }

    public String getCoverContentType() {
        return coverContentType;
    }

    public void setCoverContentType(String coverContentType) {
        this.coverContentType = coverContentType;
    }
}
