<template>
  <div id="userLoginPage">
    <div class="login-background">
      <div class="bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>

    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <div class="logo-section">
            <h1 class="brand-title">AI 应用生成</h1>
          </div>
          <h2 class="page-title">欢迎回来</h2>
          <p class="page-subtitle">登录您的账户，开始创造之旅</p>
        </div>

        <div class="login-form">
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
                placeholder="邮箱"
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
                placeholder="密码"
                size="large"
              />
            </a-form-item>

            <div class="form-options">
              <RouterLink to="/user/reset-password" class="forgot-password">忘记密码?</RouterLink>
            </div>

            <a-form-item class="submit-item">
              <a-button type="primary" html-type="submit" size="large" class="login-btn">
                <span class="btn-text">登录</span>
                <span class="btn-arrow">→</span>
              </a-button>
            </a-form-item>
          </a-form>

          <div class="login-footer">
            <div class="register-link">
              <span>还没有账号？</span>
              <RouterLink to="/user/register" class="link">立即注册</RouterLink>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userEmail: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

/* 动态背景 */
.login-background {
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
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  backdrop-filter: blur(10px);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 70%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  top: 20%;
  right: 25%;
  animation-delay: 4s;
}

.shape-4 {
  width: 120px;
  height: 120px;
  bottom: 20%;
  left: 20%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-20px) rotate(120deg); }
  66% { transform: translateY(10px) rotate(240deg); }
}

/* 登录容器 */
.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

/* 登录卡片 */
.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.1), 0 16px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
  animation: cardAppear 0.8s ease-out;
  transition: all 0.3s ease;
}

.login-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 40px 80px rgba(0, 0, 0, 0.15), 0 20px 40px rgba(0, 0, 0, 0.1);
}

@keyframes cardAppear {
  0% {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 登录头部 */
.login-header {
  text-align: center;
  padding: 40px 32px 32px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(168, 85, 247, 0.1) 100%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.1);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 48px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-10px); }
  60% { transform: translateY(-5px); }
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.02em;
}

.page-title {
  font-size: 32px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

/* 登录表单 */
.login-form {
  padding: 32px;
}

/* 表单项样式覆盖 */
:deep(.ant-form-item) {
  margin-bottom: 24px;
}

:deep(.ant-input) {
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  padding: 0 16px;
  font-size: 16px;
  height: 52px;
  display: flex;
  align-items: center;
}

:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

:deep(.ant-input-password) {
  border-radius: 12px;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 12px !important;
  border: 2px solid #e5e7eb !important;
  transition: all 0.3s ease;
  padding: 0 16px !important;
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
  border-color: #667eea !important;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1) !important;
}

/* 覆盖错误状态样式 */
:deep(.ant-form-item-has-error .ant-input-affix-wrapper) {
  border-color: #e5e7eb !important;
}

:deep(.ant-form-item-has-error .ant-input-affix-wrapper:focus),
:deep(.ant-form-item-has-error .ant-input-affix-wrapper-focused) {
  border-color: #667eea !important;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1) !important;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
}

.forgot-password::after {
  content: '';
  position: absolute;
  width: 0;
  height: 1.5px;
  bottom: -2px;
  left: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

.forgot-password:hover {
  color: #764ba2;
}

.forgot-password:hover::after {
  width: 100%;
}


/* 提交按钮 */
.submit-item {
  margin-bottom: 0 !important;
  margin-top: 40px !important;
}

.login-btn {
  width: 100%;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, transparent 100%);
  transition: left 0.5s ease;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(102, 126, 234, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.btn-text {
  transition: transform 0.3s ease;
}

.btn-arrow {
  transition: transform 0.3s ease;
  font-size: 20px;
}

.login-btn:hover .btn-text {
  transform: translateX(-4px);
}

.login-btn:hover .btn-arrow {
  transform: translateX(4px);
}

/* 页脚链接 */
.login-footer {
  padding-top: 24px;
  border-top: 1px solid #f3f4f6;
  text-align: center;
}

.register-link {
  font-size: 14px;
  color: #6b7280;
}

.register-link .link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.register-link .link::after {
  content: '';
  position: absolute;
  width: 0;
  height: 2px;
  bottom: -2px;
  left: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;
}

.register-link .link:hover::after {
  width: 100%;
}

.register-link .link:hover {
  color: #764ba2;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 16px;
    max-width: 360px;
  }

  .login-card {
    border-radius: 20px;
  }

  .login-header {
    padding: 32px 24px 24px;
  }

  .brand-title {
    font-size: 24px;
  }

  .page-title {
    font-size: 28px;
  }

  .page-subtitle {
    font-size: 14px;
  }

  .login-form {
    padding: 24px;
  }

  .login-btn {
    height: 52px;
    font-size: 16px;
  }
}

/* 暗色模式适配 */
@media (prefers-color-scheme: dark) {
  .login-card {
    background: rgba(17, 17, 17, 0.95);
    color: white;
  }

  .page-title {
    color: white;
  }

  .page-subtitle {
    color: #d1d5db;
  }


  .register-link {
    color: #9ca3af;
  }
}
</style>
