package com.spring.aicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.constant.AppConstant;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.model.dto.app.AppVersionQueryRequest;
import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.model.entity.AppVersion;
import com.spring.aicodemother.mapper.AppVersionMapper;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.vo.AppVersionVO;
import com.spring.aicodemother.model.vo.UserVO;
import com.spring.aicodemother.manager.CosManager;
import com.spring.aicodemother.service.AppVersionService;
import com.spring.aicodemother.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 应用版本历史 服务层实现。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Service
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements AppVersionService {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Override
    public Long saveVersion(App app, String deployUrl, User loginUser) {
        ThrowUtils.throwIf(app == null || app.getId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        Long appId = app.getId();
        String codeGenType = app.getCodeGenType();

        // 1. 构建代码目录路径
        String codeDirName = codeGenType + "_" + appId;
        String codeDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + codeDirName;
        File codeDir = new File(codeDirPath);

        // 检查代码目录是否存在
        if (!codeDir.exists() || !codeDir.isDirectory()) {
            log.warn("代码目录不存在，无法保存版本：{}", codeDirPath);
            return null;
        }

        // 2. 读取代码目录中的所有文件，打包成JSON
        String codeContent = packCodeToJson(codeDir);

        // 2.5 将代码内容上传到COS
        String codeStorageUrl = null;
        File tempJsonFile = null;
        try {
            // 创建临时JSON文件
            tempJsonFile = new File(AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "temp_" + UUID.randomUUID() + ".json");
            FileUtil.writeString(codeContent, tempJsonFile, StandardCharsets.UTF_8);
            
            // 上传到COS
            String cosKey = String.format("app-versions/%d/%s_v%s.json", 
                appId, 
                codeGenType, 
                System.currentTimeMillis());
            codeStorageUrl = cosManager.uploadFile(cosKey, tempJsonFile);
            
            if (codeStorageUrl != null) {
                log.info("代码内容已上传到COS: {}", codeStorageUrl);
                // 清空codeContent，避免存储到数据库
                codeContent = null;
            } else {
                log.warn("代码内容上传COS失败，将存储到数据库（可能因为文件过大而失败）");
            }
        } catch (Exception e) {
            log.error("上传代码内容到COS时出错: {}", e.getMessage(), e);
        } finally {
            // 删除临时文件
            if (tempJsonFile != null && tempJsonFile.exists()) {
                FileUtil.del(tempJsonFile);
            }
        }

        // 3. 查询当前应用的最大版本号
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(AppVersion::getAppId, appId)
                .orderBy(AppVersion::getVersionNum, false)
                .limit(1);
        AppVersion latestVersion = this.getOne(queryWrapper);
        int nextVersionNum = (latestVersion == null) ? 1 : latestVersion.getVersionNum() + 1;
        String versionTag = "v" + nextVersionNum;

        // 4. 创建版本记录
        AppVersion appVersion = AppVersion.builder()
                .appId(appId)
                .versionNum(nextVersionNum)
                .versionTag(versionTag)
                .codeContent(codeContent)  // 如果上传COS成功则为null
                .codeStorageUrl(codeStorageUrl)  // COS存储URL
                .deployKey(app.getDeployKey())
                .deployUrl(deployUrl)
                .deployedTime(LocalDateTime.now())
                .userId(loginUser.getId())
                .build();

        boolean result = this.save(appVersion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存版本失败");

        log.info("保存应用版本成功：appId={}, versionNum={}, versionTag={}", appId, nextVersionNum, versionTag);
        return appVersion.getId();
    }

    @Override
    public Page<AppVersionVO> listVersionsByPage(AppVersionQueryRequest appVersionQueryRequest) {
        long current = appVersionQueryRequest.getPageNum();
        long size = appVersionQueryRequest.getPageSize();

        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(AppVersion::getAppId, appVersionQueryRequest.getAppId())
                .orderBy(AppVersion::getVersionNum, false);

        // 分页查询
        Page<AppVersion> appVersionPage = this.page(Page.of(current, size), queryWrapper);

        // 转换为VO
        Page<AppVersionVO> appVersionVOPage = new Page<>(current, size, appVersionPage.getTotalRow());
        List<AppVersionVO> appVersionVOList = appVersionPage.getRecords().stream()
                .map(this::getAppVersionVO)
                .collect(Collectors.toList());
        appVersionVOPage.setRecords(appVersionVOList);

        return appVersionVOPage;
    }

    @Override
    public List<AppVersionVO> listVersionsByAppId(Long appId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(AppVersion::getAppId, appId)
                .orderBy(AppVersion::getVersionNum, false);

        List<AppVersion> appVersionList = this.list(queryWrapper);
        return appVersionList.stream()
                .map(this::getAppVersionVO)
                .collect(Collectors.toList());
    }

    @Override
    public AppVersionVO getVersionVO(Long versionId) {
        AppVersion appVersion = this.getById(versionId);
        ThrowUtils.throwIf(appVersion == null, ErrorCode.NOT_FOUND_ERROR, "版本不存在");
        return getAppVersionVO(appVersion);
    }

    @Override
    public boolean rollbackToVersion(Long versionId, Long appId, User loginUser) {
        ThrowUtils.throwIf(versionId == null || appId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 1. 查询目标版本
        AppVersion targetVersion = this.getById(versionId);
        ThrowUtils.throwIf(targetVersion == null, ErrorCode.NOT_FOUND_ERROR, "版本不存在");
        ThrowUtils.throwIf(!targetVersion.getAppId().equals(appId), ErrorCode.PARAMS_ERROR, "版本与应用不匹配");

        // 2. 获取代码内容（优先从COS获取）
        String codeContent = null;
        String codeStorageUrl = targetVersion.getCodeStorageUrl();
        
        if (cn.hutool.core.util.StrUtil.isNotBlank(codeStorageUrl)) {
            // 从COS下载代码内容
            try {
                String cosKey = cosManager.extractKeyFromUrl(codeStorageUrl);
                if (cosKey != null) {
                    String tempFilePath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "temp_rollback_" + UUID.randomUUID() + ".json";
                    boolean downloadSuccess = cosManager.downloadFile(cosKey, tempFilePath);
                    
                    if (downloadSuccess) {
                        codeContent = FileUtil.readString(new File(tempFilePath), StandardCharsets.UTF_8);
                        FileUtil.del(tempFilePath);  // 删除临时文件
                        log.info("从COS下载代码内容成功：{}", codeStorageUrl);
                    } else {
                        log.warn("从COS下载代码内容失败：{}", codeStorageUrl);
                    }
                }
            } catch (Exception e) {
                log.error("从COS下载代码内容时出错：{}", codeStorageUrl, e);
            }
        }
        
        // 如果COS下载失败，尝试从数据库获取（兼容旧版本）
        if (cn.hutool.core.util.StrUtil.isBlank(codeContent)) {
            codeContent = targetVersion.getCodeContent();
        }
        
        ThrowUtils.throwIf(cn.hutool.core.util.StrUtil.isBlank(codeContent), ErrorCode.OPERATION_ERROR, "版本代码内容为空");

        // 3. 将文件写回到代码生成目录
        String codeGenType = codeContent.contains("\"codeGenType\"") ?
                JSONUtil.parseObj(codeContent).getStr("codeGenType") : "html";
        String codeDirName = codeGenType + "_" + appId;
        String codeDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + codeDirName;

        try {
            unpackJsonToCode(codeContent, codeDirPath);
            log.info("版本回滚成功：appId={}, versionId={}, versionNum={}", appId, versionId, targetVersion.getVersionNum());
            return true;
        } catch (Exception e) {
            log.error("版本回滚失败：appId={}, versionId={}", appId, versionId, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "版本回滚失败：" + e.getMessage());
        }
    }

    /**
     * 将代码目录打包成JSON
     *
     * @param codeDir 代码目录
     * @return JSON字符串
     */
    private String packCodeToJson(File codeDir) {
        JSONObject jsonObject = new JSONObject();
        JSONArray filesArray = new JSONArray();

        // 递归读取所有文件
        packDirectory(codeDir, codeDir.getAbsolutePath(), filesArray);

        jsonObject.set("files", filesArray);
        jsonObject.set("totalFiles", filesArray.size());
        return jsonObject.toString();
    }

    /**
     * 递归打包目录
     *
     * @param dir        当前目录
     * @param basePath   基础路径
     * @param filesArray 文件数组
     */
    private void packDirectory(File dir, String basePath, JSONArray filesArray) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归处理子目录
                packDirectory(file, basePath, filesArray);
            } else {
                // 读取文件内容
                String content = FileUtil.readString(file, StandardCharsets.UTF_8);
                String relativePath = file.getAbsolutePath().substring(basePath.length() + 1).replace("\\", "/");

                JSONObject fileObj = new JSONObject();
                fileObj.set("path", relativePath);
                fileObj.set("content", content);
                filesArray.add(fileObj);
            }
        }
    }

    /**
     * 将JSON解包到代码目录
     *
     * @param codeContent JSON字符串
     * @param codeDirPath 目标目录路径
     */
    private void unpackJsonToCode(String codeContent, String codeDirPath) {
        JSONObject jsonObject = JSONUtil.parseObj(codeContent);
        JSONArray filesArray = jsonObject.getJSONArray("files");

        // 清空目标目录
        File codeDir = new File(codeDirPath);
        if (codeDir.exists()) {
            FileUtil.del(codeDir);
        }
        FileUtil.mkdir(codeDir);

        // 写入文件
        for (Object obj : filesArray) {
            JSONObject fileObj = (JSONObject) obj;
            String path = fileObj.getStr("path");
            String content = fileObj.getStr("content");

            File targetFile = new File(codeDirPath + File.separator + path);
            FileUtil.writeString(content, targetFile, StandardCharsets.UTF_8);
        }
    }

    /**
     * 将AppVersion转换为AppVersionVO
     *
     * @param appVersion 版本实体
     * @return 版本VO
     */
    private AppVersionVO getAppVersionVO(AppVersion appVersion) {
        AppVersionVO appVersionVO = new AppVersionVO();
        BeanUtil.copyProperties(appVersion, appVersionVO);

        // 查询用户信息
        User user = userService.getById(appVersion.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user, userVO);
            appVersionVO.setUser(userVO);
        }

        // 不返回完整的codeContent，减少数据传输
        appVersionVO.setCodeContent(null);

        return appVersionVO;
    }
}
