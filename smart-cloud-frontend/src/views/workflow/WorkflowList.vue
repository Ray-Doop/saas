<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { workflowApi, type Workflow, type WorkflowQO } from '@/api/workflow'

const router = useRouter()
const loading = ref(false)
const dataSource = ref<Workflow[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const searchForm = ref<WorkflowQO>({ page: 1, size: 10 })

const columns = [
  { title: '名称', dataIndex: 'workflowName', key: 'workflowName' },
  {
    title: '分类', dataIndex: 'category', key: 'category',
    slots: { customRender: 'category' }
  },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '节点数', dataIndex: 'nodeCount', key: 'nodeCount' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', slots: { customRender: 'action' } }
]

const categoryMap: Record<string, string> = {
  poster: '海报', model: '模特图', scene: '场景图'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await workflowApi.page({
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

function goEditor(id?: number) {
  if (id) {
    router.push(`/workflow/editor/${id}`)
  } else {
    router.push('/workflow/editor')
  }
}

async function handleDelete(id: number) {
  try {
    await workflowApi.delete(id)
    message.success('删除成功')
    fetchData()
  } catch { }
}

onMounted(fetchData)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>工作流管理</h2>
      <a-space>
        <a-button @click="goEditor()"><PlusOutlined /> 新建工作流</a-button>
        <a-button type="primary" @click="goEditor()">打开编辑器</a-button>
      </a-space>
    </div>

    <div class="card-container">
      <a-table :columns="columns" :data-source="dataSource" :loading="loading"
        :pagination="pagination" row-key="id"
        @change="({ current, pageSize }: any) => { pagination.current = current; pagination.pageSize = pageSize; fetchData() }">
        <template #category="{ record }">
          <a-tag>{{ categoryMap[record.category] || record.category }}</a-tag>
        </template>
        <template #action="{ record }">
          <a-space>
            <a @click="goEditor(record.id)">编辑</a>
            <a-popconfirm title="确定删除?" @confirm="handleDelete(record.id)">
              <a style="color: red">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </div>
  </div>
</template>
