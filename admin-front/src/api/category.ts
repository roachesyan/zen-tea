import request from './request'
import type { Result, Category, CategoryCreateRequest, CategoryUpdateRequest } from '@/types'

export function getCategories() {
  return request.get<Result<Category[]>>('/categories')
}

export function createCategory(data: CategoryCreateRequest) {
  return request.post<Result<Category>>('/categories', data)
}

export function updateCategory(id: number, data: CategoryUpdateRequest) {
  return request.put<Result<Category>>(`/categories/${id}`, data)
}

export function deleteCategory(id: number) {
  return request.delete<Result<void>>(`/categories/${id}`)
}
