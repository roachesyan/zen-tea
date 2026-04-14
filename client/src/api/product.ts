import request from './request'
import type { Result, Category, Product } from '@/types'

export function getCategories() {
  return request.get<Result<Category[]>>('/categories')
}

export function getProductsByCategory(categoryId?: number) {
  return request.get<Result<Product[]>>('/products/list', {
    params: { categoryId },
  })
}

export function getProduct(id: number) {
  return request.get<Result<Product>>(`/products/${id}`)
}
