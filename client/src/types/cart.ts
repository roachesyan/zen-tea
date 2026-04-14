import type { ProductSpecs } from './order'

export interface CartItem {
  productId: number
  productName: string
  productPrice: number
  productImage: string
  quantity: number
  specs: ProductSpecs
  specsText: string
}
