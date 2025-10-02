package com.spring.aicodemother.service;

import com.mybatisflex.core.service.IService;
import com.spring.aicodemother.model.entity.PointsRecord;

import java.util.List;

/**
 * 积分明细 服务层。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
public interface PointsRecordService extends IService<PointsRecord> {

    /**
     * 查询用户的积分记录
     *
     * @param userId 用户ID
     * @param type 积分类型（可选）
     * @return 积分记录列表
     */
    List<PointsRecord> getUserPointsRecords(Long userId, String type);

}
