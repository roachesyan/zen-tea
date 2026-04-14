<template>
  <a-tag :color="color">{{ text }}</a-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: number | string
  type?: 'category' | 'product' | 'order'
}>()

const type = props.type ?? 'product'

const configMap = {
  category: { 1: { text: '启用', color: 'green' }, 0: { text: '禁用', color: 'red' } },
  product: { 1: { text: '上架', color: 'green' }, 0: { text: '下架', color: 'red' } },
  order: {
    PENDING: { text: '待处理', color: 'orange' },
    MAKING: { text: '制作中', color: 'blue' },
    DONE: { text: '已完成', color: 'green' },
    CANCELLED: { text: '已取消', color: 'default' },
  },
} as const

const text = computed(() => {
  const map = configMap[type]
  return (map as Record<string | number, { text: string }>)[props.status]?.text ?? String(props.status)
})

const color = computed(() => {
  const map = configMap[type]
  return (map as Record<string | number, { color: string }>)[props.status]?.color ?? 'default'
})
</script>
