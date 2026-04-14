import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { showFailToast } from 'vant'
import type { CartItem, ProductSpecs } from '@/types'
import { formatSpecs } from '@/utils/format'
import { getProductsByCategory } from '@/api/product'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((sum, item) => sum + item.productPrice * item.quantity, 0))

  async function addItem(productId: number, productName: string, productPrice: number, productImage: string, specs: ProductSpecs) {
    const specsText = formatSpecs(specs)
    const existing = items.value.find(
      (item) => item.productId === productId && item.specsText === specsText,
    )
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({
        productId,
        productName,
        productPrice,
        productImage,
        quantity: 1,
        specs,
        specsText,
      })
    }
  }

  function removeItem(index: number) {
    items.value.splice(index, 1)
  }

  function updateQuantity(index: number, quantity: number) {
    if (quantity <= 0) {
      removeItem(index)
    } else {
      items.value[index].quantity = quantity
    }
  }

  function clearCart() {
    items.value = []
  }

  return { items, totalCount, totalPrice, addItem, removeItem, updateQuantity, clearCart }
}, {
  persist: true,
})
