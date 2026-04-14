<template>
  <div class="cart-item">
    <van-image :src="item.productImage || defaultImage" fit="cover" width="80" height="80" radius="8" />
    <div class="cart-info">
      <div class="cart-name van-ellipsis">{{ item.productName }}</div>
      <div class="cart-specs">{{ item.specsText }}</div>
      <div class="cart-bottom">
        <span class="price">¥{{ item.productPrice.toFixed(2) }}</span>
        <van-stepper v-model="quantity" min="0" @change="handleQuantityChange" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { CartItem } from '@/types'

const props = defineProps<{ item: CartItem; index: number }>()
const emit = defineEmits<{ (e: 'update', index: number, quantity: number): void }>()

const defaultImage = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" fill="%23ebedf0"><rect width="100" height="100"/></svg>')
const quantity = ref(props.item.quantity)

watch(() => props.item.quantity, (val) => {
  quantity.value = val
})

function handleQuantityChange() {
  emit('update', props.index, quantity.value)
}
</script>

<style scoped lang="scss">
.cart-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;

  .cart-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
  }

  .cart-name {
    font-size: 15px;
    font-weight: 500;
  }

  .cart-specs {
    font-size: 12px;
    color: #969799;
    margin-top: 4px;
  }

  .cart-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;

    .price {
      color: #ee0a24;
      font-size: 15px;
      font-weight: 600;
    }
  }
}
</style>
