package com.spring.aicodemother.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.spring.aicodemother.constants.PointsConstants;
import com.spring.aicodemother.exception.BusinessException;
import com.spring.aicodemother.exception.ErrorCode;
import com.spring.aicodemother.exception.ThrowUtils;
import com.spring.aicodemother.mapper.InviteRecordMapper;
import com.spring.aicodemother.model.entity.InviteRecord;
import com.spring.aicodemother.model.enums.InviteStatusEnum;
import com.spring.aicodemother.model.enums.PointsTypeEnum;
import com.spring.aicodemother.service.InviteRecordService;
import com.spring.aicodemother.service.UserPointsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 邀请关系 服务层实现。
 *
 * @author <a href="https://github.com/vasc-language">Join2049</a>
 */
@Slf4j
@Service
public class InviteRecordServiceImpl extends ServiceImpl<InviteRecordMapper, InviteRecord> implements InviteRecordService {

    @Resource
    private UserPointsService userPointsService;

    // 邀请奖励配置
    private static final int INVITE_REGISTER_POINTS = 20; // 注册奖励
    private static final int INVITE_FIRST_GENERATE_INVITER_POINTS = 30; // 首次生成-邀请人奖励
    private static final int INVITE_FIRST_GENERATE_INVITEE_POINTS = 10; // 首次生成-被邀请人奖励
    private static final int MAX_DAILY_INVITE_COUNT = 3; // 单日最多邀请次数
    private static final int ABUSE_CHECK_DAYS = 7; // 防刷检测天数

    @Override
    public String generateInviteCode(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        // 优先返回等待使用的占位邀请码
        QueryWrapper pendingWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviterId, userId)
                .eq(InviteRecord::getStatus, InviteStatusEnum.PENDING.getValue())
                .orderBy(InviteRecord::getCreateTime, false)
                .limit(1);

        InviteRecord existingRecord = this.getOne(pendingWrapper);
        if (existingRecord != null && existingRecord.getInviteCode() != null) {
            return existingRecord.getInviteCode();
        }

        // 兼容旧数据：若不存在占位记录，则查找最近一次记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviterId, userId)
                .isNotNull(InviteRecord::getInviteCode)
                .orderBy(InviteRecord::getCreateTime, false)
                .limit(1);

        InviteRecord latestRecord = this.getOne(queryWrapper);
        if (latestRecord != null && latestRecord.getInviteCode() != null) {
            return latestRecord.getInviteCode();
        }

        // 生成新的邀请码（8位随机字符串）
        String inviteCode;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            inviteCode = "INV" + RandomUtil.randomString(8).toUpperCase();
            attempts++;

            // 检查邀请码是否已存在
            QueryWrapper checkWrapper = QueryWrapper.create()
                    .eq(InviteRecord::getInviteCode, inviteCode);

            if (this.count(checkWrapper) == 0) {
                break;
            }

            if (attempts >= maxAttempts) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成邀请码失败，请重试");
            }
        } while (true);

        log.info("为用户 {} 生成邀请码: {}", userId, inviteCode);
        return inviteCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindInvite(String inviteCode, Long inviteeId, String registerIp, String deviceId) {
        ThrowUtils.throwIf(inviteCode == null || inviteCode.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "邀请码不能为空");
        ThrowUtils.throwIf(inviteeId == null || inviteeId <= 0, ErrorCode.PARAMS_ERROR, "被邀请人ID不能为空");

        // 根据邀请码查找邀请人
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviteCode, inviteCode)
                .eq(InviteRecord::getStatus, InviteStatusEnum.PENDING.getValue())
                .orderBy(InviteRecord::getCreateTime, false)
                .limit(1);

        InviteRecord inviterRecord = this.getOne(queryWrapper);

        // 兼容旧数据：若没有占位记录，退化为读取任一匹配记录
        if (inviterRecord == null) {
            QueryWrapper fallbackWrapper = QueryWrapper.create()
                    .eq(InviteRecord::getInviteCode, inviteCode)
                    .orderBy(InviteRecord::getCreateTime, false)
                    .limit(1);
            inviterRecord = this.getOne(fallbackWrapper);
        }
        if (inviterRecord == null) {
            // 如果邀请码不存在，创建一个临时记录用于生成邀请码
            // 这种情况可能是第一次使用邀请码
            log.warn("邀请码 {} 不存在", inviteCode);
            return false;
        }

        Long inviterId = inviterRecord.getInviterId();

        // 检查是否是自己邀请自己
        if (inviterId.equals(inviteeId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能使用自己的邀请码");
        }

        // 防刷检测
        if (checkInviteAbuse(registerIp, deviceId)) {
            log.warn("检测到邀请防刷风险，IP: {}, 设备ID: {}", registerIp, deviceId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邀请注册失败，请稍后重试");
        }

        // 检查邀请人今日邀请次数是否超限
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        QueryWrapper dailyCountWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviterId, inviterId)
                .ge(InviteRecord::getRegisterTime, todayStart)
                .eq(InviteRecord::getStatus, InviteStatusEnum.REGISTERED.getValue());

        long todayInviteCount = this.count(dailyCountWrapper);
        if (todayInviteCount >= MAX_DAILY_INVITE_COUNT) {
            log.warn("用户 {} 今日邀请次数已达上限", inviterId);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邀请人今日邀请次数已达上限");
        }

        // 创建邀请关系记录
        InviteRecord inviteRecord = InviteRecord.builder()
                .inviterId(inviterId)
                .inviteeId(inviteeId)
                .inviteCode(generateUniqueInviteRecordCode(inviteCode))
                .registerIp(registerIp)
                .deviceId(deviceId)
                .status(InviteStatusEnum.REGISTERED.getValue())
                .registerTime(LocalDateTime.now())
                .build();

        boolean saved = this.save(inviteRecord);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存邀请关系失败");

        log.info("绑定邀请关系成功，邀请人: {}, 被邀请人: {}", inviterId, inviteeId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rewardInviteRegister(Long inviteeId) {
        ThrowUtils.throwIf(inviteeId == null || inviteeId <= 0, ErrorCode.PARAMS_ERROR, "被邀请人ID不能为空");

        // 查询邀请关系
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviteeId, inviteeId)
                .eq(InviteRecord::getStatus, InviteStatusEnum.REGISTERED.getValue());

        InviteRecord inviteRecord = this.getOne(queryWrapper);
        if (inviteRecord == null) {
            log.warn("未找到用户 {} 的邀请关系", inviteeId);
            return false;
        }

        Long inviterId = inviteRecord.getInviterId();

        // 发放邀请人奖励
        boolean inviterRewarded = userPointsService.addPoints(
                inviterId,
                INVITE_REGISTER_POINTS,
                PointsTypeEnum.INVITE.getValue(),
                "邀请新用户注册奖励",
                inviteRecord.getId()
        );

        // 发放被邀请人奖励
        boolean inviteeRewarded = userPointsService.addPoints(
                inviteeId,
                INVITE_REGISTER_POINTS,
                PointsTypeEnum.INVITE.getValue(),
                "接受邀请注册奖励",
                inviteRecord.getId()
        );

        if (inviterRewarded && inviteeRewarded) {
            // 更新邀请记录
            inviteRecord.setInviterPoints(inviteRecord.getInviterPoints() + INVITE_REGISTER_POINTS);
            inviteRecord.setInviteePoints(inviteRecord.getInviteePoints() + INVITE_REGISTER_POINTS);
            this.updateById(inviteRecord);

            log.info("发放邀请注册奖励成功，邀请人: {}, 被邀请人: {}", inviterId, inviteeId);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rewardInviteFirstGenerate(Long inviteeId) {
        ThrowUtils.throwIf(inviteeId == null || inviteeId <= 0, ErrorCode.PARAMS_ERROR, "被邀请人ID不能为空");

        // 查询邀请关系
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviteeId, inviteeId)
                .in(InviteRecord::getStatus, InviteStatusEnum.REGISTERED.getValue(), InviteStatusEnum.REWARDED.getValue());

        InviteRecord inviteRecord = this.getOne(queryWrapper);
        if (inviteRecord == null) {
            log.warn("未找到用户 {} 的邀请关系", inviteeId);
            return false;
        }

        // 检查是否已经发放过首次生成奖励
        if (InviteStatusEnum.REWARDED.getValue().equals(inviteRecord.getStatus())) {
            log.info("用户 {} 已经领取过首次生成奖励", inviteeId);
            return false;
        }

        Long inviterId = inviteRecord.getInviterId();

        // 发放邀请人奖励
        boolean inviterRewarded = userPointsService.addPoints(
                inviterId,
                INVITE_FIRST_GENERATE_INVITER_POINTS,
                PointsTypeEnum.INVITE.getValue(),
                "邀请用户首次生成应用奖励",
                inviteRecord.getId()
        );

        // 发放被邀请人奖励
        boolean inviteeRewarded = userPointsService.addPoints(
                inviteeId,
                INVITE_FIRST_GENERATE_INVITEE_POINTS,
                PointsTypeEnum.INVITE.getValue(),
                "首次生成应用额外奖励",
                inviteRecord.getId()
        );

        if (inviterRewarded && inviteeRewarded) {
            // 更新邀请记录状态
            inviteRecord.setStatus(InviteStatusEnum.REWARDED.getValue());
            inviteRecord.setInviterPoints(inviteRecord.getInviterPoints() + INVITE_FIRST_GENERATE_INVITER_POINTS);
            inviteRecord.setInviteePoints(inviteRecord.getInviteePoints() + INVITE_FIRST_GENERATE_INVITEE_POINTS);
            inviteRecord.setRewardTime(LocalDateTime.now());
            this.updateById(inviteRecord);

            log.info("发放邀请首次生成奖励成功，邀请人: {} 获得 {} 积分, 被邀请人: {} 获得 {} 积分",
                    inviterId, INVITE_FIRST_GENERATE_INVITER_POINTS,
                    inviteeId, INVITE_FIRST_GENERATE_INVITEE_POINTS);
            return true;
        }

        return false;
    }

    @Override
    public boolean checkInviteAbuse(String registerIp, String deviceId) {
        if (registerIp == null && deviceId == null) {
            return false;
        }

        LocalDateTime checkTime = LocalDateTime.now().minusDays(ABUSE_CHECK_DAYS);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .ge(InviteRecord::getRegisterTime, checkTime);

        // 检查IP
        if (registerIp != null) {
            queryWrapper.and((java.util.function.Consumer<QueryWrapper>) qw -> qw.eq(InviteRecord::getRegisterIp, registerIp));
        }

        // 检查设备ID
        if (deviceId != null && registerIp == null) {
            queryWrapper.and((java.util.function.Consumer<QueryWrapper>) qw -> qw.eq(InviteRecord::getDeviceId, deviceId));
        }

        long count = this.count(queryWrapper);
        return count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revokeExpiredInviteRewards() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(PointsConstants.INVITE_REWARD_GRACE_DAYS);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(InviteRecord::getStatus, InviteStatusEnum.REGISTERED.getValue())
                .le(InviteRecord::getRegisterTime, deadline);

        List<InviteRecord> expiredRecords = this.list(queryWrapper);
        if (expiredRecords.isEmpty()) {
            return 0;
        }

        int revokedCount = 0;
        for (InviteRecord record : expiredRecords) {
            Long inviterId = record.getInviterId();
            Long inviteeId = record.getInviteeId();
            if (inviterId == null || inviteeId == null || inviteeId <= 0) {
                continue;
            }

            boolean deductionSucceeded = false;

            try {
                userPointsService.deductPoints(
                        inviterId,
                        PointsConstants.INVITE_REGISTER_INVITER_REWARD,
                        PointsTypeEnum.INVITE.getValue(),
                        "邀请奖励回收（被邀请人7天无操作）",
                        record.getId()
                );
                deductionSucceeded = true;
            } catch (Exception e) {
                log.warn("邀请奖励回收失败-邀请人扣减异常，记录ID: {}, 错误: {}", record.getId(), e.getMessage());
            }

            try {
                userPointsService.deductPoints(
                        inviteeId,
                        PointsConstants.INVITE_REGISTER_INVITEE_REWARD,
                        PointsTypeEnum.INVITE.getValue(),
                        "邀请奖励回收（被邀请人7天无操作）",
                        record.getId()
                );
                deductionSucceeded = true;
            } catch (Exception e) {
                log.warn("邀请奖励回收失败-被邀请人扣减异常，记录ID: {}, 错误: {}", record.getId(), e.getMessage());
            }

            int inviterPoints = record.getInviterPoints() == null ? 0 : record.getInviterPoints();
            int inviteePoints = record.getInviteePoints() == null ? 0 : record.getInviteePoints();
            record.setStatus(InviteStatusEnum.REVOKED.getValue());
            record.setRewardTime(LocalDateTime.now());
            record.setInviterPoints(Math.max(0, inviterPoints - PointsConstants.INVITE_REGISTER_INVITER_REWARD));
            record.setInviteePoints(Math.max(0, inviteePoints - PointsConstants.INVITE_REGISTER_INVITEE_REWARD));
            boolean updated = this.updateById(record);
            if (!updated) {
                log.warn("邀请奖励回收后更新记录状态失败，记录ID: {}", record.getId());
            }
            if (deductionSucceeded) {
                revokedCount++;
            }
        }

        return revokedCount;
    }

    @Override
    public List<InviteRecord> getUserInviteRecords(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviterId, userId)
                .gt(InviteRecord::getInviteeId, 0)
                .orderBy(InviteRecord::getCreateTime, false);

        return this.list(queryWrapper);
    }

    @Override
    public String getUserInviteCode(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        // 优先返回占位邀请码
        QueryWrapper pendingWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviterId, userId)
                .eq(InviteRecord::getStatus, InviteStatusEnum.PENDING.getValue())
                .orderBy(InviteRecord::getCreateTime, false)
                .limit(1);

        InviteRecord pendingRecord = this.getOne(pendingWrapper);
        if (pendingRecord != null && pendingRecord.getInviteCode() != null) {
            return pendingRecord.getInviteCode();
        }

        // 兼容旧数据：返回最近一次邀请使用的邀请码
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(InviteRecord::getInviterId, userId)
                .isNotNull(InviteRecord::getInviteCode)
                .orderBy(InviteRecord::getCreateTime, false)
                .limit(1);

        InviteRecord existingRecord = this.getOne(queryWrapper);
        if (existingRecord != null && existingRecord.getInviteCode() != null) {
            return existingRecord.getInviteCode();
        }

        // 如果没有，生成一个新的
        String inviteCode = generateInviteCode(userId);

        // 保存邀请码记录（只是为了保存邀请码，不是完整的邀请关系）
        InviteRecord record = InviteRecord.builder()
                .inviterId(userId)
                .inviteeId(0L) // 占位，表示还没有被邀请人
                .inviteCode(inviteCode)
                .status(InviteStatusEnum.PENDING.getValue())
                .build();
        this.save(record);

        return inviteCode;
    }

    /**
     * 为邀请记录生成避免冲突的唯一邀请码
     */
    private String generateUniqueInviteRecordCode(String baseInviteCode) {
        String candidate;
        int attempts = 0;
        final int maxAttempts = 10;

        do {
            if (attempts == 0) {
                candidate = baseInviteCode;
            } else {
                candidate = baseInviteCode + "-" + RandomUtil.randomString(6).toUpperCase();
            }

            QueryWrapper wrapper = QueryWrapper.create()
                    .eq(InviteRecord::getInviteCode, candidate);
            if (this.count(wrapper) == 0) {
                return candidate;
            }

            attempts++;
        } while (attempts < maxAttempts);

        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成邀请记录编码失败，请稍后重试");
    }

}
