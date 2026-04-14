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

export interface OrderCreateRequest {
  items: {
    productId: number
    quantity: number
    specs?: string
  }[]
  remark?: string
}

export interface ProductSpecs {
  ice: 'normal' | 'less' | 'no_ice' | 'hot'
  sweetness: 'full' | 'less' | 'half' | 'little' | 'none'
  toppings: string[]
}
