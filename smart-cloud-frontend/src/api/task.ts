import http from './request'

export interface GenTask {
  id?: number
  taskCode: string
  workflowId: number
  spuId?: number
  skuId?: number
  productImage?: string
  inputParams?: string
  status: string
  resultImages?: string
  progress: number
  errorMessage?: string
  costPoints: number
  startTime?: string
  finishTime?: string
  createTime?: string
  results?: GenResult[]
}

export interface GenResult {
  id: number
  taskId: number
  imageUrl: string
  imageIndex: number
  fileSize?: number
  width?: number
  height?: number
}

export interface TaskQO {
  workflowId?: number
  spuId?: number
  status?: string
  page?: number
  size?: number
}

export interface TaskCreateDTO {
  workflowId: number
  spuId?: number
  skuId?: number
  productImage?: string
  variableMap?: Record<string, string>
}

export const taskApi = {
  create(data: TaskCreateDTO, workflowJson: string) {
    return http.post('/task/create', data, {
      headers: { 'X-Workflow-Json': workflowJson }
    })
  },
  getById(id: number): Promise<GenTask> {
    return http.get(`/task/${id}`)
  },
  page(params: TaskQO) {
    return http.get('/task/page', { params })
  },
  retry(id: number) {
    return http.post(`/task/${id}/retry`)
  }
}
