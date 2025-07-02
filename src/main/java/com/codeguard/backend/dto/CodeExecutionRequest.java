package com.codeguard.backend.dto;


import com.codeguard.backend.model.CodeSubmission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeExecutionRequest {
    
    @NotBlank
    private String code;
    
    @NotNull
    private CodeSubmission.Language language;
    
    @NotBlank
    private String fileName;
    
    private String userId;
}
    