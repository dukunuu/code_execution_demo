package com.biye_daalt.ollama_problem_generator.controller;

import com.biye_daalt.ollama_problem_generator.dto.OllamaResponse;
import com.biye_daalt.ollama_problem_generator.service.OllamaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OllamaController {

    private static final Logger logger =
            LoggerFactory.getLogger(OllamaController.class);
    private final OllamaService ollamaService;

    public OllamaController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @GetMapping(
            value = "/generate-problem"
    )
    public ResponseEntity<OllamaResponse> generateProblem() {
        logger.info(
                "Received request to generate a default programming problem."
        );
        OllamaResponse response = ollamaService.generateProgrammingProblem();
        return ResponseEntity.ok(response);
    }
}
