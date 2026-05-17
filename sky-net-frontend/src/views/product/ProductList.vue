<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { productApi, type ProductSPU, type ProductQO } from '@/api/product'

const router = useRouter()
const loading = ref(false)
const dataSource = ref<ProductSPU[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})
const searchForm = ref<ProductQO>({ page: 1, size: 10 })

const columns = [
  { title: 'SPU编码', dataIndex: 'spuCode', key: 'spuCode' },
  { title: '商品名称', dataIndex: 'spuName', key: 'spuName' },
  { title: '品牌', dataIndex: 'brand', key: 'brand' },
  {
    title: '主图', dataIndex: 'mainImage', key: 'mainImage',
    slots: { customRender: 'mainImage' }
  },
  {
    title: '状态', dataIndex: 'status', key: 'status',
    slots: { customRender: 'status' }
  },
  {
    title: '创建时间', dataIndex: 'createTime', key: 'createTime'
  },
  {
    title: '操作', key: 'action',
    slots: { customRender: 'action' }
  }
]

async function fetchData() {
  loading.value = true
  try {
    const res: any = await productApi.page({
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

function handleSearch() {
  pagination.current = 1
  fetchData()
}

function handlePageChange(page: number, size: number) {
  pagination.current = page
  pagination.pageSize = size
  fetchData()
}

function goCreate() {
  router.push('/product/detail')
}

function goEdit(id: number) {
  router.push(`/product/detail/${id}`)
}

async function handleDelete(id: number) {
  try {
    await productApi.delete(id)
    message.success('删除成功')
    fetchData()
  } catch { /* error handled by interceptor */ }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div>
    <div class="page-header">
      <h2>商品管理</h2>
      <a-button type="primary" @click="goCreate"><PlusOutlined /> 新建商品</a-button>
    </div>

    <div class="card-container">
      <a-form layout="inline" style="margin-bottom: 16px">
        <a-form-item label="商品名称">
          <a-input v-model:value="searchForm.spuName" placeholder="搜索商品名称" />
        </a-form-item>
        <a-form-item label="SPU编码">
          <a-input v-model:value="searchForm.spuCode" placeholder="搜索编码" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">搜索</a-button>
          <a-button style="margin-left: 8px" @click="searchForm = { page: 1, size: 10 }; handleSearch()">重置</a-button>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="({ current, pageSize }: any) => handlePageChange(current, pageSize)"
      >
        <template #mainImage="{ record }">
          <a-image v-if="record.mainImage" :src="record.mainImage" width="60" height="60" style="object-fit: cover; border-radius: 4px" />
          <span v-else style="color: #ccc">无图片</span>
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '上架' : '下架' }}
          </a-tag>
        </template>
        <template #action="{ record }">
          <a-space>
            <a @click="goEdit(record.id)">编辑</a>
            <a-popconfirm title="确定删除?" @confirm="handleDelete(record.id)">
              <a style="color: red">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </div>
  </div>
</template>
