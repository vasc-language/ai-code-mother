package com.spring.aicodemother.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.spring.aicodemother.config.CosClientConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * COS对象存储管理器
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Autowired(required = false)
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        if (cosClient == null) {
            log.warn("COSClient 未配置，跳过上传。key={}", key);
            return null;
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        if (cosClient == null) {
            log.warn("COSClient 未配置，无法上传文件: {}", file != null ? file.getName() : "null");
            return null;
        }
        // 上传文件
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            // 构建访问URL
            String url = String.format("%s%s", cosClientConfig.getHost(), key);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传COS失败，返回结果为空");
            return null;
        }
    }

    /**
     * 从 COS 下载文件到本地
     *
     * @param key             COS对象键
     * @param localFilePath   本地文件路径
     * @return 是否下载成功
     */
    public boolean downloadFile(String key, String localFilePath) {
        if (cosClient == null) {
            log.warn("COSClient 未配置，无法下载文件: {}", key);
            return false;
        }
        try {
            File localFile = new File(localFilePath);
            // 确保父目录存在
            if (localFile.getParentFile() != null && !localFile.getParentFile().exists()) {
                localFile.getParentFile().mkdirs();
            }
            GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
            cosClient.getObject(getObjectRequest, localFile);
            log.info("文件从COS下载成功: {} -> {}", key, localFilePath);
            return true;
        } catch (Exception e) {
            log.error("文件从COS下载失败: {}", key, e);
            return false;
        }
    }

    /**
     * 从URL提取COS对象键
     *
     * @param url COS文件URL
     * @return COS对象键，如果URL无效返回null
     */
    public String extractKeyFromUrl(String url) {
        if (url == null || cosClientConfig == null) {
            return null;
        }
        String host = cosClientConfig.getHost();
        if (url.startsWith(host)) {
            return url.substring(host.length());
        }
        return null;
    }
}
