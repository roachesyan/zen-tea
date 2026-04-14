export interface Product {
  id: number
  categoryId: number
  categoryName: string
  name: string
  price: number
  imageUrl: string
  description: string
  status: number
  sort: number
  createTime: string
  updateTime: string
}

export interface ProductCreateRequest {
  categoryId: number
  name: string
  price: number
  imageUrl?: string
  description?: string
  sort?: number
}

export interface ProductUpdateRequest {
  categoryId?: number
  name?: string
  price?: number
  imageUrl?: string
  description?: string
  status?: number
  sort?: number
}
