<template>
  <div class="app-header-bar">
    <div class="header-left">
      <h1 class="app-name">{{ appInfo?.appName || '网站生成器' }}</h1>
      <a-tag v-if="appInfo?.codeGenType" color="blue" class="code-gen-type-tag">
        {{ formatCodeGenType(appInfo.codeGenType) }}
      </a-tag>
    </div>
    <div class="header-right">
      <a-button type="default" @click="$emit('show-detail')">
        <template #icon>
          <InfoCircleOutlined />
        </template>
        应用详情
      </a-button>
      <a-button v-if="isOwner" type="default" @click="$emit('show-version-history')">
        <template #icon>
          <HistoryOutlined />
        </template>
        历史版本
      </a-button>
      <a-button
        type="primary"
        ghost
        @click="$emit('download-code')"
        :loading="downloading"
        :disabled="!isOwner"
      >
        <template #icon>
          <DownloadOutlined />
        </template>
        下载代码
      </a-button>
      <a-button type="primary" @click="$emit('deploy')" :loading="deploying">
        <template #icon>
          <CloudUploadOutlined />
        </template>
        部署
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  CloudUploadOutlined,
  InfoCircleOutlined,
  DownloadOutlined,
  HistoryOutlined,
} from '@ant-design/icons-vue'
import { formatCodeGenType } from '@/utils/codeGenTypes'

interface Props {
  appInfo?: API.AppVO
  isOwner: boolean
  downloading?: boolean
  deploying?: boolean
}

defineProps<Props>()

defineEmits<{
  'show-detail': []
  'show-version-history': []
  'download-code': []
  deploy: []
}>()
</script>

<style scoped>
.app-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.code-gen-type-tag {
  font-size: 12px;
}

.header-right {
  display: flex;
  gap: 12px;
}
</style>
