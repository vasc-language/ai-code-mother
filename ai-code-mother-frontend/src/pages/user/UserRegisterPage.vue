<template>
  <div id="userRegisterPage">
    <div class="register-background">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
        <div class="shape shape-5"></div>
      </div>
    </div>

    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <div class="logo-section">
            <h1 class="brand-title">AI 应用生成</h1>
          </div>
          <h2 class="page-title">创建账户</h2>
          <p class="page-subtitle">加入我们，开启AI应用创造之旅</p>
        </div>

        <div class="register-form">
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
            <a-form-item
              name="userAccount"
              :rules="[{ required: true, message: '请输入您的账号' }]"
            >
              <a-input
                v-model:value="formState.userAccount"
                placeholder="请输入账号"
                size="large"
              />
            </a-form-item>

            <a-form-item
              name="userPassword"
              :rules="[
                { required: true, message: '请输入您的密码' },
                { min: 8, message: '密码长度不能小于 8 位' },
              ]"
            >
              <a-input-password
                v-model:value="formState.userPassword"
                placeholder="请输入密码"
                size="large"
              />
            </a-form-item>

            <a-form-item
              name="checkPassword"
              :rules="[
                { required: true, message: '请确认您的密码' },
                { min: 8, message: '密码长度不能小于 8 位' },
                { validator: validateCheckPassword },
              ]"
            >
              <a-input-password
                v-model:value="formState.checkPassword"
                placeholder="请再次输入密码"
                size="large"
              />
            </a-form-item>

            <a-form-item class="submit-item">
              <a-button type="primary" html-type="submit" size="large" class="register-btn">
                <span class="btn-text">立即注册</span>
                <span class="btn-sparkle">✨</span>
              </a-button>
            </a-form-item>
          </a-form>

          <div class="register-footer">
            <div class="login-link">
              <span>已有账号？</span>
              <RouterLink to="/user/login" class="link">立即登录</RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'

const router = useRouter()
const route = useRoute()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  inviteCode: '',
})

const queryInviteCode = route.query.inviteCode
if (typeof queryInviteCode === 'string') {
  formState.inviteCode = queryInviteCode
}

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const payload: API.UserRegisterRequest = {
    ...values,
    inviteCode: formState.inviteCode || undefined,
  }
  const res = await userRegister(payload)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

/* 动态背景 */
.register-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg,
    #f093fb 0%,
    #f5576c 25%,
    #4facfe 50%,
    #00f2fe 75%,
    #667eea 100%
  );
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
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
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  backdrop-filter: blur(10px);
  animation: float 8s ease-in-out infinite;
}

.shape-1 {
  width: 180px;
  height: 180px;
  top: 5%;
  left: 5%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  top: 60%;
  right: 10%;
  animation-delay: 1.5s;
}

.shape-3 {
  width: 80px;
  height: 80px;
  top: 15%;
  right: 30%;
  animation-delay: 3s;
}

.shape-4 {
  width: 140px;
  height: 140px;
  bottom: 15%;
  left: 15%;
  animation-delay: 0.5s;
}

.shape-5 {
  width: 60px;
  height: 60px;
  top: 35%;
  left: 70%;
  animation-delay: 2.5s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg) scale(1); }
  25% { transform: translateY(-15px) rotate(90deg) scale(1.1); }
  50% { transform: translateY(5px) rotate(180deg) scale(0.9); }
  75% { transform: translateY(-8px) rotate(270deg) scale(1.05); }
}

/* 注册容器 */
.register-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 450px;
  padding: 20px;
}

/* 注册卡片 */
.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 28px;
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.1), 0 16px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  animation: cardAppear 1s ease-out;
  transition: all 0.3s ease;
}

.register-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 48px 96px rgba(0, 0, 0, 0.15), 0 24px 48px rgba(0, 0, 0, 0.1);
}

@keyframes cardAppear {
  0% {
    opacity: 0;
    transform: translateY(50px) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 注册头部 */
.register-header {
  text-align: center;
  padding: 42px 36px 36px;
  background: linear-gradient(135deg, rgba(240, 147, 251, 0.1) 0%, rgba(245, 87, 108, 0.1) 100%);
  border-bottom: 1px solid rgba(240, 147, 251, 0.15);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-bottom: 28px;
}

.logo-icon {
  font-size: 52px;
  animation: sparkle 3s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(240, 147, 251, 0.3));
}

@keyframes sparkle {
  0%, 100% {
    transform: rotate(0deg) scale(1);
    filter: drop-shadow(0 4px 8px rgba(240, 147, 251, 0.3));
  }
  25% {
    transform: rotate(-5deg) scale(1.1);
    filter: drop-shadow(0 6px 12px rgba(240, 147, 251, 0.4));
  }
  50% {
    transform: rotate(5deg) scale(1.05);
    filter: drop-shadow(0 8px 16px rgba(240, 147, 251, 0.5));
  }
  75% {
    transform: rotate(-3deg) scale(1.08);
    filter: drop-shadow(0 6px 12px rgba(240, 147, 251, 0.4));
  }
}

.brand-title {
  font-size: 30px;
  font-weight: 700;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 50%, #4facfe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.02em;
}

.page-title {
  font-size: 34px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 10px 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 17px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
  font-weight: 400;
}

/* 注册表单 */
.register-form {
  padding: 36px;
}

/* 表单项样式覆盖 */
:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-input) {
  border-radius: 14px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  padding: 0 18px;
  font-size: 16px;
  height: 52px;
  display: flex;
  align-items: center;
}

:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #f093fb;
  box-shadow: 0 0 0 4px rgba(240, 147, 251, 0.15);
}

:deep(.ant-input-password) {
  border-radius: 14px;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 14px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  padding: 0 18px;
  height: 52px;
  display: flex;
  align-items: center;
}

:deep(.ant-input-affix-wrapper .ant-input) {
  border: none;
  box-shadow: none;
  padding: 0;
  height: auto;
  background: transparent;
}

:deep(.ant-input-affix-wrapper:focus),
:deep(.ant-input-affix-wrapper-focused) {
  border-color: #f093fb;
  box-shadow: 0 0 0 4px rgba(240, 147, 251, 0.15);
}


/* 提交按钮 */
.submit-item {
  margin-bottom: 0 !important;
  margin-top: 32px !important;
}

.register-btn {
  width: 100%;
  height: 60px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 50%, #4facfe 100%);
  border: none;
  font-size: 19px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25) 0%, transparent 100%);
  transition: left 0.6s ease;
}

.register-btn:hover::before {
  left: 100%;
}

.register-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 20px 40px rgba(240, 147, 251, 0.4);
}

.register-btn:active {
  transform: translateY(-1px);
}

.btn-text {
  transition: transform 0.3s ease;
}

.btn-sparkle {
  transition: transform 0.3s ease;
  font-size: 22px;
  animation: sparkle 2s ease-in-out infinite;
}

.register-btn:hover .btn-text {
  transform: translateX(-6px);
}

.register-btn:hover .btn-sparkle {
  transform: translateX(6px) rotate(180deg);
}

/* 页脚链接 */
.register-footer {
  padding-top: 28px;
  border-top: 1px solid #f3f4f6;
  text-align: center;
}

.login-link {
  font-size: 15px;
  color: #6b7280;
}

.login-link .link {
  color: #f093fb;
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
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  transition: width 0.3s ease;
}

.login-link .link:hover::after {
  width: 100%;
}

.login-link .link:hover {
  color: #f5576c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    padding: 16px;
    max-width: 380px;
  }

  .register-card {
    border-radius: 24px;
  }

  .register-header {
    padding: 36px 28px 28px;
  }

  .brand-title {
    font-size: 26px;
  }

  .page-title {
    font-size: 30px;
  }

  .page-subtitle {
    font-size: 15px;
  }

  .register-form {
    padding: 28px;
  }

  .register-btn {
    height: 56px;
    font-size: 17px;
  }

  .logo-icon {
    font-size: 48px;
  }
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .register-card {
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

  .register-header {
    border-bottom: 1px solid rgba(240, 147, 251, 0.2);
  }

  .register-footer {
    border-top: 1px solid #374151;
  }
}
</style>
