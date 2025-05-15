package com.biye_daalt.code_execution_service.dto;

public class ExecutionRequest {
    private String code;
    private String language;

    public ExecutionRequest() {}

    public ExecutionRequest(String code, String language) {
        this.code = code;
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
