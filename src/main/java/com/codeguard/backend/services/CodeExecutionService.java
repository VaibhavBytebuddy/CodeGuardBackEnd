package com.codeguard.backend.services;

import com.codeguard.backend.dto.CodeExecutionRequest;
import com.codeguard.backend.dto.CodeExecutionResponse;
import com.codeguard.backend.model.CodeSubmission;
import com.codeguard.backend.repository.CodeSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    @Autowired
    private CodeSubmissionRepository repository;

    private final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/code-editor/";
    private final long EXECUTION_TIMEOUT = 10000; // 10 seconds

    public CodeExecutionService() {
        // Create temp directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(TEMP_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp directory", e);
        }
    }

    public CodeExecutionResponse executeCode(CodeExecutionRequest request) {
        // Save submission to database
        CodeSubmission submission = new CodeSubmission(
                request.getFileName(),
                request.getCode(),
                request.getLanguage()
        );
        submission.setUserId(request.getUserId());
        submission = repository.save(submission);

        long startTime = System.currentTimeMillis();

        try {
            submission.setStatus(CodeSubmission.ExecutionStatus.RUNNING);
            repository.save(submission);

            String output = executeCodeByLanguage(request);
            long executionTime = System.currentTimeMillis() - startTime;

            submission.setOutput(output);
            submission.setStatus(CodeSubmission.ExecutionStatus.SUCCESS);
            submission.setExecutionTime(executionTime);
            repository.save(submission);

            return new CodeExecutionResponse(
                    submission.getId(),
                    output,
                    CodeSubmission.ExecutionStatus.SUCCESS,
                    executionTime
            );

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            submission.setErrorMessage(e.getMessage());
            submission.setStatus(CodeSubmission.ExecutionStatus.ERROR);
            submission.setExecutionTime(executionTime);
            repository.save(submission);

            CodeExecutionResponse response = new CodeExecutionResponse();
            response.setSubmissionId(submission.getId());
            response.setError(e.getMessage());
            response.setStatus(CodeSubmission.ExecutionStatus.ERROR);
            response.setExecutionTime(executionTime);

            return response;
        }
    }

    private String executeCodeByLanguage(CodeExecutionRequest request) throws Exception {
        switch (request.getLanguage()) {
            case JAVA:
                return executeJavaCode(request);
            case CPP:
                return executeCppCode(request);
            case JAVASCRIPT:
                return executeJavaScriptCode(request);
            default:
                throw new UnsupportedOperationException("Language not supported: " + request.getLanguage());
        }
    }

    private String executeJavaCode(CodeExecutionRequest request) throws Exception {
        String fileName = request.getFileName();
        if (!fileName.endsWith(".java")) {
            fileName += ".java";
        }

        // Extract class name from the code itself, not just filename
        String className = extractJavaClassName(request.getCode());
        if (className == null) {
            className = fileName.replace(".java", "");
        }

        // Use the extracted class name for the file
        String actualFileName = className + ".java";
        Path filePath = Paths.get(TEMP_DIR, actualFileName);

        // Write code to file
        Files.write(filePath, request.getCode().getBytes());

        try {
            // Compile with better error handling
            String compileCommand = "javac \"" + filePath.toString() + "\"";
            String compileOutput = executeCommand(compileCommand, TEMP_DIR);

            if (!compileOutput.trim().isEmpty()) {
                throw new RuntimeException("Compilation Error: " + compileOutput);
            }

            // Check if class file was created
            Path classFilePath = Paths.get(TEMP_DIR, className + ".class");
            if (!Files.exists(classFilePath)) {
                throw new RuntimeException("Compilation failed: .class file not generated");
            }

            // Execute with better classpath handling
            String executeCommand = "java -cp \"" + TEMP_DIR + "\" " + className;
            return executeCommand(executeCommand, TEMP_DIR);

        } finally {
            // Cleanup both original filename and actual filename
            cleanupFiles(TEMP_DIR, className);
            if (!className.equals(fileName.replace(".java", ""))) {
                cleanupFiles(TEMP_DIR, fileName.replace(".java", ""));
            }
        }
    }

    private String extractJavaClassName(String code) {
        // Extract class name from public class declaration
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("public class ")) {
                // Extract class name
                String[] parts = line.split("\\s+");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("class".equals(parts[i])) {
                        String className = parts[i + 1];
                        // Remove any opening brace
                        if (className.contains("{")) {
                            className = className.substring(0, className.indexOf("{"));
                        }
                        return className.trim();
                    }
                }
            }
        }

        // Fallback: look for any class declaration
        for (String line : lines) {
            line = line.trim();
            if (line.contains("class ") && !line.startsWith("//") && !line.startsWith("*")) {
                String[] parts = line.split("\\s+");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("class".equals(parts[i])) {
                        String className = parts[i + 1];
                        if (className.contains("{")) {
                            className = className.substring(0, className.indexOf("{"));
                        }
                        return className.trim();
                    }
                }
            }
        }

        return null;
    }

    private String executeCppCode(CodeExecutionRequest request) throws Exception {
        String fileName = request.getFileName();
        if (!fileName.endsWith(".cpp")) {
            fileName += ".cpp";
        }

        Path filePath = Paths.get(TEMP_DIR, fileName);
        Path executablePath = Paths.get(TEMP_DIR, "output");

        // Write code to file
        Files.write(filePath, request.getCode().getBytes());

        try {
            // Compile
            String compileCommand = "g++ -o " + executablePath.toString() + " " + filePath.toString();
            String compileOutput = executeCommand(compileCommand, TEMP_DIR);

            if (!compileOutput.isEmpty()) {
                throw new RuntimeException("Compilation Error: " + compileOutput);
            }

            // Execute
            String executeCommand = executablePath.toString();
            return executeCommand(executeCommand, TEMP_DIR);

        } finally {
            // Cleanup
            try {
                Files.deleteIfExists(filePath);
                Files.deleteIfExists(executablePath);
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }

    private String executeJavaScriptCode(CodeExecutionRequest request) throws Exception {
        String fileName = request.getFileName();
        if (!fileName.endsWith(".js")) {
            fileName += ".js";
        }

        Path filePath = Paths.get(TEMP_DIR, fileName);

        // Write code to file
        Files.write(filePath, request.getCode().getBytes());

        try {
            // Execute
            String executeCommand = "node " + filePath.toString();
            return executeCommand(executeCommand, TEMP_DIR);

        } finally {
            // Cleanup
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }

    private String executeCommand(String command, String workingDir) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Handle different operating systems
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            processBuilder.command("cmd", "/c", command);
        } else {
            processBuilder.command("bash", "-c", command);
        }

        processBuilder.directory(new File(workingDir));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Use ExecutorService for timeout handling
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                return output.toString();
            }
        });

        try {
            // Use future.get() with timeout instead of process.waitFor()
            String result = future.get(EXECUTION_TIMEOUT, TimeUnit.MILLISECONDS);

            // Wait for process to complete
            int exitCode = process.waitFor();

            // Check exit code for compilation/execution errors
            if (exitCode != 0 && result.trim().isEmpty()) {
                throw new RuntimeException("Process exited with code: " + exitCode);
            }

            return result;
        } catch (TimeoutException e) {
            process.destroyForcibly();
            future.cancel(true);
            throw new RuntimeException("Execution timeout - process took longer than " +
                    EXECUTION_TIMEOUT + "ms to complete");
        } catch (InterruptedException e) {
            process.destroyForcibly();
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execution interrupted");
        } finally {
            executor.shutdown();
        }
    }

    private void cleanupFiles(String directory, String className) {
        try {
            // Delete .java and .class files
            Files.deleteIfExists(Paths.get(directory, className + ".java"));
            Files.deleteIfExists(Paths.get(directory, className + ".class"));
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    public List<CodeSubmission> getUserSubmissions(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<CodeSubmission> getRecentSubmissions(String userId, int limit) {
        return repository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
    }

    public CodeSubmission getSubmissionById(String id) {
        return repository.findById(id).orElse(null);
    }
}
