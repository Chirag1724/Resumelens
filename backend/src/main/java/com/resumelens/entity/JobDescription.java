package com.resumelens.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    // Comma-separated list; no separate join table needed for current scope
    @Column(columnDefinition = "TEXT")
    private String requiredSkills;

    protected JobDescription() {}

    public JobDescription(String title, String description, String requiredSkills) {
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRequiredSkills() { return requiredSkills; }
}