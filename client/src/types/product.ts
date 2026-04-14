export interface Category {
  id: number
  name: string
  icon: string
  sort: number
  status: number
}

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
}
