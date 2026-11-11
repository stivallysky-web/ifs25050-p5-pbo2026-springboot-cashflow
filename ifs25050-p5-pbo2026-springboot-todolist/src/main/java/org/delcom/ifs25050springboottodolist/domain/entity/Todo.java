package org.delcom.ifs25050springbootcashflow.domain.entity;

import java.time.LocalDateTime;

public class Todo {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private boolean completed;
    private String coverPath;
    private LocalDateTime createdAt;

    public Todo(Long id, Long userId, String title, String description,
                boolean completed, String coverPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.coverPath = coverPath;
        this.createdAt = createdAt;
    }

    public Todo(Long userId, String title, String description) {
        this(null, userId, title, description, false, null, LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}