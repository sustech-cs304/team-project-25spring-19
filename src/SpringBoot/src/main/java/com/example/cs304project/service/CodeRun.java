package com.example.cs304project.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
 * CHATGPT o3
 * 指令：如何实现前端传入的代码在后端直接运行并返回结果
 * 方式：复制代码
 * */
@Service
public class CodeRun {

    public String runCode(String code,String language){
        String result;
        try {
            if ("python".equalsIgnoreCase(language)) {
                result = runPythonCode(code);
            } else if ("java".equalsIgnoreCase(language)) {
                result = runJavaCode(code);
            } else if ("cpp".equalsIgnoreCase(language) || "c++".equalsIgnoreCase(language)) {
                result = runCppCode(code);
            } else {
                result = "Unsupported language: " + language;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = "Error occurred: " + e.getMessage();
        }
        return result;
    }
    private String runPythonCode(String code) throws Exception {
        File tempFile = File.createTempFile("codeSnippet", ".py");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }
        ProcessBuilder pb = new ProcessBuilder("python", tempFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = readProcessOutput(process.getInputStream());
        process.waitFor();
        if (process.exitValue() != 0) {
            output = "Execution error:\n" + output;
        }
        tempFile.delete();
        return output;
    }

    private String runJavaCode(String code) throws Exception {
        // 提取 Java 类名
        String className = extractJavaClassName(code);
        if (className == null) {
            return "Error: Unable to determine Java class name from code.";
        }
        // 在临时文件夹内创建一个以 className 命名的 .java 文件
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File javaFile = new File(tempDir, className + ".java");
        try (FileWriter writer = new FileWriter(javaFile)) {
            writer.write(code);
        }
        ProcessBuilder compilePb = new ProcessBuilder("javac", javaFile.getAbsolutePath());
        compilePb.redirectErrorStream(true);
        Process compileProcess = compilePb.start();
        String compileOutput = readProcessOutput(compileProcess.getInputStream());
        compileProcess.waitFor();
        String output;
        if (compileProcess.exitValue() != 0) {
            output = "Compilation error:\n" + compileOutput;
        } else {
            ProcessBuilder runPb = new ProcessBuilder("java", "-cp", tempDir.getAbsolutePath(), className);
            runPb.redirectErrorStream(true);
            Process runProcess = runPb.start();
            output = readProcessOutput(runProcess.getInputStream());
            runProcess.waitFor();
            if (runProcess.exitValue() != 0) {
                output = "Runtime error:\n" + output;
            }
        }
        // 清理临时文件
        javaFile.delete();
        File classFile = new File(tempDir, className + ".class");
        if (classFile.exists()) {
            classFile.delete();
        }
        return output;
    }

    private String runCppCode(String code) throws Exception {
        // 创建临时的 .cpp 源文件
        File tempFile = File.createTempFile("codeSnippet", ".cpp");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }
        // 编译输出的可执行文件路径（取临时文件名去除 .cpp 后缀）
        String executablePath = tempFile.getAbsolutePath().replace(".cpp", "");
        ProcessBuilder compilePb = new ProcessBuilder("g++", tempFile.getAbsolutePath(), "-o", executablePath);
        compilePb.redirectErrorStream(true);
        Process compileProcess = compilePb.start();
        String compileOutput = readProcessOutput(compileProcess.getInputStream());
        compileProcess.waitFor();
        String output;
        if (compileProcess.exitValue() != 0) {
            output = "Compilation error:\n" + compileOutput;
        } else {
            ProcessBuilder runPb = new ProcessBuilder(executablePath);
            runPb.redirectErrorStream(true);
            Process runProcess = runPb.start();
            output = readProcessOutput(runProcess.getInputStream());
            runProcess.waitFor();
            if (runProcess.exitValue() != 0) {
                output = "Runtime error:\n" + output;
            }
        }
        tempFile.delete();
        File executableFile = new File(executablePath);
        if (executableFile.exists()) {
            executableFile.delete();
        }
        return output;
    }

    private String readProcessOutput(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
        }
        return output.toString();
    }

    /**
     * 利用正则表达式解析出 Java 代码中的 public class 名称。
     */
    private String extractJavaClassName(String code) {
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
