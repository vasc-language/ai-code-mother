package com.spring.aicodemother.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目文件下载
 */
public interface ProjectDownloadService {
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
