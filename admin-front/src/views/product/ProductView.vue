<template>
  <div>
    <PageHeader title="商品管理">
      <a-button type="primary" @click="openCreate">
        <PlusOutlined /> 新建商品
      </a-button>
    </PageHeader>

    <div class="filter-bar">
      <a-space>
        <a-select
          v-model:value="filters.categoryId"
          placeholder="选择分类"
          allow-clear
          style="width: 160px"
          @change="fetchProducts"
        >
          <a-select-option v-for="cat in categoryList" :key="cat.id" :value="cat.id">
            {{ cat.name }}
          </a-select-option>
        </a-select>
        <a-input-search
          v-model:value="filters.keyword"
          placeholder="搜索商品"
          style="width: 200px"
          @search="fetchProducts"
        />
      </a-space>
    </div>

    <a-table
      :columns="columns"
      :data-source="products"
      :loading="loading"
      row-key="id"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'price'">
          ¥{{ record.price.toFixed(2) }}
        </template>
        <template v-if="column.key === 'status'">
          <ProductStatusSwitch
            :product-id="record.id"
            :status="record.status"
            @change="fetchProducts"
          />
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除该商品？" @confirm="handleDelete(record.id)">
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <ProductForm
      :open="formOpen"
      :product="editingProduct"
      :categories="categoryList"
      @update:open="formOpen = $event"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import ProductStatusSwitch from '@/components/product/ProductStatusSwitch.vue'
import ProductForm from '@/components/product/ProductForm.vue'
import { getCategories } from '@/api/category'
import { getProducts, createProduct, updateProduct, deleteProduct } from '@/api/product'
import type { Category, Product, ProductCreateRequest, ProductUpdateRequest } from '@/types'

const loading = ref(false)
const products = ref<Product[]>([])
const categoryList = ref<Category[]>([])
const formOpen = ref(false)
const editingProduct = ref<Product | null>(null)

const filters = reactive({
  categoryId: undefined as number | undefined,
  keyword: '',
})

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 70 },
  { title: '商品名称', dataIndex: 'name', key: 'name' },
  { title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 120 },
  { title: '价格', dataIndex: 'price', key: 'price', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '操作', key: 'action', width: 160 },
]

async function fetchProducts() {
  loading.value = true
  try {
    const { data: res } = await getProducts({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      categoryId: filters.categoryId,
      keyword: filters.keyword || undefined,
    })
    products.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const { data: res } = await getCategories()
    categoryList.value = res.data
  } catch {
    // handled by interceptor
  }
}

function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current ?? 1
  pagination.pageSize = pag.pageSize ?? 10
  fetchProducts()
}

function openCreate() {
  editingProduct.value = null
  formOpen.value = true
}

function openEdit(record: Product) {
  editingProduct.value = record
  formOpen.value = true
}

async function handleFormSubmit(data: ProductCreateRequest | ProductUpdateRequest) {
  try {
    if (editingProduct.value) {
      await updateProduct(editingProduct.value.id, data as ProductUpdateRequest)
      message.success('更新成功')
    } else {
      await createProduct(data as ProductCreateRequest)
      message.success('创建成功')
    }
    formOpen.value = false
    await fetchProducts()
  } catch (err) {
    message.error((err as Error).message || '操作失败')
  }
}

async function handleDelete(id: number) {
  try {
    await deleteProduct(id)
    message.success('删除成功')
    await fetchProducts()
  } catch (err) {
    message.error((err as Error).message || '删除失败')
  }
}

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped lang="less">
.filter-bar {
  margin-bottom: 16px;
}
</style>
