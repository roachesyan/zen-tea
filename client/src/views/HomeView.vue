<template>
  <div class="page home-page">
    <van-nav-bar title="禅茶 ZenTea" />

    <van-swipe :autoplay="3000" indicator-color="#1989fa" class="home-swipe">
      <van-swipe-item v-for="item in banners" :key="item.id">
        <div class="banner-item" :style="{ background: item.bg }">
          <div class="banner-text">{{ item.text }}</div>
        </div>
      </van-swipe-item>
    </van-swipe>

    <div class="category-grid">
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="category-item"
        @click="goToCategory(cat.id)"
      >
        <span class="category-icon">{{ cat.icon }}</span>
        <span class="category-name">{{ cat.name }}</span>
      </div>
    </div>

    <div class="section">
      <div class="section-title">推荐茶饮</div>
      <div v-if="loading" style="padding: 20px; text-align: center;">
        <van-loading />
      </div>
      <div v-else-if="recommendProducts.length === 0" style="padding: 20px;">
        <EmptyState description="暂无推荐商品" />
      </div>
      <div v-else class="product-list">
        <ProductCard
          v-for="product in recommendProducts"
          :key="product.id"
          :product="product"
          @click="goToMenu"
          @add="openSpec(product)"
        />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import ProductCard from '@/components/product/ProductCard.vue'
import SpecSelector from '@/components/product/SpecSelector.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useProductStore } from '@/stores/product'
import { useCartStore } from '@/stores/cart'
import type { Product, ProductSpecs } from '@/types'

const router = useRouter()
const productStore = useProductStore()
const cartStore = useCartStore()

const loading = ref(false)
const specVisible = ref(false)
const selectedProduct = ref<Product | null>(null)

const categories = ref(productStore.categories)
const recommendProducts = ref<Product[]>([])

const banners = [
  { id: 1, text: '今日特惠 全场8折', bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 2, text: '新品尝鲜 买一送一', bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { id: 3, text: '会员积分 双倍返还', bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
]

onMounted(async () => {
  loading.value = true
  try {
    await productStore.fetchCategories()
    categories.value = productStore.categories
    await productStore.fetchProducts()
    recommendProducts.value = productStore.products.slice(0, 6)
  } finally {
    loading.value = false
  }
})

function goToCategory(categoryId: number) {
  router.push({ path: '/menu', query: { categoryId: String(categoryId) } })
}

function goToMenu() {
  router.push('/menu')
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
.home-swipe {
  .banner-item {
    height: 160px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;

    .banner-text {
      font-size: 22px;
      font-weight: 600;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  padding: 16px;
  background: #fff;
  margin-bottom: 12px;

  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px 0;

    .category-icon { font-size: 28px; }
    .category-name { font-size: 12px; color: #323233; }
  }
}

.section {
  .section-title {
    padding: 16px;
    font-size: 17px;
    font-weight: 600;
    background: #fff;
  }
}

.product-list {
  background: #fff;
}
</style>
