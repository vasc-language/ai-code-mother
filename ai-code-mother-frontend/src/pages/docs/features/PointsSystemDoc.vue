<template>
  <div class="doc-page">
    <h1 class="doc-title">积分系统</h1>
    <p class="doc-intro">
      完善的积分机制,让你通过多种方式获取免费积分,持续使用 AI 代码生成服务。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <WalletOutlined class="title-icon" />
        积分获取方式
      </h2>

      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :md="12">
          <a-card class="points-card" hoverable>
            <template #title>
              <GiftOutlined /> 新用户注册
            </template>
            <div class="points-amount">100 积分</div>
            <p>注册成功即可获得,无需其他操作</p>
            <a-tag color="green">一次性</a-tag>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card class="points-card" hoverable>
            <template #title>
              <CalendarOutlined /> 每日签到
            </template>
            <div class="points-amount">5 积分</div>
            <p>每天签到一次,连续签到有额外奖励</p>
            <a-tag color="blue">每日</a-tag>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card class="points-card" hoverable>
            <template #title>
              <TeamOutlined /> 邀请好友
            </template>
            <div class="points-amount">50 积分</div>
            <p>每邀请一位好友注册,双方各得 50 积分</p>
            <a-tag color="purple">不限次数</a-tag>
          </a-card>
        </a-col>

        <a-col :xs="24" :md="12">
          <a-card class="points-card" hoverable>
            <template #title>
              <TrophyOutlined /> 完成任务
            </template>
            <div class="points-amount">不定</div>
            <p>参与平台活动,完成指定任务获得奖励</p>
            <a-tag color="orange">活动</a-tag>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ThunderboltOutlined class="title-icon" />
        积分消耗规则
      </h2>

      <a-table :dataSource="pointsUsage" :columns="usageColumns" :pagination="false" bordered>
      </a-table>

      <a-alert
        message="温馨提示"
        type="info"
        show-icon
        style="margin-top: 16px;"
      >
        <template #description>
          积分消耗在生成开始时扣除,生成失败会自动退还积分到账户
        </template>
      </a-alert>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <LineChartOutlined class="title-icon" />
        积分明细查询
      </h2>

      <p class="section-content">
        登录后点击右上角用户头像,选择"积分明细"可以查看:
      </p>
      <ul class="content-list">
        <li>当前积分余额</li>
        <li>积分获取记录(时间、来源、数量)</li>
        <li>积分消耗记录(时间、用途、数量)</li>
        <li>积分变动趋势图表</li>
      </ul>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <QuestionCircleOutlined class="title-icon" />
        常见问题
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="积分会过期吗?">
          <p>积分自获得之日起 90 天内有效。过期前 7 天系统会提醒你,请及时使用。</p>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="生成失败积分会退还吗?">
          <p>是的,如果生成失败(如网络错误、服务器异常等),已扣除的积分会自动退还到你的账户。</p>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="可以购买积分吗?">
          <p>目前暂不支持购买积分,请通过每日签到和邀请好友等方式获取。</p>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="邀请好友有限制吗?">
          <p>没有次数限制,但需要确保被邀请人是真实用户且成功注册。恶意刷邀请会被系统检测并封禁账号。</p>
        </a-collapse-panel>
      </a-collapse>
    </section>
  </div>
</template>

<script setup lang="ts">
import {
  WalletOutlined,
  GiftOutlined,
  CalendarOutlined,
  TeamOutlined,
  TrophyOutlined,
  ThunderboltOutlined,
  LineChartOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue'

const pointsUsage = [
  { type: 'Vue 3 应用', points: 15, time: '约 15 分钟', description: '完整 Vue 3 项目,包含组件、路由等' },
]

const usageColumns = [
  { title: '生成类型', dataIndex: 'type', key: 'type' },
  { title: '消耗积分', dataIndex: 'points', key: 'points' },
  { title: '生成时间', dataIndex: 'time', key: 'time' },
  { title: '说明', dataIndex: 'description', key: 'description' },
]
</script>

<style scoped>
@import './doc-common.css';

.points-card {
  text-align: center;
  border-left: 3px solid #52c41a;
}

.points-amount {
  font-size: 32px;
  font-weight: 700;
  color: #52c41a;
  margin: 16px 0;
}

.points-card p {
  color: #666;
  margin: 12px 0;
}
</style>
