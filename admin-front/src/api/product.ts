import request from './request'
import type { Result, PageResult, Product, ProductCreateRequest, ProductUpdateRequest } from '@/types'

export function getProducts(params: {
  pageNum?: number
  pageSize?: number
  categoryId?: number
  keyword?: string
  status?: number
}) {
  return request.get<Result<PageResult<Product>>>('/products', { params })
}

export function getProduct(id: number) {
  return request.get<Result<Product>>(`/products/${id}`)
}

export function createProduct(data: ProductCreateRequest) {
  return request.post<Result<Product>>('/products', data)
}

export function updateProduct(id: number, data: ProductUpdateRequest) {
  return request.put<Result<Product>>(`/products/${id}`, data)
}

export function updateProductStatus(id: number, status: number) {
  return request.patch<Result<void>>(`/products/${id}/status`, null, { params: { status } })
}

export function deleteProduct(id: number) {
  return request.delete<Result<void>>(`/products/${id}`)
}
