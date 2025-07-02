package com.codeguard.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "code_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmission {

    @Id
    private String id;

    @NotBlank
    private String fileName;

    @NotBlank
    private String code;

    @NotNull
    private Language language;

    private String userId;

    private String output;

    private ExecutionStatus status = ExecutionStatus.PENDING;

    private String errorMessage;

    private Long executionTime; // in milliseconds

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructor used when creating new submissions
    public CodeSubmission(String fileName, String code, Language language) {
        this.fileName = fileName;
        this.code = code;
        this.language = language;
        this.status = ExecutionStatus.PENDING;
    }

    @Getter
    public enum Language {
        JAVA("java", ".java", "javac", "java"),
        CPP("cpp", ".cpp", "g++", "./a.out"),
        JAVASCRIPT("javascript", ".js", "node", "node");

        private final String name;
        private final String extension;
        private final String compiler;
        private final String executor;

        Language(String name, String extension, String compiler, String executor) {
            this.name = name;
            this.extension = extension;
            this.compiler = compiler;
            this.executor = executor;
        }

        @JsonCreator
        public static Language fromValue(String value) {
            for (Language lang : values()) {
                if (lang.name.equalsIgnoreCase(value) || lang.name().equalsIgnoreCase(value)) {
                    return lang;
                }
            }
            throw new IllegalArgumentException("Unknown language: " + value);
        }

        @JsonValue
        public String toValue() {
            return this.name;
        }
    }

    public enum ExecutionStatus {
        PENDING, RUNNING, SUCCESS, ERROR, TIMEOUT
    }
}
