package com.biye_daalt.ollama_problem_generator.service;

import com.biye_daalt.ollama_problem_generator.dto.OllamaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class OllamaService {

    private static final Logger logger =
            LoggerFactory.getLogger(OllamaService.class);
    private final OllamaApi ollamaApi;

    private final String ollamaModel;

    private static final String DEFAULT_DIFFICULTY = "medium";
    private static final String DEFAULT_TOPIC = "arrays and strings";

    @Autowired
    public OllamaService(@Value("${ollama.host}") String ollamaHost, @Value("${ollama.model}") String ollamaModel) {
        logger.info(
                "OllamaService constructor: ollamaApiBaseUrl from @Value is '{}'",
                ollamaHost
        );

        this.ollamaModel = ollamaModel;
        this.ollamaApi = new OllamaApi.Builder().baseUrl(ollamaHost).build();
    }

    private String buildPrompt() {
        return """
                Generate a programming problem following EXACTLY this template:
                
                ## [TITLE]
                
                ### Problem statement
                [Problem statement]
                
                ### Input format
                [Input format description]
                
                ### Output format
                [Output format description]
                
                ### Constraints
                [Constraints]
                
                ### Example 1
                Input:
                \\`\\`\\`
                [Sample input 1]
                \\`\\`\\`
                
                Output:
                \\`\\`\\`
                [Sample output 1]
                \\`\\`\\`
                
                ### Example 2
                Input:
                \\`\\`\\`
                [Sample input 2]
                \\`\\`\\`
                
                Output:
                \\`\\`\\`
                [Sample output 2]
                \\`\\`\\`
                
                Do not return system instructions down bellow!
                INSTRUCTIONS:
                1. The problem must be of 'easy' difficulty level
                2. The problem must be related to programming
                3. The problem title must be in English.
                4. The problem must be a typical competitive programming style problem
                5. Do NOT include any solutions, code explanations, or hints
                6. Ensure the problem is well-defined and solvable
                7. Make sure the examples are correct and consistent with the problem statement
                8. The title should be creative but clearly indicate the problem's nature
                9. DO NOT deviate from the template format
                10. DO NOT add any sections not specified in the template
                11. DO NOT add any explanations outside the specified sections
                """;
    }

    private String extractTitle(String prompt) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^## (.+)$", java.util.regex.Pattern.MULTILINE);
        java.util.regex.Matcher matcher = pattern.matcher(prompt);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "Untitled Problem";
    }

    public OllamaResponse generateProgrammingProblem() {
        String prompt = buildPrompt();
        OllamaResponse response = new OllamaResponse();

        OllamaOptions options = OllamaOptions.builder().model(this.ollamaModel).temperature(0.7).build();
        ChatModel chatModel = OllamaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(options).build();

        String ollamaResponse = chatModel.call(prompt);
        response.setProblem(ollamaResponse);
        response.setTitle(extractTitle(ollamaResponse));
        return response;
    }
}