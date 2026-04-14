<template>
  <a-switch
    :checked="checked"
    checked-children="上架"
    un-checked-children="下架"
    @change="handleChange"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { updateProductStatus } from '@/api/product'

const props = defineProps<{
  productId: number
  status: number
}>()

const emit = defineEmits<{
  (e: 'change'): void
}>()

const checked = ref(props.status === 1)

async function handleChange(val: boolean | string) {
  const newStatus = val ? 1 : 0
  try {
    await updateProductStatus(props.productId, newStatus)
    checked.value = val as boolean
    message.success(newStatus === 1 ? '已上架' : '已下架')
    emit('change')
  } catch (err) {
    message.error((err as Error).message || '操作失败')
  }
}
</script>
