// ===== Controller =====
// src/main/java/com/vaibhav/codeeditor/controller/CodeEditorController.java
package com.codeguard.backend.controller;

import com.codeguard.backend.dto.CodeExecutionRequest;
import com.codeguard.backend.dto.CodeExecutionResponse;
import com.codeguard.backend.model.CodeSubmission;
import com.codeguard.backend.services.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class CodeEditorController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResponse> executeCode(
            @Valid @RequestBody CodeExecutionRequest request) {
        try {
            CodeExecutionResponse response = codeExecutionService.executeCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CodeExecutionResponse errorResponse = new CodeExecutionResponse();
            errorResponse.setError("Internal server error: " + e.getMessage());
            errorResponse.setStatus(CodeSubmission.ExecutionStatus.ERROR);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<CodeSubmission>> getUserSubmissions(
            @RequestParam(required = false) String userId) {
        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }
        List<CodeSubmission> submissions = codeExecutionService.getUserSubmissions(userId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/recent")
    public ResponseEntity<List<CodeSubmission>> getRecentSubmissions(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "10") int limit) {
        if (userId == null || userId.isEmpty()) {
            userId = "anonymous";
        }
        List<CodeSubmission> submissions = codeExecutionService.getRecentSubmissions(userId, limit);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<CodeSubmission> getSubmission(@PathVariable String id) {
        CodeSubmission submission = codeExecutionService.getSubmissionById(id);
        if (submission == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(submission);
    }
}