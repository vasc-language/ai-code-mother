<template>
  <div id="userResetPasswordPage">
    <div class="reset-background">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>

    <div class="reset-container">
      <div class="reset-card">
        <div class="reset-header">
          <div class="logo-section">
            <h1 class="brand-title">AI 应用生成</h1>
          </div>
          <h2 class="page-title">重置密码</h2>
          <p class="page-subtitle">通过邮箱验证码重置您的账户密码</p>
        </div>

        <div class="reset-form">
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
            <a-form-item
              name="userEmail"
              :rules="[
                { required: true, message: '请输入您的邮箱' },
                { type: 'email', message: '请输入正确的邮箱格式' }
              ]"
            >
              <a-input
                v-model:value="formState.userEmail"
                placeholder="注册邮箱"
                size="large"
              />
            </a-form-item>

            <a-form-item
              name="emailCode"
              :rules="[{ required: true, message: '请输入验证码' }]"
            >
              <div style="display: flex; gap: 12px;">
                <a-input
                  v-model:value="formState.emailCode"
                  placeholder="邮箱验证码"
                  size="large"
                  style="flex: 1;"
                />
                <a-button
                  size="large"
                  :disabled="sendCodeDisabled"
                  :loading="isSendingCode"
                  @click="sendEmailCode"
                  class="send-code-btn"
                >
                  {{ sendCodeText }}
                </a-button>
              </div>
            </a-form-item>

            <a-form-item
              name="newPassword"
              :rules="[
                { required: true, message: '请输入新密码' },
                { min: 8, max: 20, message: '密码长度必须在8-20位之间' },
              ]"
            >
              <a-input-password
                v-model:value="formState.newPassword"
                placeholder="新密码（8-20位）"
                size="large"
              />
            </a-form-item>

            <a-form-item
              name="checkPassword"
              :rules="[
                { required: true, message: '请确认您的新密码' },
                { min: 8, max: 20, message: '密码长度必须在8-20位之间' },
                { validator: validateCheckPassword },
              ]"
            >
              <a-input-password
                v-model:value="formState.checkPassword"
                placeholder="确认新密码"
                size="large"
              />
            </a-form-item>

            <a-form-item class="submit-item">
              <a-button type="primary" html-type="submit" size="large" class="reset-btn">
                <span class="btn-text">重置密码</span>
                <span class="btn-icon">🔑</span>
              </a-button>
            </a-form-item>
          </a-form>

          <div class="reset-footer">
            <div class="login-link">
              <span>想起密码了？</span>
              <RouterLink to="/user/login" class="link">返回登录</RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { reactive, ref, computed } from 'vue'
import request from '@/request'

const router = useRouter()

const formState = reactive({
  userEmail: '',
  emailCode: '',
  newPassword: '',
  checkPassword: '',
})

// 验证码发送相关状态
const sendCodeDisabled = ref(false)
const isSendingCode = ref(false)
const countdown = ref(0)

const sendCodeText = computed(() => {
  return countdown.value > 0 ? `${countdown.value}秒后重发` : '发送验证码'
})

/**
 * 发送邮箱验证码
 */
const sendEmailCode = async () => {
  // 校验邮箱
  if (!formState.userEmail) {
    message.error('请先输入邮箱')
    return
  }

  const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
  if (!emailRegex.test(formState.userEmail)) {
    message.error('请输入正确的邮箱格式')
    return
  }

  try {
    sendCodeDisabled.value = true
    isSendingCode.value = true
    const res = await request.post('/user/email/send', {
      email: formState.userEmail,
      type: 'RESET_PASSWORD'
    })

    if (res.data.code === 0) {
      message.success('验证码已发送，请查收邮件')
      // 开始倒计时60秒
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
          sendCodeDisabled.value = false
        }
      }, 1000)
    } else {
      message.error('发送失败：' + res.data.message)
      sendCodeDisabled.value = false
    }
  } catch (error: any) {
    message.error('发送失败：' + (error.message || '网络错误'))
    sendCodeDisabled.value = false
  } finally {
    isSendingCode.value = false
  }
}

/**
 * 验证确认密码
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    const res = await request.post('/user/reset-password', formState)

    if (res.data.code === 0) {
      message.success('密码重置成功，请使用新密码登录')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('重置失败：' + res.data.message)
    }
  } catch (error: any) {
    message.error('重置失败：' + (error.message || '网络错误'))
  }
}
</script>

<style scoped>
#userResetPasswordPage {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

/* 动态背景 */
.reset-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: transparent;
}

/* 浮动装饰元素 */
.bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.shape {
  position: absolute;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  backdrop-filter: blur(10px);
  animation: float 7s ease-in-out infinite;
}

.shape-1 {
  width: 160px;
  height: 160px;
  top: 8%;
  left: 8%;
  animation-delay: 0s;
}

.shape-2 {
  width: 110px;
  height: 110px;
  top: 65%;
  right: 12%;
  animation-delay: 1.8s;
}

.shape-3 {
  width: 90px;
  height: 90px;
  top: 18%;
  right: 28%;
  animation-delay: 3.2s;
}

.shape-4 {
  width: 130px;
  height: 130px;
  bottom: 18%;
  left: 18%;
  animation-delay: 0.8s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg) scale(1); }
  25% { transform: translateY(-18px) rotate(90deg) scale(1.08); }
  50% { transform: translateY(8px) rotate(180deg) scale(0.92); }
  75% { transform: translateY(-10px) rotate(270deg) scale(1.04); }
}

/* 重置容器 */
.reset-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

/* 重置卡片 */
.reset-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 26px;
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.1), 0 16px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  animation: cardAppear 0.9s ease-out;
  transition: all 0.3s ease;
}

.reset-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 44px 88px rgba(0, 0, 0, 0.14), 0 22px 44px rgba(0, 0, 0, 0.1);
}

@keyframes cardAppear {
  0% {
    opacity: 0;
    transform: translateY(45px) scale(0.92);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 重置头部 */
.reset-header {
  text-align: center;
  padding: 40px 34px 32px;
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.1) 0%, rgba(239, 68, 68, 0.1) 100%);
  border-bottom: 1px solid rgba(251, 146, 60, 0.12);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 13px;
  margin-bottom: 26px;
}

.brand-title {
  font-size: 29px;
  font-weight: 700;
  background: linear-gradient(135deg, #fb923c 0%, #ef4444 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.02em;
}

.page-title {
  font-size: 33px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 9px 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 16.5px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
  font-weight: 400;
}

/* 重置表单 */
.reset-form {
  padding: 34px;
}

/* 表单项样式覆盖 */
:deep(.ant-form-item) {
  margin-bottom: 22px;
}

:deep(.ant-input) {
  border-radius: 13px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  padding: 0 17px;
  font-size: 16px;
  height: 52px;
  display: flex;
  align-items: center;
}

:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #fb923c;
  box-shadow: 0 0 0 4px rgba(251, 146, 60, 0.13);
}

:deep(.ant-input-password) {
  border-radius: 13px;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 13px !important;
  border: 2px solid #e5e7eb !important;
  transition: all 0.3s ease;
  padding: 0 17px !important;
  height: 52px !important;
  display: flex;
  align-items: center;
  font-size: 16px;
}

:deep(.ant-input-affix-wrapper .ant-input) {
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
  height: auto !important;
  background: transparent;
  font-size: 16px !important;
}

:deep(.ant-input-affix-wrapper .ant-input:focus),
:deep(.ant-input-affix-wrapper .ant-input-focused) {
  border: none !important;
  box-shadow: none !important;
}

:deep(.ant-input-affix-wrapper:focus),
:deep(.ant-input-affix-wrapper-focused) {
  border-color: #fb923c !important;
  box-shadow: 0 0 0 4px rgba(251, 146, 60, 0.13) !important;
}

/* 覆盖错误状态样式 */
:deep(.ant-form-item-has-error .ant-input-affix-wrapper) {
  border-color: #e5e7eb !important;
}

:deep(.ant-form-item-has-error .ant-input-affix-wrapper:focus),
:deep(.ant-form-item-has-error .ant-input-affix-wrapper-focused) {
  border-color: #fb923c !important;
  box-shadow: 0 0 0 4px rgba(251, 146, 60, 0.13) !important;
}

.send-code-btn {
  width: 120px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
  border: 1px solid rgba(251, 146, 60, 0.28);
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.16) 0%, rgba(239, 68, 68, 0.16) 100%);
  color: rgba(234, 88, 12, 0.75);
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  box-shadow: 0 12px 24px rgba(251, 146, 60, 0.1);
  backdrop-filter: blur(6px);
}

.send-code-btn:hover,
.send-code-btn:focus {
  border-color: rgba(251, 146, 60, 0.45);
  background: linear-gradient(135deg, rgba(251, 146, 60, 0.22) 0%, rgba(239, 68, 68, 0.22) 100%);
  color: rgba(194, 65, 12, 0.88);
  box-shadow: 0 16px 32px rgba(251, 146, 60, 0.16);
}

.send-code-btn:active {
  transform: translateY(1px);
  box-shadow: 0 8px 18px rgba(251, 146, 60, 0.14);
}

.send-code-btn[disabled] {
  border-color: rgba(148, 163, 184, 0.4);
  background: rgba(241, 245, 255, 0.9);
  color: rgba(148, 163, 184, 0.9);
  box-shadow: none;
  cursor: not-allowed;
}

/* 提交按钮 */
.submit-item {
  margin-bottom: 0 !important;
  margin-top: 30px !important;
}

.reset-btn {
  width: 100%;
  height: 58px;
  border-radius: 17px;
  background: linear-gradient(135deg, #fb923c 0%, #ef4444 100%);
  border: none;
  font-size: 18.5px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.reset-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.23) 0%, transparent 100%);
  transition: left 0.55s ease;
}

.reset-btn:hover::before {
  left: 100%;
}

.reset-btn:hover {
  transform: translateY(-2.5px);
  box-shadow: 0 18px 36px rgba(251, 146, 60, 0.38);
}

.reset-btn:active {
  transform: translateY(-0.5px);
}

.btn-text {
  transition: transform 0.3s ease;
}

.btn-icon {
  transition: transform 0.3s ease;
  font-size: 21px;
}

.reset-btn:hover .btn-text {
  transform: translateX(-5px);
}

.reset-btn:hover .btn-icon {
  transform: translateX(5px) rotate(15deg);
}

/* 页脚链接 */
.reset-footer {
  padding-top: 26px;
  border-top: 1px solid #f3f4f6;
  text-align: center;
}

.login-link {
  font-size: 14.5px;
  color: #6b7280;
}

.login-link .link {
  color: #fb923c;
  text-decoration: none;
  font-weight: 600;
  margin-left: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.login-link .link::after {
  content: '';
  position: absolute;
  width: 0;
  height: 2px;
  bottom: -2px;
  left: 0;
  background: linear-gradient(135deg, #fb923c 0%, #ef4444 100%);
  transition: width 0.3s ease;
}

.login-link .link:hover::after {
  width: 100%;
}

.login-link .link:hover {
  color: #ef4444;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .reset-container {
    padding: 16px;
    max-width: 370px;
  }

  .reset-card {
    border-radius: 22px;
  }

  .reset-header {
    padding: 34px 26px 26px;
  }

  .brand-title {
    font-size: 25px;
  }

  .page-title {
    font-size: 29px;
  }

  .page-subtitle {
    font-size: 14.5px;
  }

  .reset-form {
    padding: 26px;
  }

  .reset-btn {
    height: 54px;
    font-size: 16.5px;
  }
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .reset-card {
    background: rgba(17, 17, 17, 0.95);
    color: white;
  }

  .page-title {
    color: white;
  }

  .page-subtitle {
    color: #d1d5db;
  }

  .login-link {
    color: #9ca3af;
  }

  .reset-header {
    border-bottom: 1px solid rgba(251, 146, 60, 0.18);
  }

  .reset-footer {
    border-top: 1px solid #374151;
  }
}
</style>
