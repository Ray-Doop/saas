<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Graph, Shape, Addon } from '@antv/x6'
import { message } from 'ant-design-vue'
import { workflowApi } from '@/api/workflow'

const route = useRoute()
const router = useRouter()

let graph: Graph | null = null
const containerRef = ref<HTMLDivElement>()
const saving = ref(false)
const workflowName = ref('未命名工作流')
const workflowDesc = ref('')

// 节点模板
const nodeTemplates = [
  {
    category: '加载器',
    items: [
      { type: 'LoadImage', label: '加载图片', icon: '🖼️', color: '#1890ff' },
      { type: 'LoadCheckpoint', label: '加载模型', icon: '🧠', color: '#1890ff' },
      { type: 'LoadVAE', label: '加载VAE', icon: '⚙️', color: '#1890ff' }
    ]
  },
  {
    category: '条件',
    items: [
      { type: 'CLIPTextEncode', label: '正向提示词', icon: '📝', color: '#52c41a' },
      { type: 'CLIPTextEncodeNeg', label: '负向提示词', icon: '🚫', color: '#ff4d4f' }
    ]
  },
  {
    category: '采样',
    items: [
      { type: 'KSampler', label: '采样器', icon: '🎯', color: '#722ed1' },
      { type: 'KSamplerAdvanced', label: '高级采样器', icon: '🎯', color: '#722ed1' }
    ]
  },
  {
    category: '输出',
    items: [
      { type: 'VAEDecode', label: 'VAE解码', icon: '🔓', color: '#fa8c16' },
      { type: 'SaveImage', label: '保存图片', icon: '💾', color: '#fa8c16' },
      { type: 'PreviewImage', label: '预览图片', icon: '👁️', color: '#fa8c16' }
    ]
  }
]

// 属性面板
const selectedNode = ref<any>(null)
const showProperties = ref(false)
const nodeProperties = ref<Record<string, any>>({})

function initGraph() {
  graph = new Graph({
    container: containerRef.value!,
    width: containerRef.value!.clientWidth,
    height: containerRef.value!.clientHeight,
    background: { color: '#f5f5f5' },
    grid: { size: 10, visible: true, type: 'dot' },
    connecting: {
      connector: { name: 'smooth' },
      connectionPoint: 'boundary',
      router: { name: 'er' },
      allowBlank: false,
      snap: { radius: 20 },
      allowEdge: true,
      allowLoop: false,
      highlight: true,
      createEdge() {
        return new Shape.Edge({
          attrs: {
            line: {
              stroke: '#1890ff',
              strokeWidth: 2,
              targetMarker: { name: 'block', width: 12, height: 8 }
            }
          }
        })
      }
    },
    mousewheel: {
      enabled: true,
      modifiers: 'ctrl',
      factor: 1.1
    }
  })

  // 使用 DnD 插件
  const dnd = new Addon.Dnd({
    target: graph,
    scaled: false,
    dndContainer: document.body
  })

  // 节点点击 - 显示属性面板
  graph.on('node:click', ({ node }) => {
    selectedNode.value = node
    showProperties.value = true
    const data = node.getData() || {}
    nodeProperties.value = { ...data.properties } || {}
  })

  // 空白区域点击 - 隐藏属性面板
  graph.on('blank:click', () => {
    showProperties.value = false
    selectedNode.value = null
  })

  // 绑定 DnD 到容器
  if (containerRef.value) {
    dnd.setDndContainer(containerRef.value)
  }

  // 键盘快捷键
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Delete' && selectedNode.value) {
      graph?.removeCell(selectedNode.value.id)
      showProperties.value = false
      selectedNode.value = null
    }
  })
}

// 从工具栏拖拽添加节点
function onDragStart(event: DragEvent, template: any) {
  if (!graph) return
  event.dataTransfer?.setData('nodeType', template.type)
  event.dataTransfer?.setData('nodeLabel', template.label)
  event.dataTransfer?.setData('nodeColor', template.color)
}

// 监听 drop 事件
function setupDropHandler() {
  const container = containerRef.value
  if (!container || !graph) return

  container.addEventListener('dragover', (e) => {
    e.preventDefault()
  })

  container.addEventListener('drop', (e) => {
    e.preventDefault()
    const type = e.dataTransfer?.getData('nodeType')
    const label = e.dataTransfer?.getData('nodeLabel')
    const color = e.dataTransfer?.getData('nodeColor')
    if (!type || !graph) return

    const rect = container.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top

    addNode(type, label, color, x, y)
  })
}

function addNode(type: string, label: string, color: string, x: number, y: number) {
  if (!graph) return

  const node = graph.addNode({
    shape: 'rect',
    x,
    y,
    width: 180,
    height: 60,
    label,
    attrs: {
      body: {
        fill: '#fff',
        stroke: color,
        strokeWidth: 2,
        rx: 8,
        ry: 8
      },
      label: {
        fill: '#333',
        fontSize: 13,
        fontWeight: 'bold'
      }
    },
    ports: {
      groups: {
        in: {
          position: 'left',
          attrs: { circle: { r: 5, fill: color, stroke: '#fff', strokeWidth: 2 } }
        },
        out: {
          position: 'right',
          attrs: { circle: { r: 5, fill: color, stroke: '#fff', strokeWidth: 2 } }
        }
      },
      items: [
        { id: 'in', group: 'in' },
        { id: 'out', group: 'out' }
      ]
    },
    data: { type, properties: getDefaultProperties(type) }
  })
}

function getDefaultProperties(type: string): Record<string, any> {
  const defaults: Record<string, Record<string, any>> = {
    LoadCheckpoint: { model: 'SDXL', ckpt_name: 'sd_xl_base_1.0.safetensors' },
    CLIPTextEncode: { text: 'masterpiece, best quality...', width: 1024, height: 1024 },
    CLIPTextEncodeNeg: { text: 'worst quality, low quality...' },
    KSampler: { seed: -1, steps: 20, cfg: 7.0, sampler: 'euler', scheduler: 'normal', denoise: 1.0 },
    LoadImage: { image: '{{product_image}}' },
    SaveImage: { prefix: 'generated_' },
    VAEDecode: {},
    PreviewImage: {}
  }
  return defaults[type] || {}
}

function updateNodeProperties() {
  if (selectedNode.value && graph) {
    selectedNode.value.setData({ ...selectedNode.value.getData(), properties: { ...nodeProperties.value } })
    message.success('属性已更新')
  }
}

// 导出 ComfyUI 格式 JSON
function exportWorkflowJSON() {
  if (!graph) return

  const cells = graph.getCells()
  const nodes: any[] = []
  const links: any[] = []
  let nodeIdx = 1

  cells.forEach((cell: any) => {
    if (cell.isNode()) {
      const data = cell.getData()
      const position = cell.getPosition()
      const nodeDef = {
        id: nodeIdx,
        type: data.type,
        pos: [Math.round(position.x), Math.round(position.y)],
        size: [cell.getSize().width, cell.getSize().height],
        flags: {},
        order: nodeIdx,
        mode: 0,
        inputs: [],
        outputs: [],
        properties: data.properties || {},
        widgets_values: Object.values(data.properties || {})
      }
      nodes.push(nodeDef)
      cell.setData({ ...data, comfyNodeId: nodeIdx })
      nodeIdx++
    }
  })

  // 处理连线
  cells.forEach((cell: any) => {
    if (cell.isEdge()) {
      const source = cell.getSourceCell()
      const target = cell.getTargetCell()
      if (source && target) {
        const srcData = source.getData()
        const tgtData = target.getData()
        links.push([srcData.comfyNodeId || 0, 0, tgtData.comfyNodeId || 0, 0, null])
      }
    }
  })

  const workflow = {
    last_node_id: nodeIdx - 1,
    last_link_id: links.length,
    nodes,
    links,
    groups: [],
    config: {},
    extra: {
      ds: { scale: 1, offset: [0, 0] }
    },
    version: 0.4
  }

  // 同时构建 ComfyUI API 格式
  const apiFormat: Record<string, any> = {}
  nodes.forEach((node: any) => {
    apiFormat[String(node.id)] = {
      inputs: {},
      class_type: node.type,
      _meta: { title: node.type }
    }
  })
  links.forEach((link: any, idx: number) => {
    apiFormat[String(link[2])].inputs[String(link[1])] = [String(link[0]), link[3]]
  })

  const result = { workflow, apiFormat, nodeCount: nodes.length, linkCount: links.length }

  console.log('导出工作流:', JSON.stringify(result, null, 2))
  message.success(`导出了 ${nodes.length} 个节点, ${links.length} 条连线`)

  return result
}

// 保存工作流
async function saveWorkflow() {
  const json = exportWorkflowJSON()
  if (!json) return

  saving.value = true
  try {
    const payload = {
      workflowName: workflowName.value,
      description: workflowDesc.value,
      category: 'poster',
      workflowJson: JSON.stringify(json),
      nodeCount: json.nodeCount
    }

    const id = route.params.id as string
    if (id) {
      await workflowApi.update(Number(id), payload)
      message.success('更新成功')
    } else {
      await workflowApi.create(payload)
      message.success('保存成功')
    }
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await nextTick()
  initGraph()
  setupDropHandler()

  // 加载已有工作流
  const id = route.params.id as string
  if (id) {
    try {
      const data: any = await workflowApi.getById(Number(id))
      workflowName.value = data.workflowName
      workflowDesc.value = data.description || ''

      // 从保存的JSON恢复节点和连线
      if (data.workflowJson && graph) {
        const saved = JSON.parse(data.workflowJson)
        const wf = saved.workflow
        if (wf && wf.nodes) {
          wf.nodes.forEach((node: any) => {
            addNode(node.type, node.type, '#1890ff', node.pos[0], node.pos[1])
          })
          // 恢复连线需要节点映射, MVP简化处理
        }
        message.success('已加载工作流')
      }
    } catch {
      message.error('加载工作流失败')
    }
  }
})

onBeforeUnmount(() => {
  if (graph) {
    graph.dispose()
    graph = null
  }
})
</script>

<template>
  <div style="height: calc(100vh - 150px); display: flex; flex-direction: column">
    <!-- 顶部工具栏 -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
      <a-space>
        <a-input v-model:value="workflowName" style="width: 200px" placeholder="工作流名称" />
        <a-input v-model:value="workflowDesc" style="width: 300px" placeholder="描述(可选)" />
      </a-space>
      <a-space>
        <a-button @click="exportWorkflowJSON()">导出JSON</a-button>
        <a-button type="primary" :loading="saving" @click="saveWorkflow()">保存工作流</a-button>
        <a-button @click="router.back()">返回</a-button>
      </a-space>
    </div>

    <!-- 主体区域 -->
    <div style="display: flex; flex: 1; gap: 12px; overflow: hidden">
      <!-- 左侧节点面板 -->
      <div style="width: 200px; background: #fff; border-radius: 8px; padding: 12px; overflow-y: auto; border: 1px solid #e8e8e8">
        <h4 style="margin-bottom: 12px; color: #666">节点工具箱</h4>
        <div v-for="group in nodeTemplates" :key="group.category" style="margin-bottom: 12px">
          <div style="font-size: 12px; color: #999; margin-bottom: 6px">{{ group.category }}</div>
          <div
            v-for="item in group.items"
            :key="item.type"
            draggable="true"
            @dragstart="onDragStart($event, item)"
            style="
              padding: 8px 12px;
              margin-bottom: 4px;
              border: 1px solid #e8e8e8;
              border-radius: 6px;
              cursor: grab;
              font-size: 13px;
              display: flex;
              align-items: center;
              gap: 6px;
              user-select: none;
            "
            :style="{ borderLeft: `3px solid ${item.color}` }"
          >
            <span>{{ item.icon }}</span>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>

      <!-- 中间画布 -->
      <div ref="containerRef" class="x6-editor-container" style="flex: 1"></div>

      <!-- 右侧属性面板 -->
      <div v-if="showProperties" style="width: 260px; background: #fff; border-radius: 8px; padding: 16px; overflow-y: auto; border: 1px solid #e8e8e8">
        <h4 style="margin-bottom: 12px; color: #666">属性配置</h4>
        <template v-if="selectedNode">
          <a-descriptions :column="1" size="small" bordered style="margin-bottom: 12px">
            <a-descriptions-item label="类型">{{ selectedNode.getData().type }}</a-descriptions-item>
            <a-descriptions-item label="标签">{{ selectedNode.attr('label/text') }}</a-descriptions-item>
          </a-descriptions>

          <a-divider style="margin: 8px 0">节点参数</a-divider>

          <div v-for="(value, key) in nodeProperties" :key="key" style="margin-bottom: 8px">
            <label style="display: block; font-size: 12px; color: #666; margin-bottom: 4px">{{ key }}</label>
            <a-input-number
              v-if="typeof value === 'number'"
              v-model:value="nodeProperties[key]"
              size="small"
              style="width: 100%"
            />
            <a-input
              v-else
              v-model:value="nodeProperties[key]"
              size="small"
            />
          </div>

          <a-button type="primary" size="small" block @click="updateNodeProperties" style="margin-top: 8px">
            应用更改
          </a-button>
        </template>
      </div>
    </div>
  </div>
</template>
