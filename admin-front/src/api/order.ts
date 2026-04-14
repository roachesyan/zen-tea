import request from './request'
import type { Result, PageResult, Order, DashboardStats } from '@/types'

export function getOrders(params: { pageNum?: number; pageSize?: number; status?: string }) {
  return request.get<Result<PageResult<Order>>>('/orders', { params })
}

export function getOrder(id: number) {
  return request.get<Result<Order>>(`/orders/${id}`)
}

export function updateOrderStatus(id: number, status: string) {
  return request.patch<Result<void>>(`/orders/${id}/status`, { status })
}

export function getDashboardStats() {
  return request.get<Result<DashboardStats>>('/orders/dashboard/stats')
}
