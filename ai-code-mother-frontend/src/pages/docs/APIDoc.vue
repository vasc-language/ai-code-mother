<template>
  <div class="doc-page">
    <h1 class="doc-title">API 文档</h1>
    <p class="doc-intro">
      后端 API 接口文档,用于了解系统接口和进行二次开发。
    </p>

    <a-divider />

    <section class="doc-section">
      <h2 class="section-title">
        <ApiOutlined class="title-icon" />
        基础信息
      </h2>

      <a-descriptions bordered :column="1">
        <a-descriptions-item label="API 基础地址">
          <a-tag color="blue">{{ API_HOST }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="在线文档">
          <a :href="`${API_HOST}/doc.html`" target="_blank">
            Swagger UI (Knife4j)
          </a>
        </a-descriptions-item>
        <a-descriptions-item label="OpenAPI 规范">
          <a :href="`${API_HOST}/v3/api-docs`" target="_blank">
            {{ API_HOST }}/v3/api-docs
          </a>
        </a-descriptions-item>
        <a-descriptions-item label="请求格式">
          application/json
        </a-descriptions-item>
        <a-descriptions-item label="响应格式">
          application/json
        </a-descriptions-item>
      </a-descriptions>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <CodeOutlined class="title-icon" />
        统一响应格式
      </h2>

      <p class="section-content">
        所有 API 接口统一使用 <code>BaseResponse&lt;T&gt;</code> 响应格式:
      </p>

      <pre class="code-block">{
  "code": 0,           // 状态码:0-成功,其他-错误
  "data": {},          // 响应数据
  "message": "ok"      // 状态消息
}</pre>

      <h3 style="margin-top: 24px;">错误码说明</h3>
      <a-table :dataSource="errorCodes" :columns="errorCodeColumns" :pagination="false" size="small" bordered>
      </a-table>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <UserOutlined class="title-icon" />
        用户接口
      </h2>

      <a-collapse :bordered="false" default-active-key="['1']">
        <a-collapse-panel key="1" header="POST /user/register - 用户注册">
          <h4>请求参数</h4>
          <pre class="code-block">{
  "userAccount": "username",
  "userPassword": "password",
  "checkPassword": "password"
}</pre>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": 123456,      // 用户ID
  "message": "ok"
}</pre>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="POST /user/login - 用户登录">
          <h4>请求参数</h4>
          <pre class="code-block">{
  "userAccount": "username",
  "userPassword": "password"
}</pre>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": {
    "id": 123456,
    "userName": "用户名",
    "userAvatar": "头像URL",
    "userRole": "user",
    "points": 100
  },
  "message": "ok"
}</pre>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="POST /user/logout - 用户登出">
          <h4>请求参数</h4>
          <p>无</p>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": true,
  "message": "ok"
}</pre>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="GET /user/get/login - 获取登录用户">
          <h4>请求参数</h4>
          <p>无(通过 Session)</p>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": {
    "id": 123456,
    "userName": "用户名",
    "userAvatar": "头像URL",
    "userRole": "user",
    "points": 100
  },
  "message": "ok"
}</pre>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <AppstoreOutlined class="title-icon" />
        应用接口
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="POST /app/generate/sse/:id - 生成代码(流式)">
          <h4>请求参数</h4>
          <ul>
            <li>路径参数: <code>id</code> - 应用ID</li>
          </ul>

          <h4>响应类型</h4>
          <p>Server-Sent Events (SSE) 流式响应</p>

          <h4>事件格式</h4>
          <pre class="code-block">data: {"type": "code", "content": "生成的代码片段"}

data: {"type": "done", "message": "生成完成"}</pre>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="POST /app/add - 创建应用">
          <h4>请求参数</h4>
          <pre class="code-block">{
  "appName": "应用名称",
  "appDesc": "应用描述",
  "generationType": "HTML",  // HTML/VUE/MULTIFILE
  "requirement": "需求描述"
}</pre>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": 789,      // 应用ID
  "message": "ok"
}</pre>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="GET /app/get/:id - 获取应用详情">
          <h4>请求参数</h4>
          <ul>
            <li>路径参数: <code>id</code> - 应用ID</li>
          </ul>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": {
    "id": 789,
    "appName": "应用名称",
    "appDesc": "应用描述",
    "generationType": "HTML",
    "generatedCode": "生成的代码",
    "status": "completed",
    "userId": 123456
  },
  "message": "ok"
}</pre>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="POST /app/list/page - 分页查询应用">
          <h4>请求参数</h4>
          <pre class="code-block">{
  "current": 1,
  "pageSize": 10,
  "sortField": "createTime",
  "sortOrder": "desc"
}</pre>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": {
    "records": [...],    // 应用列表
    "total": 100,        // 总数
    "current": 1,        // 当前页
    "size": 10           // 每页数量
  },
  "message": "ok"
}</pre>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <WalletOutlined class="title-icon" />
        积分接口
      </h2>

      <a-collapse :bordered="false">
        <a-collapse-panel key="1" header="POST /points/sign-in - 每日签到">
          <h4>请求参数</h4>
          <p>无</p>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": {
    "points": 10,
    "message": "签到成功"
  },
  "message": "ok"
}</pre>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="GET /points/detail - 积分明细">
          <h4>请求参数</h4>
          <ul>
            <li>查询参数: <code>current</code> - 页码(默认1)</li>
            <li>查询参数: <code>pageSize</code> - 每页数量(默认10)</li>
          </ul>

          <h4>响应示例</h4>
          <pre class="code-block">{
  "code": 0,
  "data": {
    "records": [
      {
        "type": "EARN",
        "amount": 10,
        "reason": "每日签到",
        "createTime": "2025-01-01 12:00:00"
      }
    ],
    "total": 50
  },
  "message": "ok"
}</pre>
        </a-collapse-panel>
      </a-collapse>
    </section>

    <section class="doc-section">
      <h2 class="section-title">
        <ToolOutlined class="title-icon" />
        使用示例
      </h2>

      <a-tabs>
        <a-tab-pane key="1" tab="JavaScript">
          <pre class="code-block">// 用户登录
fetch('{{ API_HOST }}/user/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userAccount: 'username',
    userPassword: 'password'
  }),
  credentials: 'include'  // 包含cookie
})
.then(res => res.json())
.then(data => {
  if (data.code === 0) {
    console.log('登录成功', data.data);
  }
});</pre>
        </a-tab-pane>

        <a-tab-pane key="2" tab="Axios">
          <pre class="code-block">import axios from 'axios';

const api = axios.create({
  baseURL: '{{ API_HOST }}',
  withCredentials: true
});

// 用户登录
const login = async (account, password) => {
  const res = await api.post('/user/login', {
    userAccount: account,
    userPassword: password
  });
  return res.data;
};</pre>
        </a-tab-pane>

        <a-tab-pane key="3" tab="cURL">
          <pre class="code-block"># 用户登录
curl -X POST {{ API_HOST }}/user/login \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"username","userPassword":"password"}' \
  --cookie-jar cookies.txt

# 获取登录用户(使用保存的cookie)
curl -X GET {{ API_HOST }}/user/get/login \
  --cookie cookies.txt</pre>
        </a-tab-pane>
      </a-tabs>
    </section>

    <a-divider />

    <div style="text-align: center; padding: 24px; background: #f5f5f5; border-radius: 8px;">
      <h3>在线文档</h3>
      <p>更详细的 API 文档请访问 Swagger UI</p>
      <a-button type="primary" :href="`${API_HOST}/doc.html`" target="_blank">
        <ApiOutlined /> 访问 Swagger UI
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  ApiOutlined,
  CodeOutlined,
  UserOutlined,
  AppstoreOutlined,
  WalletOutlined,
  ToolOutlined
} from '@ant-design/icons-vue'
import { API_BASE_URL } from '@/config/env'

// 获取 API 主机地址（用于文档展示）
const API_HOST = import.meta.env.PROD ? 'https://joinoai.cloud/api' : 'http://localhost:8123/api'

const errorCodes = [
  { code: 0, name: 'SUCCESS', description: '成功' },
  { code: 40000, name: 'PARAMS_ERROR', description: '请求参数错误' },
  { code: 40100, name: 'NOT_LOGIN_ERROR', description: '未登录' },
  { code: 40101, name: 'NO_AUTH_ERROR', description: '无权限' },
  { code: 40300, name: 'FORBIDDEN_ERROR', description: '禁止访问' },
  { code: 40400, name: 'NOT_FOUND_ERROR', description: '资源不存在' },
  { code: 50000, name: 'SYSTEM_ERROR', description: '系统错误' },
  { code: 50001, name: 'OPERATION_ERROR', description: '操作失败' },
]

const errorCodeColumns = [
  { title: '错误码', dataIndex: 'code', key: 'code' },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '说明', dataIndex: 'description', key: 'description' },
]
</script>

<style scoped>
@import './features/doc-common.css';

.code-block {
  background: linear-gradient(135deg, #1e1e2e 0%, #2d2d44 100%);
  color: #e0e0e0;
  padding: 24px;
  border-radius: 12px;
  overflow-x: auto;
  font-family: 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
  margin: 16px 0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.05);
  position: relative;
  transition: all 0.3s ease;
}

.code-block:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.4);
  transform: translateY(-2px);
}

.code-block::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 32px;
  background: linear-gradient(90deg,
    rgba(255, 95, 109, 0.3) 0%,
    rgba(255, 195, 113, 0.3) 25%,
    rgba(100, 221, 23, 0.3) 50%,
    rgba(0, 0, 0, 0) 100%);
  border-radius: 12px 12px 0 0;
  opacity: 0.6;
}

/* JSON 语法高亮 */
.code-block {
  white-space: pre;
}

/* 行号效果 */
.code-block::after {
  counter-reset: line;
}

code {
  background: linear-gradient(135deg, #fef2f2 0%, #e8ecff 100%);
  padding: 3px 8px;
  border-radius: 6px;
  font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
  color: #ec4899;
  font-weight: 600;
  font-size: 0.9em;
  border: 1px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 1px 3px rgba(102, 126, 234, 0.1);
}

/* 响应式代码块 */
@media (max-width: 768px) {
  .code-block {
    font-size: 12px;
    padding: 16px;
  }
}
</style>
