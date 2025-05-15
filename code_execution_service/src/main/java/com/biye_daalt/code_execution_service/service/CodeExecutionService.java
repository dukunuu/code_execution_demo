package com.biye_daalt.code_execution_service.service;

import com.biye_daalt.code_execution_service.dto.ExecutionResponse;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; // Added
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {

    private static final Logger logger = LoggerFactory
            .getLogger(CodeExecutionService.class);

    private final DockerClient dockerClient;
    private final long executionTimeoutSeconds;
    private final String tempFileHostBasePath; // Renamed for clarity

    private static final String CONTAINER_WORKING_DIR = "/app";
    private static final Map<String, LanguageConfig> languageConfigs;

    static {
        languageConfigs = new HashMap<>();
        languageConfigs.put(
                "python",
                new LanguageConfig("dukunuu/python-executor:latest", "script.py")
        );
        languageConfigs.put(
                "javascript",
                new LanguageConfig("dukunuu/js-executor:latest", "script.js")
        );
        languageConfigs.put(
                "java",
                new LanguageConfig("dukunuu/java-executor:latest", "Main.java")
        );
    }

    private static class LanguageConfig {
        final String imageName;
        final String scriptFileName;

        LanguageConfig(String imageName, String scriptFileName) {
            this.imageName = imageName;
            this.scriptFileName = scriptFileName;
        }
    }

    @Autowired
    public CodeExecutionService(
            DockerClient dockerClient,
            @Value("${execution.timeout.seconds:15}") long executionTimeoutSeconds,
            // This path MUST be accessible by the Docker daemon AND the service container.
            // If service is in a container, this path inside service container should be a mount
            // from a host path that Docker daemon can see.
            @Value(
                    "${execution.temp.host.basepath:/tmp/code_execution_service_temp}"
            ) String tempFileHostBasePath
    ) {
        this.dockerClient = dockerClient;
        this.executionTimeoutSeconds = executionTimeoutSeconds;
        this.tempFileHostBasePath = tempFileHostBasePath;
        logger.info(
                "CodeExecutionService initialized with timeout: {} seconds",
                executionTimeoutSeconds
        );
        logger.info(
                "Temporary script files will be created in host base path: {}",
                this.tempFileHostBasePath
        );
        try {
            Files.createDirectories(Paths.get(this.tempFileHostBasePath));
            logger.info(
                    "Ensured temporary file base directory exists: {}",
                    this.tempFileHostBasePath
            );
        } catch (IOException e) {
            logger.error(
                    "CRITICAL: Failed to create temporary file base directory: {}. " +
                            "Please ensure this path is writable by the service and " +
                            "accessible by the Docker daemon for bind mounts.",
                    this.tempFileHostBasePath,
                    e
            );
            // Consider throwing a runtime exception here to prevent service startup
            // if this directory is essential and cannot be created.
        }
    }

    public ExecutionResponse executeCode(String code, String language) {
        ExecutionResponse response = new ExecutionResponse();
        LanguageConfig config = languageConfigs.get(
                language.toLowerCase().trim()
        );

        if (config == null) {
            response.setError("Unsupported language: " + language);
            response.setExitCode(-1);
            logger.warn("Unsupported language request: {}", language);
            return response;
        }

        Path hostScriptFilePath = null; // Path to the script file on the host/shared volume
        String containerId = null;

        try {
            // 1. Create temp file with user's code in the configured shared path
            Path tempDir = Paths.get(this.tempFileHostBasePath);
            // Ensure the directory exists (it should from constructor, but good practice)
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            // Create a uniquely named file in the shared directory
            hostScriptFilePath = Files.createTempFile(
                    tempDir, // Directory for the temp file
                    "userscript_", // Prefix
                    "_" + config.scriptFileName // Suffix (e.g., "_script.py")
            );
            Files.writeString(
                    hostScriptFilePath,
                    code,
                    StandardCharsets.UTF_8
            );
            logger.debug(
                    "Temporary script file created at host path: {}",
                    hostScriptFilePath.toAbsolutePath()
            );

            // 2. Prepare HostConfig for bind mount
            // This hostPathForBind MUST be the path as seen by the Docker Daemon
            String hostPathForBind = hostScriptFilePath
                    .toAbsolutePath()
                    .toString();
            String containerPath = CONTAINER_WORKING_DIR +
                    "/" +
                    config.scriptFileName; // e.g., /app/script.py

            logger.info(
                    "Attempting to bind hostPath: [{}] to containerPath: [{}]",
                    hostPathForBind,
                    containerPath
            );

            Bind bind = new Bind(
                    hostPathForBind,
                    new Volume(containerPath),
                    AccessMode.ro
            );
            HostConfig hostConfig = HostConfig
                    .newHostConfig()
                    .withBinds(bind)
                    .withMemory(256L * 1024 * 1024)
                    .withCpuShares(512);

            // 3. Create container
            logger.debug(
                    "Creating container with image: {}",
                    config.imageName
            );
            CreateContainerResponse containerResponse = dockerClient
                    .createContainerCmd(config.imageName)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(CONTAINER_WORKING_DIR)
                    .withNetworkDisabled(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();
            containerId = containerResponse.getId();
            logger.info(
                    "Container created: {} for language: {}",
                    containerId,
                    language
            );

            // 4. Start container
            dockerClient.startContainerCmd(containerId).exec();
            logger.debug("Container started: {}", containerId);

            // 5. Wait for container to complete or timeout
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            int exitCode = -1;
            boolean timedOut = false;

            try {
                logger.debug(
                        "Waiting for container {} to complete (timeout: {}s)",
                        containerId,
                        executionTimeoutSeconds
                );
                exitCode = dockerClient
                        .waitContainerCmd(containerId)
                        .exec(waitCallback)
                        .awaitStatusCode(executionTimeoutSeconds, TimeUnit.SECONDS);
                response.setExitCode(exitCode);
                logger.info(
                        "Container {} finished with exit code: {}",
                        containerId,
                        exitCode
                );
            } catch (DockerClientException e) {
                logger.warn(
                        "Wait for container {} timed out or was interrupted: {}",
                        containerId,
                        e.getMessage()
                );
                timedOut = true;
                response.setTimeout(true);
                response.setError(
                        "Execution timed out after " +
                                executionTimeoutSeconds +
                                " seconds."
                );
                response.setExitCode(-1);

                try {
                    logger.info(
                            "Attempting to kill timed-out container: {}",
                            containerId
                    );
                    dockerClient.killContainerCmd(containerId).exec();
                } catch (Exception killEx) {
                    logger.error(
                            "Failed to kill timed-out container {}: {}",
                            containerId,
                            killEx.getMessage()
                    );
                }
            }

            // 6. Fetch logs
            final StringBuilder stdOutBuilder = new StringBuilder();
            final StringBuilder stdErrBuilder = new StringBuilder();

            LogContainerCmd logContainerCmd = dockerClient
                    .logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(false)
                    .withTimestamps(false);

            try {
                logContainerCmd
                        .exec(
                                new ResultCallback.Adapter<Frame>() {
                                    @Override
                                    public void onNext(Frame item) {
                                        if (
                                                StreamType.STDOUT.equals(item.getStreamType())
                                        ) {
                                            stdOutBuilder.append(
                                                    new String(
                                                            item.getPayload(),
                                                            StandardCharsets.UTF_8
                                                    )
                                            );
                                        } else if (
                                                StreamType.STDERR.equals(item.getStreamType())
                                        ) {
                                            stdErrBuilder.append(
                                                    new String(
                                                            item.getPayload(),
                                                            StandardCharsets.UTF_8
                                                    )
                                            );
                                        }
                                    }
                                }
                        )
                        .awaitCompletion();
                logger.debug(
                        "Logs collected for container {}",
                        containerId
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error(
                        "Log collection interrupted for container {}: {}",
                        containerId,
                        e.getMessage()
                );
                stdErrBuilder.append(
                        "\n[Service Error: Log collection interrupted]"
                );
            } catch (Exception e) {
                logger.error(
                        "Failed to retrieve logs for container {}: {}",
                        containerId,
                        e.getMessage()
                );
                stdErrBuilder.append(
                        "\n[Service Error: Failed to retrieve logs: " +
                                e.getMessage() +
                                "]"
                );
            }

            response.setOutput(stdOutBuilder.toString().trim());
            if (!timedOut) { // Only set error from stderr if not timed out
                response.setError(stdErrBuilder.toString().trim());
            } else { // If timed out, the timeout message is already set. Append stderr if any.
                String collectedStdErr = stdErrBuilder.toString().trim();
                if (!collectedStdErr.isEmpty()) {
                    response.setError(response.getError() + "\nPartial stderr before timeout:\n" + collectedStdErr);
                }
            }


        } catch (IOException e) {
            logger.error(
                    "IOException during code execution setup: {}",
                    e.getMessage(),
                    e
            );
            response.setError(
                    "Server error: Could not prepare execution environment. " +
                            e.getMessage()
            );
            response.setExitCode(-1);
        } catch (DockerException e) {
            logger.error(
                    "DockerException during code execution: {}",
                    e.getMessage(),
                    e
            );
            response.setError(
                    "Server error: Docker operation failed. " + e.getMessage()
            );
            response.setExitCode(-1);
        } catch (Exception e) {
            logger.error(
                    "Unexpected error during code execution: {}",
                    e.getMessage(),
                    e
            );
            response.setError(
                    "Server error: An unexpected error occurred. " + e.getMessage()
            );
            response.setExitCode(-1);
        } finally {
            if (containerId != null) {
                try {
                    logger.debug("Removing container: {}", containerId);
                    dockerClient
                            .removeContainerCmd(containerId)
                            .withForce(true)
                            .exec();
                    logger.info("Container removed: {}", containerId);
                } catch (Exception e) {
                    logger.error(
                            "Failed to remove container {}: {}",
                            containerId,
                            e.getMessage()
                    );
                }
            }
            if (hostScriptFilePath != null) {
                try {
                    Files.deleteIfExists(hostScriptFilePath);
                    logger.debug(
                            "Temporary script file deleted: {}",
                            hostScriptFilePath.toAbsolutePath()
                    );
                } catch (IOException e) {
                    logger.error(
                            "Failed to delete temporary script file {}: {}",
                            hostScriptFilePath,
                            e.getMessage()
                    );
                }
            }
        }
        return response;
    }
}
