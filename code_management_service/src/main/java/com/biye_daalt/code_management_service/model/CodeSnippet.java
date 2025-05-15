package com.biye_daalt.code_management_service.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="code_snippets")
public class CodeSnippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Two Sum Problem"

    @Lob // For potentially large text, maps to TEXT or CLOB
    @Column(nullable = false, columnDefinition = "TEXT") // Explicitly TEXT for SQLite
    private String problemMarkdown; // Markdown text for the problem description

    @Lob
    @Column(columnDefinition = "TEXT") // User's code solution
    private String solution;

    @Column(nullable = false)
    private String language; // e.g., "java", "python", "javascript"

    @CreationTimestamp // Automatically set on creation by Hibernate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER) // EAGER or LAZY, EAGER is simpler for now
    @CollectionTable(name = "problem_comments", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "comment", columnDefinition = "TEXT")
    private List<String> comments = new ArrayList<>();

    public CodeSnippet(){}

    public CodeSnippet(String name, String problemMarkdown, String language) {
        this.name = name;
        this.problemMarkdown = problemMarkdown;
        this.language = language;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProblemMarkdown() {
        return problemMarkdown;
    }

    public void setProblemMarkdown(String problemMarkdown) {
        this.problemMarkdown = problemMarkdown;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        // Typically not set manually if using @CreationTimestamp,
        // but setter might be needed for deserialization or testing.
        this.createdAt = createdAt;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeSnippet that = (CodeSnippet) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(language, that.language); // Basic equality check
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, language); // Basic hash
    }

    @Override
    public String toString() {
        return "CodeProblem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", problemMarkdown='" + (problemMarkdown != null ? problemMarkdown.substring(0, Math.min(problemMarkdown.length(), 50)) + "..." : "null") + '\'' +
                ", solution='" + (solution != null ? solution.substring(0, Math.min(solution.length(), 50)) + "..." : "null") + '\'' +
                ", language='" + language + '\'' +
                ", createdAt=" + createdAt +
                ", commentsCount=" + (comments != null ? comments.size() : 0) +
                '}';
    }
}
