<template>
  <div>
    <PageHeader title="订单管理" />

    <div class="filter-bar">
      <a-radio-group v-model:value="statusFilter" button-style="solid" @change="handleFilterChange">
        <a-radio-button value="">全部</a-radio-button>
        <a-radio-button value="PENDING">待处理</a-radio-button>
        <a-radio-button value="MAKING">制作中</a-radio-button>
        <a-radio-button value="DONE">已完成</a-radio-button>
        <a-radio-button value="CANCELLED">已取消</a-radio-button>
      </a-radio-group>
    </div>

    <a-table
      :columns="columns"
      :data-source="orders"
      :loading="loading"
      row-key="id"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'totalAmount'">
          ¥{{ record.totalAmount.toFixed(2) }}
        </template>
        <template v-if="column.key === 'status'">
          <StatusTag :status="record.status" type="order" />
        </template>
        <template v-if="column.key === 'createTime'">
          {{ formatDateTime(record.createTime) }}
        </template>
        <template v-if="column.key === 'items'">
          <a-popover trigger="click">
            <template #content>
              <div class="items-popover">
                <div v-for="item in record.items" :key="item.id" class="item-row">
                  <span>{{ item.productName }} x{{ item.quantity }}</span>
                  <span>¥{{ item.subtotal.toFixed(2) }}</span>
                </div>
              </div>
            </template>
            <a-button type="link" size="small">{{ record.items.length }} 件商品</a-button>
          </a-popover>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <OrderStatusSelect
              :order-id="record.id"
              :status="record.status"
              @change="fetchOrders"
            />
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import OrderStatusSelect from '@/components/order/OrderStatusSelect.vue'
import { getOrders } from '@/api/order'
import { formatDateTime } from '@/utils/format'
import type { Order } from '@/types'

const loading = ref(false)
const orders = ref<Order[]>([])
const statusFilter = ref('')

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 200 },
  { title: '用户', dataIndex: 'username', key: 'username', width: 100 },
  { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 100 },
  { title: '商品', key: 'items', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '下单时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 140 },
]

async function fetchOrders() {
  loading.value = true
  try {
    const { data: res } = await getOrders({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      status: statusFilter.value || undefined,
    })
    orders.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current ?? 1
  pagination.pageSize = pag.pageSize ?? 10
  fetchOrders()
}

function handleFilterChange() {
  pagination.current = 1
  fetchOrders()
}

onMounted(fetchOrders)
</script>

<style scoped lang="less">
.filter-bar {
  margin-bottom: 16px;
}

.items-popover {
  min-width: 200px;

  .item-row {
    display: flex;
    justify-content: space-between;
    padding: 4px 0;

    &:not(:last-child) {
      border-bottom: 1px solid #f0f0f0;
    }
  }
}
</style>
