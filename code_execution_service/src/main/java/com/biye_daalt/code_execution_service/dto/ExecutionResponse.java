package com.biye_daalt.code_execution_service.dto;

public class ExecutionResponse {
    private String output;
    private String error;
    private int exitCode = -1; // Default to -1 if not set
    private boolean timeout = false;

    public ExecutionResponse() {
    }

    public ExecutionResponse(String output, String error, int exitCode, boolean timeout) {
        this.output = output;
        this.error = error;
        this.exitCode = exitCode;
        this.timeout = timeout;
    }

    // Getters and Setters
    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }
}