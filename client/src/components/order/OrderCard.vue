<template>
  <div class="order-card" @click="$emit('click')">
    <div class="order-header">
      <span class="order-no">{{ order.orderNo }}</span>
      <span :class="['order-status', `status-${order.status.toLowerCase()}`]">{{ statusText }}</span>
    </div>
    <div class="order-items">
      <div v-for="item in order.items" :key="item.id" class="order-item">
        <span class="item-name">{{ item.productName }} x{{ item.quantity }}</span>
        <span class="item-price">¥{{ item.subtotal.toFixed(2) }}</span>
      </div>
    </div>
    <div class="order-footer">
      <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
      <span class="order-total">合计: <b>¥{{ order.totalAmount.toFixed(2) }}</b></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Order } from '@/types'
import { formatDateTime, getOrderStatusText } from '@/utils/format'

const props = defineProps<{ order: Order }>()
defineEmits<{ (e: 'click'): void }>()

const statusText = computed(() => getOrderStatusText(props.order.status))
</script>

<style scoped lang="scss">
.order-card {
  margin: 12px 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .order-no {
      font-size: 13px;
      color: #969799;
    }

    .order-status {
      font-size: 13px;
      font-weight: 500;

      &.status-pending { color: #ff976a; }
      &.status-making { color: #1989fa; }
      &.status-done { color: #07c160; }
      &.status-cancelled { color: #969799; }
    }
  }

  .order-items {
    .order-item {
      display: flex;
      justify-content: space-between;
      padding: 4px 0;
      font-size: 14px;

      .item-name { color: #323233; }
      .item-price { color: #323233; }
    }
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f5f5f5;

    .order-time {
      font-size: 12px;
      color: #969799;
    }

    .order-total {
      font-size: 14px;

      b {
        color: #ee0a24;
        font-size: 16px;
      }
    }
  }
}
</style>
