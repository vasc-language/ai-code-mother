package com.spring.aicodemother.core.build;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 构建 Vue 项目
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * Java21的虚拟线程：异步构建 Vue 项目
     *
     * @param projectPath
     */
    public void buildProjectAsync(String projectPath) {
        // 在单独的线程中执行构建，避免阻塞主流程
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis()).start(() -> {
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 构建 Vue 项目
     *
     * @param projectPath 项目根目录路径
     * @return 构建结果
     */
    public BuildResult buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectPath);
            BuildResult result = new BuildResult(false);
            result.getStderrLines().add("项目目录不存在: " + projectPath);
            return result;
        }
        // 检查 package.json 是否存在
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在: {}", packageJson.getAbsolutePath());
            BuildResult result = new BuildResult(false);
            result.getStderrLines().add("package.json 文件不存在: " + packageJson.getAbsolutePath());
            return result;
        }
        log.info("开始构建 Vue 项目: {}", projectPath);
        // 执行 npm install
        BuildResult installResult = executeNpmInstall(projectDir);
        if (!installResult.isSuccess()) {
            log.error("npm install 执行失败");
            return installResult;
        }
        // 执行 npm run build
        BuildResult buildResult = executeNpmBuild(projectDir);
        if (!buildResult.isSuccess()) {
            log.error("npm run build 执行失败");
            return buildResult;
        }
        // 验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists()) {
            log.error("构建完成但 dist 目录未生成: {}", distDir.getAbsolutePath());
            BuildResult result = new BuildResult(false);
            result.getStderrLines().add("构建完成但 dist 目录未生成: " + distDir.getAbsolutePath());
            return result;
        }
        log.info("Vue 项目构建成功，dist 目录: {}", distDir.getAbsolutePath());
        return new BuildResult(true, 0, buildResult.getStdoutLines(), new ArrayList<>());
    }


    /**
     * 执行 npm install 命令
     */
    private BuildResult executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private BuildResult executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }

    /**
     * 根据操作系统构造命令
     *
     * @param baseCommand
     * @return
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 操作系统检测
     *
     * @return
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 构建结果
     */
    private BuildResult executeCommand(File workingDir, String command, int timeoutSeconds) {
        Process process = null;
        // 使用线程安全的集合收集输出
        List<String> stdoutLines = new CopyOnWriteArrayList<>();
        List<String> stderrLines = new CopyOnWriteArrayList<>();
        
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            
            // 启动线程读取标准输出
            final Process finalProcess = process;
            Thread stdoutReader = Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(finalProcess.getInputStream(), Charset.defaultCharset()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[STDOUT] {}", line);
                        stdoutLines.add(line);
                    }
                } catch (Exception e) {
                    log.error("读取标准输出失败: {}", e.getMessage());
                }
            });
            
            // 启动线程读取错误输出
            Thread stderrReader = Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(finalProcess.getErrorStream(), Charset.defaultCharset()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.error("[STDERR] {}", line);
                        stderrLines.add(line);
                    }
                } catch (Exception e) {
                    log.error("读取错误输出失败: {}", e.getMessage());
                }
            });
            
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            // 等待输出读取线程完成（最多1秒）
            stdoutReader.join(1000);
            stderrReader.join(1000);
            
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                stderrLines.add("命令执行超时（" + timeoutSeconds + "秒）");
                return new BuildResult(false, -1, new ArrayList<>(stdoutLines), new ArrayList<>(stderrLines));
            }
            
            int exitCode = process.exitValue();
            boolean success = exitCode == 0;
            
            if (success) {
                log.info("命令执行成功: {}", command);
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
            }
            
            return new BuildResult(success, exitCode, new ArrayList<>(stdoutLines), new ArrayList<>(stderrLines));
            
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage(), e);
            stderrLines.add("执行命令异常: " + e.getMessage());
            return new BuildResult(false, -1, new ArrayList<>(stdoutLines), new ArrayList<>(stderrLines));
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

}
