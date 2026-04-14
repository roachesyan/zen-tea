export interface Order {
  id: number
  orderNo: string
  userId: number
  username: string
  totalAmount: number
  status: string
  remark: string
  items: OrderItem[]
  createTime: string
  updateTime: string
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  quantity: number
  price: number
  specs: string
  subtotal: number
}

export interface DashboardStats {
  todayOrderCount: number
  todayRevenue: number
  totalOrderCount: number
  totalRevenue: number
  pendingOrderCount: number
}
