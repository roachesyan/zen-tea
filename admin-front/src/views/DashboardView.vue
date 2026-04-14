<template>
  <div class="dashboard">
    <PageHeader title="数据看板" />

    <a-spin :spinning="loading">
      <a-row :gutter="16" class="stat-row">
        <a-col :span="6">
          <a-card>
            <a-statistic title="今日订单" :value="stats.todayOrderCount">
              <template #prefix><FileTextOutlined /></template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="今日营业额" :value="stats.todayRevenue" :precision="2" prefix="¥" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="待处理订单" :value="stats.pendingOrderCount" value-style="color: #faad14" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card>
            <a-statistic title="累计营业额" :value="stats.totalRevenue" :precision="2" prefix="¥" />
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { FileTextOutlined } from '@ant-design/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { getDashboardStats } from '@/api/order'
import type { DashboardStats } from '@/types'

const loading = ref(false)
const stats = reactive<DashboardStats>({
  todayOrderCount: 0,
  todayRevenue: 0,
  totalOrderCount: 0,
  totalRevenue: 0,
  pendingOrderCount: 0,
})

async function fetchStats() {
  loading.value = true
  try {
    const { data: res } = await getDashboardStats()
    Object.assign(stats, res.data)
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)
</script>

<style scoped lang="less">
.stat-row {
  .ant-col {
    margin-bottom: 16px;
  }
}
</style>
