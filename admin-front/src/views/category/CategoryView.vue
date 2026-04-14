<template>
  <div>
    <PageHeader title="分类管理">
      <a-button type="primary" @click="openCreate">
        <PlusOutlined /> 新建分类
      </a-button>
    </PageHeader>

    <a-table
      :columns="columns"
      :data-source="categories"
      :loading="loading"
      row-key="id"
      :pagination="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'icon'">
          <span style="font-size: 24px">{{ record.icon }}</span>
        </template>
        <template v-if="column.key === 'status'">
          <StatusTag :status="record.status" type="category" />
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该分类？" @confirm="handleDelete(record.id)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <CategoryForm
      :open="formOpen"
      :category="editingCategory"
      @update:open="formOpen = $event"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import CategoryForm from '@/components/category/CategoryForm.vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import type { Category, CategoryCreateRequest, CategoryUpdateRequest } from '@/types'

const loading = ref(false)
const categories = ref<Category[]>([])
const formOpen = ref(false)
const editingCategory = ref<Category | null>(null)

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '图标', dataIndex: 'icon', key: 'icon', width: 80 },
  { title: '分类名称', dataIndex: 'name', key: 'name' },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 160 },
]

async function fetchCategories() {
  loading.value = true
  try {
    const { data: res } = await getCategories()
    categories.value = res.data
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingCategory.value = null
  formOpen.value = true
}

function openEdit(record: Category) {
  editingCategory.value = record
  formOpen.value = true
}

async function handleFormSubmit(data: CategoryCreateRequest | CategoryUpdateRequest) {
  try {
    if (editingCategory.value) {
      await updateCategory(editingCategory.value.id, data as CategoryUpdateRequest)
      message.success('更新成功')
    } else {
      await createCategory(data as CategoryCreateRequest)
      message.success('创建成功')
    }
    formOpen.value = false
    await fetchCategories()
  } catch (err) {
    message.error((err as Error).message || '操作失败')
  }
}

async function handleDelete(id: number) {
  try {
    await deleteCategory(id)
    message.success('删除成功')
    await fetchCategories()
  } catch (err) {
    message.error((err as Error).message || '删除失败')
  }
}

onMounted(fetchCategories)
</script>
