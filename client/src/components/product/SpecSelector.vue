<template>
  <van-action-sheet v-model:show="visible" title="选择规格" @close="$emit('update:show', false)">
    <div class="spec-selector">
      <div class="spec-section">
        <div class="spec-title">冰度</div>
        <van-radio-group v-model="specs.ice" direction="horizontal">
          <van-radio v-for="opt in iceOptions" :key="opt.value" :name="opt.value">{{ opt.label }}</van-radio>
        </van-radio-group>
      </div>

      <div class="spec-section">
        <div class="spec-title">甜度</div>
        <van-radio-group v-model="specs.sweetness" direction="horizontal">
          <van-radio v-for="opt in sweetnessOptions" :key="opt.value" :name="opt.value">{{ opt.label }}</van-radio>
        </van-radio-group>
      </div>

      <div class="spec-section">
        <div class="spec-title">加料</div>
        <van-checkbox-group v-model="specs.toppings" direction="horizontal">
          <van-checkbox v-for="opt in toppingOptions" :key="opt.value" :name="opt.value">{{ opt.label }}</van-checkbox>
        </van-checkbox-group>
      </div>

      <div class="spec-footer">
        <div class="spec-price">¥{{ price.toFixed(2) }}</div>
        <van-button type="primary" round block @click="handleConfirm">加入购物车</van-button>
      </div>
    </div>
  </van-action-sheet>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { ProductSpecs } from '@/types'

const props = defineProps<{
  show: boolean
  price: number
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'confirm', specs: ProductSpecs): void
}>()

const visible = defineModel<boolean>('show', { default: false })

const specs = reactive<ProductSpecs>({
  ice: 'normal',
  sweetness: 'full',
  toppings: [],
})

watch(() => props.show, (val) => {
  if (val) {
    specs.ice = 'normal'
    specs.sweetness = 'full'
    specs.toppings = []
  }
})

const iceOptions = [
  { label: '正常冰', value: 'normal' },
  { label: '少冰', value: 'less' },
  { label: '去冰', value: 'no_ice' },
  { label: '热饮', value: 'hot' },
]

const sweetnessOptions = [
  { label: '全糖', value: 'full' },
  { label: '少糖', value: 'less' },
  { label: '半糖', value: 'half' },
  { label: '微糖', value: 'little' },
  { label: '无糖', value: 'none' },
]

const toppingOptions = [
  { label: '珍珠', value: 'pearl' },
  { label: '椰果', value: 'coconut_jelly' },
  { label: '芋圆', value: 'taro_ball' },
  { label: '红豆', value: 'red_bean' },
]

function handleConfirm() {
  emit('confirm', { ...specs })
  emit('update:show', false)
}
</script>

<style scoped lang="scss">
.spec-selector {
  padding: 16px;
  padding-bottom: 32px;
}

.spec-section {
  margin-bottom: 20px;

  .spec-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
  }
}

.spec-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebedf0;

  .spec-price {
    font-size: 22px;
    font-weight: 600;
    color: #ee0a24;
    white-space: nowrap;
  }
}
</style>
