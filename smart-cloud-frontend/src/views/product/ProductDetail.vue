<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { productApi, type ProductSPU, type ProductSKU } from '@/api/product'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const saving = ref(false)

const formData = ref<ProductSPU>({
  spuCode: '',
  spuName: '',
  brand: '',
  description: '',
  mainImage: '',
  skuList: []
})

const skuColumns = [
  { title: 'SKU编码', dataIndex: 'skuCode', key: 'skuCode' },
  { title: 'SKU名称', dataIndex: 'skuName', key: 'skuName' },
  { title: '规格', dataIndex: 'specs', key: 'specs' },
  { title: '价格', dataIndex: 'price', key: 'price' },
  { title: '库存', dataIndex: 'stock', key: 'stock' },
  { title: '操作', key: 'action', slots: { customRender: 'action' } }
]

function addSku() {
  formData.value.skuList = formData.value.skuList || []
  formData.value.skuList.push({
    skuCode: `SKU_${Date.now()}`,
    skuName: '',
    specs: '',
    price: 0,
    costPrice: 0,
    stock: 0
  })
}

function removeSku(index: number) {
  formData.value.skuList?.splice(index, 1)
}

async function handleUpload(info: any) {
  if (info.file.status === 'done') {
    const url = info.file.response
    formData.value.mainImage = url
    message.success('上传成功')
  }
}

async function handleCustomUpload(options: any) {
  try {
    const url = await productApi.uploadImage(options.file)
    options.onSuccess(url)
  } catch {
    options.onError(new Error('上传失败'))
  }
}

async function handleSave() {
  if (!formData.value.spuCode || !formData.value.spuName) {
    message.warning('请填写SPU编码和商品名称')
    return
  }
  saving.value = true
  try {
    if (isEdit.value && formData.value.id) {
      await productApi.update(formData.value.id, formData.value)
      message.success('更新成功')
    } else {
      await productApi.create(formData.value)
      message.success('创建成功')
    }
    router.back()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const id = route.params.id as string
  if (id) {
    isEdit.value = true
    const data: any = await productApi.getById(Number(id))
    formData.value = data
  }
})
</script>

<template>
  <div>
    <div class="page-header">
      <h2>{{ isEdit ? '编辑商品' : '新建商品' }}</h2>
    </div>

    <div class="card-container">
      <a-form layout="vertical" :model="formData">
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="SPU编码" required>
              <a-input v-model:value="formData.spuCode" :disabled="isEdit" placeholder="如: PROD_20240001" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="商品名称" required>
              <a-input v-model:value="formData.spuName" placeholder="商品名称" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="品牌">
              <a-input v-model:value="formData.brand" placeholder="品牌" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="分类ID">
              <a-input-number v-model:value="formData.categoryId" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="商品描述">
          <a-textarea v-model:value="formData.description" :rows="3" />
        </a-form-item>

        <a-form-item label="商品主图">
          <a-upload
            :custom-request="handleCustomUpload"
            list-type="picture-card"
            :show-upload-list="false"
          >
            <img v-if="formData.mainImage" :src="formData.mainImage" style="width: 100%; height: 100%; object-fit: cover" />
            <div v-else>
              <UploadOutlined />
              <div style="margin-top: 8px">上传图片</div>
            </div>
          </a-upload>
        </a-form-item>

        <a-divider>SKU 管理</a-divider>

        <div style="margin-bottom: 16px">
          <a-button type="dashed" @click="addSku"><PlusOutlined /> 添加SKU</a-button>
        </div>

        <a-table :columns="skuColumns" :data-source="formData.skuList" :pagination="false" row-key="skuCode">
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.key === 'skuCode'">
              <a-input v-model:value="record.skuCode" size="small" />
            </template>
            <template v-else-if="column.key === 'skuName'">
              <a-input v-model:value="record.skuName" size="small" />
            </template>
            <template v-else-if="column.key === 'specs'">
              <a-input v-model:value="record.specs" size="small" placeholder='{"颜色":"红色"}' />
            </template>
            <template v-else-if="column.key === 'price'">
              <a-input-number v-model:value="record.price" size="small" :min="0" :precision="2" style="width: 100px" />
            </template>
            <template v-else-if="column.key === 'stock'">
              <a-input-number v-model:value="record.stock" size="small" :min="0" style="width: 80px" />
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="link" danger size="small" @click="removeSku(index)"><DeleteOutlined /></a-button>
            </template>
          </template>
        </a-table>

        <div style="margin-top: 24px; text-align: right">
          <a-space>
            <a-button @click="router.back()">取消</a-button>
            <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
          </a-space>
        </div>
      </a-form>
    </div>
  </div>
</template>
