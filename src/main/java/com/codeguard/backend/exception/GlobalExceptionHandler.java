package com.codeguard.backend.exception;



import com.codeguard.backend.dto.CodeExecutionResponse;
import com.codeguard.backend.model.CodeSubmission;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }
    
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<CodeExecutionResponse> handleUnsupportedOperation(
            UnsupportedOperationException ex) {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.setError(ex.getMessage());
        response.setStatus(CodeSubmission.ExecutionStatus.ERROR);
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CodeExecutionResponse> handleGenericException(Exception ex) {
        CodeExecutionResponse response = new CodeExecutionResponse();
        response.setError("Internal server error: " + ex.getMessage());
        response.setStatus(CodeSubmission.ExecutionStatus.ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}