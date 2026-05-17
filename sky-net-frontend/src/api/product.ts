import http from './request'

export interface ProductSPU {
  id?: number
  spuCode: string
  spuName: string
  categoryId?: number
  brand?: string
  description?: string
  mainImage?: string
  imageList?: string
  status?: number
  skuList?: ProductSKU[]
  createTime?: string
  updateTime?: string
}

export interface ProductSKU {
  id?: number
  spuId?: number
  skuCode: string
  skuName: string
  specs?: string
  price: number
  costPrice?: number
  stock: number
  image?: string
  status?: number
}

export interface ProductQO {
  spuName?: string
  spuCode?: string
  categoryId?: number
  brand?: string
  status?: number
  page?: number
  size?: number
}

export const productApi = {
  create(data: ProductSPU) {
    return http.post('/product/spu', data)
  },
  update(id: number, data: ProductSPU) {
    return http.put(`/product/spu/${id}`, data)
  },
  getById(id: number): Promise<ProductSPU> {
    return http.get(`/product/spu/${id}`)
  },
  page(params: ProductQO) {
    return http.get('/product/spu/page', { params })
  },
  delete(id: number) {
    return http.delete(`/product/spu/${id}`)
  },
  uploadImage(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/product/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
