<template>
  <div class="product-card" @click="$emit('click')">
    <div class="product-image">
      <van-image :src="product.imageUrl || defaultImage" fit="cover" width="100%" height="100%" />
    </div>
    <div class="product-info">
      <div class="product-name">{{ product.name }}</div>
      <div class="product-desc van-ellipsis">{{ product.description }}</div>
      <div class="product-bottom">
        <span class="price">
          <span class="price-symbol">¥</span>{{ product.price.toFixed(2) }}
        </span>
        <van-button class="add-btn" size="mini" round @click.stop="$emit('add')">选规格</van-button>
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
@import '@/assets/styles/variables.scss';

.product-card {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  transition: background 0.15s ease;

  &:active {
    background: $tea-warm-bg;
  }

  & + .product-card {
    border-top: 1px solid $tea-primary-lighter;
  }

  .product-image {
    width: 90px;
    height: 90px;
    border-radius: 10px;
    overflow: hidden;
    flex-shrink: 0;
    background: $tea-primary-lighter;
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
    font-weight: 600;
    color: $text-color;
  }

  .product-desc {
    font-size: 12px;
    color: $text-secondary;
    margin-top: 4px;
  }

  .product-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;

    .price {
      color: $tea-accent;
      font-size: 17px;
      font-weight: 700;

      .price-symbol {
        font-size: 12px;
        margin-right: 1px;
      }
    }

    .add-btn {
      background: linear-gradient(135deg, $tea-primary, $tea-accent);
      border: none;
      color: #fff;
      font-size: 12px;
      padding: 0 12px;
    }
  }
}
</style>
