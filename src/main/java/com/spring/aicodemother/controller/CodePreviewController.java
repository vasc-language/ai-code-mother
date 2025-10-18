package com.spring.aicodemother.controller;

import com.spring.aicodemother.common.BaseResponse;
import com.spring.aicodemother.common.ResultUtils;
import com.spring.aicodemother.constant.AppConstant;
import com.spring.aicodemother.constant.UserConstant;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.service.AppService;
import com.spring.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * 代码预览控制器
 * 提供访问生成代码和已部署代码的功能
 */
@RestController
@RequestMapping("/code")
@Slf4j
public class CodePreviewController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 列出应用的代码文件结构
     * 
     * @param appId 应用ID
     * @param request HTTP请求
     * @return 文件结构
     */
    @GetMapping("/list/{appId}")
    public BaseResponse<Map<String, Object>> listCodeFiles(@PathVariable Long appId, HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 权限检查：只有创建者或管理员可以查看
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用代码");
        }
        
        // 构建文件目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "代码文件不存在，请先生成代码");
        }
        
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("appId", appId);
            result.put("appName", app.getAppName());
            result.put("codeGenType", codeGenType);
            result.put("files", listFiles(sourceDir.toPath(), sourceDir.toPath()));
            
            return ResultUtils.success(result);
        } catch (IOException e) {
            log.error("列出文件失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件列表失败");
        }
    }

    /**
     * 获取指定文件的内容
     * 
     * @param appId 应用ID
     * @param filePath 文件路径（相对于代码根目录）
     * @param request HTTP请求
     * @return 文件内容
     */
    @GetMapping("/file/{appId}")
    public BaseResponse<Map<String, Object>> getFileContent(
            @PathVariable Long appId,
            @RequestParam String filePath,
            HttpServletRequest request) {
        
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(filePath == null || filePath.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "文件路径不能为空");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 权限检查
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用代码");
        }
        
        try {
            // 构建完整文件路径
            String codeGenType = app.getCodeGenType();
            String sourceDirName = codeGenType + "_" + appId;
            String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
            
            // 安全检查：防止路径遍历攻击
            Path baseDir = Paths.get(sourceDirPath).normalize();
            Path targetFile = baseDir.resolve(filePath).normalize();
            
            if (!targetFile.startsWith(baseDir)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法的文件路径");
            }
            
            File file = targetFile.toFile();
            if (!file.exists() || !file.isFile()) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
            }
            
            // 读取文件内容
            String content = Files.readString(targetFile);
            
            Map<String, Object> result = new HashMap<>();
            result.put("filePath", filePath);
            result.put("fileName", file.getName());
            result.put("content", content);
            result.put("size", file.length());
            
            return ResultUtils.success(result);
            
        } catch (IOException e) {
            log.error("读取文件失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败");
        }
    }

    /**
     * 下载应用的所有代码文件（ZIP格式）
     * 
     * @param appId 应用ID
     * @param request HTTP请求
     * @param response HTTP响应
     */
    @GetMapping("/download/{appId}")
    public void downloadCodeFiles(
            @PathVariable Long appId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 权限检查
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
        }
        
        // 构建文件目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "代码文件不存在");
        }
        
        // 设置响应头
        String fileName = app.getAppName() + "_code.zip";
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        
        // 创建ZIP并写入响应流
        try (var zipOut = new java.util.zip.ZipOutputStream(response.getOutputStream())) {
            addDirectoryToZip(sourceDir.toPath(), sourceDir.toPath(), zipOut);
            zipOut.finish();
        } catch (IOException e) {
            log.error("创建ZIP文件失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        }
    }

    /**
     * 递归列出目录中的所有文件
     */
    private List<Map<String, Object>> listFiles(Path rootPath, Path currentPath) throws IOException {
        List<Map<String, Object>> files = new ArrayList<>();
        
        Files.walkFileTree(currentPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.getFileName().toString();
                long fileSize = attrs.size();
                
                // 跳过超大文件（超过 5MB），避免前端加载卡顿
                final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
                if (fileSize > MAX_FILE_SIZE) {
                    log.debug("跳过大文件: {} ({}MB)", file, fileSize / 1024 / 1024);
                    return FileVisitResult.CONTINUE;
                }
                
                // 跳过特定文件
                if (fileName.equals("package-lock.json") ||
                    fileName.equals("yarn.lock") ||
                    fileName.equals("pnpm-lock.yaml") ||
                    fileName.endsWith(".lock")) {
                    log.debug("跳过锁定文件: {}", fileName);
                    return FileVisitResult.CONTINUE;
                }
                
                Map<String, Object> fileInfo = new HashMap<>();
                String relativePath = rootPath.relativize(file).toString().replace("\\", "/");
                fileInfo.put("name", fileName);
                fileInfo.put("path", relativePath);
                fileInfo.put("size", fileSize);
                fileInfo.put("type", "file");
                files.add(fileInfo);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // 跳过不需要的目录
                String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
                
                // 需要跳过的目录列表
                if (dirName.equals("node_modules") ||      // Node.js 依赖
                    dirName.equals("dist") ||              // 打包产物
                    dirName.equals("build") ||             // 编译输出
                    dirName.equals(".git") ||              // Git 版本控制
                    dirName.equals(".svn") ||              // SVN 版本控制
                    dirName.equals("target") ||            // Maven 编译输出
                    dirName.equals(".idea") ||             // IntelliJ IDEA 配置
                    dirName.equals(".vscode") ||           // VS Code 配置
                    dirName.equals(".eclipse") ||          // Eclipse 配置
                    dirName.equals(".settings") ||         // Eclipse 设置
                    dirName.equals("coverage") ||          // 测试覆盖率
                    dirName.equals(".nuxt") ||             // Nuxt.js 缓存
                    dirName.equals(".next") ||             // Next.js 缓存
                    dirName.equals(".cache") ||            // 通用缓存
                    dirName.equals("__pycache__")) {       // Python 缓存
                    log.debug("跳过目录: {}", dir);
                    return FileVisitResult.SKIP_SUBTREE;   // 跳过整个子树
                }
                
                if (!dir.equals(rootPath)) {
                    Map<String, Object> dirInfo = new HashMap<>();
                    String relativePath = rootPath.relativize(dir).toString().replace("\\", "/");
                    dirInfo.put("name", dirName);
                    dirInfo.put("path", relativePath);
                    dirInfo.put("type", "directory");
                    files.add(dirInfo);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        
        return files;
    }

    /**
     * 将目录添加到ZIP文件中
     */
    private void addDirectoryToZip(Path rootPath, Path currentPath, java.util.zip.ZipOutputStream zipOut) throws IOException {
        Files.walkFileTree(currentPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String relativePath = rootPath.relativize(file).toString().replace("\\", "/");
                zipOut.putNextEntry(new java.util.zip.ZipEntry(relativePath));
                Files.copy(file, zipOut);
                zipOut.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
    }
}