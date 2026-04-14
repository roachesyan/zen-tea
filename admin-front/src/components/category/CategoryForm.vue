<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑分类' : '新建分类'"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="分类名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入分类名称" />
      </a-form-item>
      <a-form-item label="图标" name="icon">
        <a-input v-model:value="formState.icon" placeholder="请输入图标 (如 emoji)" />
      </a-form-item>
      <a-form-item label="排序" name="sort">
        <a-input-number v-model:value="formState.sort" :min="0" style="width: 100%" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import type { Category, CategoryCreateRequest, CategoryUpdateRequest } from '@/types'

const props = defineProps<{
  open: boolean
  category?: Category | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: CategoryCreateRequest | CategoryUpdateRequest): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = ref(false)

const formState = reactive<CategoryCreateRequest>({
  name: '',
  icon: '',
  sort: 0,
})

const rules: Record<string, Rule[]> = {
  name: [{ required: true, message: '请输入分类名称' }],
}

watch(
  () => props.open,
  (val) => {
    if (val) {
      isEdit.value = !!props.category
      if (props.category) {
        formState.name = props.category.name
        formState.icon = props.category.icon ?? ''
        formState.sort = props.category.sort
      } else {
        formState.name = ''
        formState.icon = ''
        formState.sort = 0
      }
      formRef.value?.clearValidate()
    }
  },
)

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    loading.value = true
    emit('submit', { ...formState })
  } catch {
    // validation failed
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  emit('update:open', false)
}
</script>
