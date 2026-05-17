<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { taskApi, type GenTask } from '@/api/task'

const route = useRoute()
const router = useRouter()
const task = ref<GenTask | null>(null)
const loading = ref(false)

const statusMap: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'default', label: '待处理' },
  PROCESSING: { color: 'processing', label: '生成中' },
  SUCCESS: { color: 'success', label: '成功' },
  FAILED: { color: 'error', label: '失败' }
}

async function fetchDetail() {
  loading.value = true
  try {
    const id = route.params.id as string
    task.value = await taskApi.getById(Number(id))
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>任务详情</h2>
      <a-button @click="router.back()">返回</a-button>
    </div>

    <a-spin :spinning="loading">
      <div class="card-container" v-if="task">
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="任务编号">{{ task.taskCode }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="statusMap[task.status]?.color">
              {{ statusMap[task.status]?.label }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="关联工作流ID">{{ task.workflowId }}</a-descriptions-item>
          <a-descriptions-item label="关联商品ID">{{ task.spuId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="进度">
            <a-progress :percent="task.progress" size="small" />
          </a-descriptions-item>
          <a-descriptions-item label="消耗积分">{{ task.costPoints }}</a-descriptions-item>
          <a-descriptions-item v-if="task.startTime" label="开始时间">{{ task.startTime }}</a-descriptions-item>
          <a-descriptions-item v-if="task.finishTime" label="完成时间">{{ task.finishTime }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ task.createTime }}</a-descriptions-item>
          <a-descriptions-item v-if="task.errorMessage" label="错误信息" :span="2">
            <span style="color: red">{{ task.errorMessage }}</span>
          </a-descriptions-item>
        </a-descriptions>

        <!-- 生成结果图片 -->
        <a-divider>生成结果</a-divider>
        <div v-if="task.results && task.results.length > 0" style="display: flex; gap: 16px; flex-wrap: wrap">
          <a-card v-for="result in task.results" :key="result.id" hoverable style="width: 280px">
            <template #cover>
              <img :src="result.imageUrl" style="width: 100%; height: 280px; object-fit: cover" />
            </template>
            <a-card-meta>
              <template #title>图片 #{{ result.imageIndex + 1 }}</template>
              <template #description>
                <span v-if="result.width && result.height">{{ result.width }} x {{ result.height }}</span>
                <span v-if="result.fileSize"> · {{ (result.fileSize / 1024).toFixed(1) }}KB</span>
              </template>
            </a-card-meta>
          </a-card>
        </div>

        <a-empty v-else-if="task.status === 'PROCESSING' || task.status === 'PENDING'"
          description="任务处理中, 请稍候..." />

        <a-empty v-else description="暂无生成结果" />
      </div>
    </a-spin>
  </div>
</template>
