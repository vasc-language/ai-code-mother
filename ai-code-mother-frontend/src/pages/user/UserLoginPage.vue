<!--
  用户登录页面组件
  提供用户账号密码登录功能，包括表单验证和状态管理
-->
<template>
  <div id="userLoginPage">
    <h2 class="title">AI 应用生成 - 用户登录</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        没有账号
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

/**
 * 登录表单状态
 * 包含用户账号和密码字段
 */
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 处理登录表单提交。
 * 当用户点击登录按钮并且表单验证通过后，此函数会被 antd-vue 的 a-form 组件自动调用。
 * @param values - 经过 antd-vue 表单验证后的数据对象，包含了 userAccount 和 userPassword。
 */
const handleSubmit = async (values: any) => {
  // 1. 调用后端 API
  // 使用从 @/api/userController.ts 导入的 userLogin 函数，
  // 将用户输入的表单数据 (values) 作为参数，发送异步登录请求。
  const res = await userLogin(values)

  // 2. 处理登录结果
  // 后端返回的数据结构中，通常用 code === 0 表示业务处理成功。
  // 同时检查 res.data.data 是否存在，确保返回了有效的用户数据。
  if (res.data.code === 0 && res.data.data) {
    // 2.1. 登录成功
    // a. 更新全局用户状态
    //    调用 Pinia store (useLoginUserStore) 中的 fetchLoginUser action。
    //    这个 action 会重新从后端获取当前登录用户的详细信息，并存储到全局状态中，
    //    以便在应用的其他页面（如导航栏、个人中心）中展示用户信息。
    await loginUserStore.fetchLoginUser()

    // b. 提示用户
    //    使用 antd-vue 的 message 组件显示一个成功的提示。
    message.success('登录成功')

    // c. 页面跳转
    //    使用 vue-router 跳转到首页 ('/')。
    //    `replace: true` 表示这次跳转不会在浏览器历史记录中留下新条目，
    //    用户点击“后退”按钮时不会回到登录页。
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    // 2.2. 登录失败
    //    如果 code 不为 0 或其他错误情况，
    //    使用 antd-vue 的 message 组件显示错误提示，
    //    错误信息 (res.data.message) 来自后端返回的数据。
    message.error('登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  background: white;
  max-width: 720px;
  padding: 24px;
  margin: 24px auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  text-align: right;
  color: #bbb;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
