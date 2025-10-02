package com.spring.aicodemother.constants;

/**
 * 积分系统常量
 *
 * @author system
 */
public class PointsConstants {

    /**
     * 积分兑换比例：1积分 = 1000 Token
     */
    public static final int TOKENS_PER_POINT = 1000;

    /**
     * 生成应用预扣积分
     */
    public static final int GENERATE_PRE_DEDUCT = 6;

    /**
     * 首次生成奖励积分
     */
    public static final int FIRST_GENERATE_REWARD = 30;

    /**
     * 新用户注册奖励积分
     */
    public static final int REGISTER_REWARD = 30;

    /**
     * 签到基础奖励积分
     */
    public static final int SIGN_IN_BASE = 5;

    /**
     * 连续签到3天额外奖励
     */
    public static final int SIGN_IN_3_DAYS_BONUS = 3;

    /**
     * 连续签到7天额外奖励
     */
    public static final int SIGN_IN_7_DAYS_BONUS = 10;

    /**
     * 连续签到30天额外奖励
     */
    public static final int SIGN_IN_30_DAYS_BONUS = 50;

    /**
     * 邀请用户注册 - 邀请人奖励
     */
    public static final int INVITE_REGISTER_INVITER_REWARD = 20;

    /**
     * 邀请用户注册 - 被邀请人奖励
     */
    public static final int INVITE_REGISTER_INVITEE_REWARD = 20;

    /**
     * 邀请用户首次生成 - 邀请人奖励
     */
    public static final int INVITE_FIRST_GEN_INVITER_REWARD = 30;

    /**
     * 邀请用户首次生成 - 被邀请人奖励
     */
    public static final int INVITE_FIRST_GEN_INVITEE_REWARD = 10;

    /**
     * 无效生成惩罚积分
     */
    public static final int INVALID_GENERATION_PENALTY = 10;

    /**
     * 积分有效期（天数）
     */
    public static final int POINTS_VALIDITY_DAYS = 90;

    /**
     * 积分到期提醒提前天数
     */
    public static final int POINTS_EXPIRE_REMIND_DAYS = 7;

    /**
     * 单日Token消耗上限
     */
    public static final int DAILY_TOKEN_LIMIT = 120000;

    /**
     * 单日生成次数上限
     */
    public static final int DAILY_GENERATION_LIMIT = 30;

    /**
     * 单日邀请奖励次数上限
     */
    public static final int DAILY_INVITE_REWARD_LIMIT = 3;

    /**
     * 邀请防刷检测窗口期（天数）
     */
    public static final int INVITE_ABUSE_CHECK_DAYS = 7;

    /**
     * 邀请奖励回收宽限期（天数）
     */
    public static final int INVITE_REWARD_GRACE_DAYS = 7;

    /**
     * 积分操作分布式锁超时时间（秒）
     */
    public static final int POINTS_LOCK_TIMEOUT_SECONDS = 10;

    /**
     * IP级别限流 - 每分钟请求次数
     */
    public static final int IP_RATE_LIMIT_PER_MINUTE = 10;

    /**
     * IP级别限流 - 时间窗口（秒）
     */
    public static final int IP_RATE_LIMIT_WINDOW_SECONDS = 60;

    private PointsConstants() {
        // 私有构造函数，防止实例化
    }
}
