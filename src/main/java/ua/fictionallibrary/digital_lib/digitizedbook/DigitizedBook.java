package ua.fictionallibrary.digital_lib.digitizedbook;

import jakarta.persistence.*;
import ua.fictionallibrary.digital_lib.bookmark.Bookmark;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class DigitizedBook {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private List<String> authors;
    private String description;
    private String topic;
    private Year publishingYear;
    private String language;
    private String resourceType;
    private boolean isCopyrighted;
    private String coverUrl;
    private String fileUrl;

    protected DigitizedBook() {}

    public DigitizedBook(String name, List<String> authors, String description, String topic, Year publishingYear, String language, String resourceType, boolean isCopyrighted, String coverUrl, String fileUrl) {
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

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
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
