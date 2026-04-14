import request from './request'
import type { Result, PageResult, Order, OrderCreateRequest } from '@/types'

export function createOrder(data: OrderCreateRequest) {
  return request.post<Result<Order>>('/orders', data)
}

export function getMyOrders(params: { pageNum?: number; pageSize?: number; status?: string }) {
  return request.get<Result<PageResult<Order>>>('/orders/mine', { params })
}

export function getOrder(id: number) {
  return request.get<Result<Order>>(`/orders/${id}`)
}
