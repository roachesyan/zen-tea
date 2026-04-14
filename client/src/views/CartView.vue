<template>
  <div class="page cart-page">
    <van-nav-bar title="购物车" />

    <EmptyState v-if="cartStore.items.length === 0" description="购物车是空的">
      <van-button round type="primary" class="empty-btn" @click="router.push('/menu')">去点单</van-button>
    </EmptyState>

    <template v-else>
      <div class="cart-list">
        <CartItem
          v-for="(item, index) in cartStore.items"
          :key="index"
          :item="item"
          :index="index"
          @update="handleUpdate"
        />
      </div>

      <van-submit-bar
        :price="cartStore.totalPrice * 100"
        button-text="去结算"
        @submit="handleSubmit"
      >
        <template #default>
          共 {{ cartStore.totalCount }} 件
        </template>
      </van-submit-bar>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import CartItem from '@/components/cart/CartItem.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

function handleUpdate(index: number, quantity: number) {
  cartStore.updateQuantity(index, quantity)
}

function handleSubmit() {
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: '/checkout' } })
    return
  }
  router.push('/checkout')
}
</script>

<style scoped lang="scss">
.cart-list {
  padding-bottom: 60px;
}

.empty-btn {
  width: 160px;
}
</style>
