<template>
  <div class="page order-page">
    <van-nav-bar title="我的订单" />

    <van-tabs v-model:active="activeTab" sticky @change="handleTabChange">
      <van-tab title="全部" name="" />
      <van-tab title="待处理" name="PENDING" />
      <van-tab title="制作中" name="MAKING" />
      <van-tab title="已完成" name="DONE" />
    </van-tabs>

    <van-loading v-if="loading" class="order-loading" />
    <EmptyState v-else-if="orders.length === 0" description="暂无订单" />
    <div v-else class="order-list">
      <OrderCard
        v-for="order in orders"
        :key="order.id"
        :order="order"
        @click="showOrderDetail(order)"
      />
    </div>

    <van-action-sheet v-model:show="detailVisible" title="订单详情">
      <div v-if="selectedOrder" class="order-detail">
        <div class="detail-header">
          <span class="detail-no">{{ selectedOrder.orderNo }}</span>
        </div>
        <div class="detail-items">
          <div v-for="item in selectedOrder.items" :key="item.id" class="detail-item">
            <span>{{ item.productName }} x{{ item.quantity }}</span>
            <span>¥{{ item.subtotal.toFixed(2) }}</span>
          </div>
        </div>
        <div class="detail-remark" v-if="selectedOrder.remark">
          <span>备注: {{ selectedOrder.remark }}</span>
        </div>
        <div class="detail-footer">
          <span>合计: <b class="price">¥{{ selectedOrder.totalAmount.toFixed(2) }}</b></span>
        </div>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import OrderCard from '@/components/order/OrderCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { getMyOrders } from '@/api/order'
import type { Order } from '@/types'

const loading = ref(false)
const orders = ref<Order[]>([])
const activeTab = ref('')
const detailVisible = ref(false)
const selectedOrder = ref<Order | null>(null)

onMounted(() => {
  fetchOrders()
})

async function fetchOrders() {
  loading.value = true
  try {
    const { data: res } = await getMyOrders({
      pageNum: 1,
      pageSize: 50,
      status: activeTab.value || undefined,
    })
    orders.value = res.data.records
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  fetchOrders()
}

function showOrderDetail(order: Order) {
  selectedOrder.value = order
  detailVisible.value = true
}
</script>

<style scoped lang="scss">
.order-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.order-list {
  padding-top: 8px;
}

.order-detail {
  padding: 16px;
  padding-bottom: 32px;

  .detail-header {
    margin-bottom: 12px;
    .detail-no { font-size: 13px; color: #969799; }
  }

  .detail-items {
    .detail-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      font-size: 14px;
    }
  }

  .detail-remark {
    padding: 8px 0;
    font-size: 13px;
    color: #969799;
  }

  .detail-footer {
    text-align: right;
    padding-top: 12px;
    border-top: 1px solid #f5f5f5;
    font-size: 15px;

    b { font-size: 18px; }
  }
}
</style>
