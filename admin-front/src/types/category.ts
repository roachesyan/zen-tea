export interface Category {
  id: number
  name: string
  icon: string
  sort: number
  status: number
  createTime: string
  updateTime: string
}

export interface CategoryCreateRequest {
  name: string
  icon?: string
  sort?: number
}

export interface CategoryUpdateRequest {
  name?: string
  icon?: string
  sort?: number
  status?: number
}
