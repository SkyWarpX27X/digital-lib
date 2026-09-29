package ua.fictionallibrary.digital_lib.digitizedbook;

import jakarta.persistence.*;
import ua.fictionallibrary.digital_lib.author.Author;
import ua.fictionallibrary.digital_lib.bookmark.Bookmark;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
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
    @Column(nullable = false)
    private String coverUrl;
    @Column(nullable = false)
    private String fileUrl;


    protected DigitizedBook() {}

    public DigitizedBook(String name, Set<Author> authors, String description, String topic, Year publishingYear, String language, String resourceType, boolean isCopyrighted, String coverUrl, String fileUrl) {
        this.name = name;
        this.authors = authors;
        this.description = description;
        this.topic = topic;
        this.publishingYear = publishingYear;
        this.language = language;
        this.resourceType = resourceType;
        this.isCopyrighted = isCopyrighted;
        this.coverUrl = coverUrl;
        this.fileUrl = fileUrl;
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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
