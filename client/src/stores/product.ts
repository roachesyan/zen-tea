import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Category, Product } from '@/types'
import { getCategories, getProductsByCategory } from '@/api/product'

export const useProductStore = defineStore('product', () => {
  const categories = ref<Category[]>([])
  const products = ref<Product[]>([])
  const loading = ref(false)

  async function fetchCategories() {
    const { data: res } = await getCategories()
    categories.value = res.data
  }

  async function fetchProducts(categoryId?: number) {
    loading.value = true
    try {
      const { data: res } = await getProductsByCategory(categoryId)
      products.value = res.data
    } finally {
      loading.value = false
    }
  }

  return { categories, products, loading, fetchCategories, fetchProducts }
})
