package com.spring.aicodemother.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.mapper.PointsRecordMapper;
import com.spring.aicodemother.model.entity.PointsRecord;
import com.spring.aicodemother.service.PointsRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分明细 服务层实现。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Service
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements PointsRecordService {

    @Override
    public List<PointsRecord> getUserPointsRecords(Long userId, String type) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(PointsRecord::getUserId, userId);

        if (type != null && !type.isEmpty() && !"ALL".equals(type)) {
            queryWrapper.eq(PointsRecord::getType, type);
        }

        queryWrapper.orderBy(PointsRecord::getCreateTime, false);

        return this.list(queryWrapper);
    }

}
