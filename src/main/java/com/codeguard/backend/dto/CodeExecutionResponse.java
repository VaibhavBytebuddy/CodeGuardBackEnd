package com.codeguard.backend.dto;

import com.codeguard.backend.model.CodeSubmission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeExecutionResponse {

    private String submissionId;
    private String output;
    private String error;
    private CodeSubmission.ExecutionStatus status;
    private Long executionTime;


    public CodeExecutionResponse(String id, String output, CodeSubmission.ExecutionStatus executionStatus, long executionTime) {
        this.submissionId=id;
        this.output=output;
        this.status=executionStatus;
        this.executionTime=executionTime;
    }
}