<template>
  <div class="page menu-page">
    <van-nav-bar title="菜单" class="menu-nav" />

    <div class="menu-body">
      <div class="menu-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-header-text">分类</span>
        </div>
        <van-sidebar v-model="activeCategory" @change="handleCategoryChange">
          <van-sidebar-item
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :title="cat.name"
          />
        </van-sidebar>
      </div>

      <div class="menu-content">
        <div v-if="activeCat" class="category-banner">
          <span class="category-banner-name">{{ activeCat.name }}</span>
          <span class="category-banner-count">{{ products.length }} 款</span>
        </div>
        <div v-if="loading" class="menu-skeleton">
          <van-skeleton
            v-for="i in 6"
            :key="i"
            title
            avatar
            avatar-shape="square"
            avatar-size="90px"
            :row="2"
            class="product-skeleton"
          />
        </div>
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
import { ref, computed, onMounted, watch } from 'vue'
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

const activeCat = computed(() => categories.value[activeCategory.value])

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
@import '@/assets/styles/variables.scss';

.menu-nav {
  :deep(.van-nav-bar) {
    background: linear-gradient(135deg, $tea-primary, $tea-accent);
  }
  :deep(.van-nav-bar__title) {
    // color: #fff;
    font-weight: 600;
  }
}

.menu-body {
  display: flex;
  height: calc(100vh - 46px - 50px);
}

.menu-sidebar {
  width: 90px;
  flex-shrink: 0;
  overflow-y: auto;
  background: $tea-warm-bg;

  .sidebar-header {
    padding: 14px 0 8px;
    text-align: center;

    .sidebar-header-text {
      font-size: 11px;
      color: $tea-primary;
      font-weight: 600;
      letter-spacing: 2px;
    }
  }

  :deep(.van-sidebar) {
    background: transparent;
  }

  :deep(.van-sidebar-item) {
    background: transparent;
    color: $text-secondary;
    font-size: 13px;
    padding: 14px 8px;
    transition: all 0.2s ease;
  }

  :deep(.van-sidebar-item--select) {
    background: #fff;
    color: $tea-primary;
    font-weight: 600;

    &::before {
      background: $tea-primary;
      border-radius: 0 3px 3px 0;
      width: 3px;
    }
  }
}

.menu-content {
  flex: 1;
  overflow-y: auto;
  background: #fff;
}

.category-banner {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 14px 16px 8px;
  border-bottom: 1px solid $tea-primary-lighter;

  .category-banner-name {
    font-size: 16px;
    font-weight: 600;
    color: $text-color;
  }

  .category-banner-count {
    font-size: 12px;
    color: $text-secondary;
  }
}

.menu-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
</style>
