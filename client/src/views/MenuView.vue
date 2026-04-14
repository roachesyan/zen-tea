<template>
  <div class="page menu-page">
    <van-nav-bar title="菜单" />

    <div class="menu-body">
      <div class="menu-sidebar">
        <van-sidebar v-model="activeCategory" @change="handleCategoryChange">
          <van-sidebar-item
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
          />
        </van-sidebar>
      </div>

      <div class="menu-content">
        <van-loading v-if="loading" class="menu-loading" />
        <EmptyState v-else-if="products.length === 0" description="暂无商品" />
        <template v-else>
          <ProductCard
            v-for="product in products"
            :key="product.id"
            :product="product"
            @add="openSpec(product)"
          />
        </template>
      </div>
    </div>

    <SpecSelector
      v-model:show="specVisible"
      :price="selectedProduct?.price ?? 0"
      @confirm="handleSpecConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showSuccessToast } from 'vant'
import ProductCard from '@/components/product/ProductCard.vue'
import SpecSelector from '@/components/product/SpecSelector.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useProductStore } from '@/stores/product'
import { useCartStore } from '@/stores/cart'
import type { Product, ProductSpecs } from '@/types'

const route = useRoute()
const productStore = useProductStore()
const cartStore = useCartStore()

const activeCategory = ref(0)
const loading = ref(false)
const specVisible = ref(false)
const selectedProduct = ref<Product | null>(null)

const categories = ref(productStore.categories)
const products = ref<Product[]>([])

onMounted(async () => {
  await productStore.fetchCategories()
  categories.value = productStore.categories

  if (route.query.categoryId) {
    const idx = categories.value.findIndex((c) => c.id === Number(route.query.categoryId))
    if (idx >= 0) activeCategory.value = idx
  }

  await loadProducts()
})

watch(() => route.query.categoryId, (val) => {
  if (val) {
    const idx = categories.value.findIndex((c) => c.id === Number(val))
    if (idx >= 0) activeCategory.value = idx
    loadProducts()
  }
})

async function handleCategoryChange(index: number) {
  activeCategory.value = index
  await loadProducts()
}

async function loadProducts() {
  const cat = categories.value[activeCategory.value]
  if (!cat) return
  loading.value = true
  try {
    await productStore.fetchProducts(cat.id)
    products.value = productStore.products
  } finally {
    loading.value = false
  }
}

function openSpec(product: Product) {
  selectedProduct.value = product
  specVisible.value = true
}

function handleSpecConfirm(specs: ProductSpecs) {
  if (!selectedProduct.value) return
  cartStore.addItem(
    selectedProduct.value.id,
    selectedProduct.value.name,
    selectedProduct.value.price,
    selectedProduct.value.imageUrl ?? '',
    specs,
  )
  showSuccessToast('已加入购物车')
}
</script>

<style scoped lang="scss">
.menu-body {
  display: flex;
  height: calc(100vh - 46px - 50px);
}

.menu-sidebar {
  width: 90px;
  flex-shrink: 0;
  overflow-y: auto;
  background: #f7f8fa;
}

.menu-content {
  flex: 1;
  overflow-y: auto;
  background: #fff;
}

.menu-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
</style>
