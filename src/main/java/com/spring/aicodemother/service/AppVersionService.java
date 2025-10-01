package com.spring.aicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.spring.aicodemother.model.dto.app.AppVersionQueryRequest;
import com.spring.aicodemother.model.entity.App;
import com.spring.aicodemother.model.entity.AppVersion;
import com.spring.aicodemother.model.entity.User;
import com.spring.aicodemother.model.vo.AppVersionVO;

import java.util.List;

/**
 * 应用版本历史 服务层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
public interface AppVersionService extends IService<AppVersion> {

    /**
     * 保存应用版本（部署时调用）
     *
     * @param app         应用信息
     * @param deployUrl   部署URL
     * @param loginUser   当前登录用户
     * @return 版本id
     */
    Long saveVersion(App app, String deployUrl, User loginUser);

    /**
     * 查询应用的版本列表（分页）
     *
     * @param appVersionQueryRequest 查询请求
     * @return 版本列表
     */
    Page<AppVersionVO> listVersionsByPage(AppVersionQueryRequest appVersionQueryRequest);

    /**
     * 查询应用的所有版本列表
     *
     * @param appId 应用id
     * @return 版本列表
     */
    List<AppVersionVO> listVersionsByAppId(Long appId);

    /**
     * 获取版本详情（包含用户信息）
     *
     * @param versionId 版本id
     * @return 版本详情
     */
    AppVersionVO getVersionVO(Long versionId);

    /**
     * 版本回滚（将历史版本的代码恢复到代码生成目录）
     *
     * @param versionId 版本id
     * @param appId     应用id
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean rollbackToVersion(Long versionId, Long appId, User loginUser);
}
