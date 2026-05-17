import http from './request'

export interface Workflow {
  id?: number
  workflowName: string
  description?: string
  category?: string
  workflowJson: string
  previewImage?: string
  nodeCount?: number
  status?: number
  createTime?: string
  updateTime?: string
}

export interface WorkflowQO {
  workflowName?: string
  category?: string
  status?: number
  page?: number
  size?: number
}

export const workflowApi = {
  create(data: Workflow) {
    return http.post('/workflow', data)
  },
  update(id: number, data: Workflow) {
    return http.put(`/workflow/${id}`, data)
  },
  getById(id: number): Promise<Workflow> {
    return http.get(`/workflow/${id}`)
  },
  page(params: WorkflowQO) {
    return http.get('/workflow/page', { params })
  },
  delete(id: number) {
    return http.delete(`/workflow/${id}`)
  }
}
