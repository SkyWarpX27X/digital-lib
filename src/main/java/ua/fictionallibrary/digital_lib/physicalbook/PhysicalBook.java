package ua.fictionallibrary.digital_lib.physicalbook;

import jakarta.persistence.*;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "physical_books")
public class PhysicalBook {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @ElementCollection
    private List<String> authors;
    @Column(nullable = false, length = 250)
    private String description;
    @Column(nullable = false, length = 100)
    private String topic;
    @Column(nullable = false)
    private Year publishingYear;
    @Column(nullable = false, length = 30)
    private String language;
    @Column(nullable = false, length = 100)
    private String resourceType;

    public PhysicalBook() {}

    public PhysicalBook(String name, List<String> authors, String description, String topic, Year publishingYear, String language, String resourceType) {
        this.name = name;
        this.authors = authors;
        this.description = description;
        this.topic = topic;
        this.publishingYear = publishingYear;
        this.language = language;
        this.resourceType = resourceType;
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
}
