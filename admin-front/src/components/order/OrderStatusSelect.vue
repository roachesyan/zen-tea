<template>
  <a-select
    :value="status"
    style="width: 120px"
    @change="handleChange"
  >
    <a-select-option value="PENDING">待处理</a-select-option>
    <a-select-option value="MAKING">制作中</a-select-option>
    <a-select-option value="DONE">已完成</a-select-option>
    <a-select-option value="CANCELLED">已取消</a-select-option>
  </a-select>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { updateOrderStatus } from '@/api/order'

const props = defineProps<{
  orderId: number
  status: string
}>()

const emit = defineEmits<{
  (e: 'change'): void
}>()

async function handleChange(val: string) {
  try {
    await updateOrderStatus(props.orderId, val)
    message.success('状态更新成功')
    emit('change')
  } catch (err) {
    message.error((err as Error).message || '操作失败')
  }
}
</script>
