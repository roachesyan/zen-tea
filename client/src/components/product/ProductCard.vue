<template>
  <div class="product-card" @click="$emit('click')">
    <div class="product-image">
      <van-image :src="product.imageUrl || defaultImage" fit="cover" width="100%" height="100%" />
    </div>
    <div class="product-info">
      <div class="product-name">{{ product.name }}</div>
      <div class="product-desc van-ellipsis">{{ product.description }}</div>
      <div class="product-bottom">
        <span class="price">¥{{ product.price.toFixed(2) }}</span>
        <van-button size="mini" type="primary" round @click.stop="$emit('add')">选规格</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Product } from '@/types'

defineProps<{ product: Product }>()
defineEmits<{ (e: 'click'): void; (e: 'add'): void }>()

const defaultImage = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" fill="%23ebedf0"><rect width="100" height="100"/></svg>')
</script>

<style scoped lang="scss">
.product-card {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;

  .product-image {
    width: 90px;
    height: 90px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
  }

  .product-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
  }

  .product-name {
    font-size: 15px;
    font-weight: 500;
  }

  .product-desc {
    font-size: 12px;
    color: #969799;
    margin-top: 4px;
  }

  .product-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;

    .price {
      color: #ee0a24;
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>
