<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑商品' : '新建商品'"
    :confirm-loading="loading"
    width="600px"
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
      <a-form-item label="商品名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入商品名称" />
      </a-form-item>
      <a-form-item label="所属分类" name="categoryId">
        <a-select v-model:value="formState.categoryId" placeholder="请选择分类">
          <a-select-option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="价格" name="price">
        <a-input-number
          v-model:value="formState.price"
          :min="0.01"
          :precision="2"
          :step="1"
          style="width: 100%"
          placeholder="请输入价格"
        />
      </a-form-item>
      <a-form-item label="图片URL" name="imageUrl">
        <a-input v-model:value="formState.imageUrl" placeholder="请输入图片URL" />
      </a-form-item>
      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="formState.description" :rows="3" placeholder="请输入商品描述" />
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
import type { Category, Product, ProductCreateRequest, ProductUpdateRequest } from '@/types'

const props = defineProps<{
  open: boolean
  product?: Product | null
  categories: Category[]
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: ProductCreateRequest | ProductUpdateRequest): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = ref(false)

const formState = reactive<ProductCreateRequest>({
  name: '',
  categoryId: undefined as unknown as number,
  price: 0,
  imageUrl: '',
  description: '',
  sort: 0,
})

const rules: Record<string, Rule[]> = {
  name: [{ required: true, message: '请输入商品名称' }],
  categoryId: [{ required: true, message: '请选择分类' }],
  price: [{ required: true, message: '请输入价格' }],
}

watch(
  () => props.open,
  (val) => {
    if (val) {
      isEdit.value = !!props.product
      if (props.product) {
        formState.name = props.product.name
        formState.categoryId = props.product.categoryId
        formState.price = props.product.price
        formState.imageUrl = props.product.imageUrl ?? ''
        formState.description = props.product.description ?? ''
        formState.sort = props.product.sort
      } else {
        formState.name = ''
        formState.categoryId = undefined as unknown as number
        formState.price = 0
        formState.imageUrl = ''
        formState.description = ''
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
