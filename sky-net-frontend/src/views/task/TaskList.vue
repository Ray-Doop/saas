<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { taskApi, type GenTask, type TaskQO } from '@/api/task'

const router = useRouter()
const loading = ref(false)
const dataSource = ref<GenTask[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const searchForm = ref<TaskQO>({ page: 1, size: 10 })

const columns = [
  { title: '任务编号', dataIndex: 'taskCode', key: 'taskCode' },
  {
    title: '状态', dataIndex: 'status', key: 'status',
    slots: { customRender: 'status' }
  },
  {
    title: '进度', dataIndex: 'progress', key: 'progress',
    slots: { customRender: 'progress' }
  },
  { title: '消耗积分', dataIndex: 'costPoints', key: 'costPoints' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', slots: { customRender: 'action' } }
]

const statusMap: Record<string, { color: string; label: string }> = {
  PENDING: { color: 'default', label: '待处理' },
  PROCESSING: { color: 'processing', label: '生成中' },
  SUCCESS: { color: 'success', label: '成功' },
  FAILED: { color: 'error', label: '失败' }
}

let pollTimer: any = null

async function fetchData() {
  loading.value = true
  try {
    const res: any = await taskApi.page({
      ...searchForm.value,
      page: pagination.current,
      size: pagination.pageSize
    })
    dataSource.value = res.records
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

// 定时轮询进行中的任务
function startPolling() {
  pollTimer = setInterval(() => {
    const hasProcessing = dataSource.value.some(t => t.status === 'PROCESSING' || t.status === 'PENDING')
    if (hasProcessing) fetchData()
  }, 5000)
}

async function handleRetry(id: number) {
  await taskApi.retry(id)
  fetchData()
}

function goDetail(id: number) {
  router.push(`/task/${id}`)
}

onMounted(() => {
  fetchData()
  startPolling()
})

import { onBeforeUnmount } from 'vue'
onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div>
    <div class="page-header">
      <h2>任务记录</h2>
    </div>

    <div class="card-container">
      <a-form layout="inline" style="margin-bottom: 16px">
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" style="width: 140px" allowClear placeholder="全部">
            <a-select-option value="PENDING">待处理</a-select-option>
            <a-select-option value="PROCESSING">生成中</a-select-option>
            <a-select-option value="SUCCESS">成功</a-select-option>
            <a-select-option value="FAILED">失败</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="pagination.current = 1; fetchData()">搜索</a-button>
        </a-form-item>
      </a-form>

      <a-table :columns="columns" :data-source="dataSource" :loading="loading"
        :pagination="pagination" row-key="id"
        @change="({ current, pageSize }: any) => { pagination.current = current; pagination.pageSize = pageSize; fetchData() }">
        <template #status="{ record }">
          <a-tag :color="statusMap[record.status]?.color">
            {{ statusMap[record.status]?.label || record.status }}
          </a-tag>
        </template>
        <template #progress="{ record }">
          <a-progress v-if="record.status === 'PROCESSING' || record.status === 'PENDING'"
            :percent="record.progress" size="small" style="width: 100px" />
          <span v-else-if="record.status === 'SUCCESS'">100%</span>
          <span v-else-if="record.status === 'FAILED'" style="color: red">失败</span>
          <span v-else>-</span>
        </template>
        <template #action="{ record }">
          <a-space>
            <a @click="goDetail(record.id)">详情</a>
            <a v-if="record.status === 'FAILED'" @click="handleRetry(record.id)">重试</a>
          </a-space>
        </template>
      </a-table>
    </div>
  </div>
</template>
