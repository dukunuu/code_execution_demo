package com.biye_daalt.ollama_problem_generator.dto;

public class OllamaResponse {
    private String problem;
    private String title;

    public OllamaResponse() {}

    public OllamaResponse(String problem, String title) {
        this.problem = problem;
        this.title = title;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
