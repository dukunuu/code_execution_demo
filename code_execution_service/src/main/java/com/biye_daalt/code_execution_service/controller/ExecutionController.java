package com.biye_daalt.code_execution_service.controller;

import com.biye_daalt.code_execution_service.dto.ExecutionRequest;
import com.biye_daalt.code_execution_service.dto.ExecutionResponse;
import com.biye_daalt.code_execution_service.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/execute")
public class ExecutionController {

    private final CodeExecutionService codeExecutionService;

    @Autowired
    public ExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping
    public ResponseEntity<ExecutionResponse> execute(@RequestBody ExecutionRequest request) {
        if (request.getCode() == null || request.getCode().isBlank() ||
                request.getLanguage() == null || request.getLanguage().isBlank()) {
            ExecutionResponse errorResponse = new ExecutionResponse();
            errorResponse.setError("Code and language must be provided.");
            errorResponse.setExitCode(-1);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        ExecutionResponse response = codeExecutionService.executeCode(request.getCode(), request.getLanguage());
        return ResponseEntity.ok(response);
    }
}
