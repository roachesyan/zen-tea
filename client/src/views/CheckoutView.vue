<template>
  <div class="page checkout-page">
    <van-nav-bar title="确认订单" left-arrow @click-left="router.back()" />

    <div class="checkout-content">
      <div class="order-items">
        <div v-for="(item, idx) in cartStore.items" :key="idx" class="checkout-item">
          <div class="item-info">
            <span class="item-name">{{ item.productName }}</span>
            <span class="item-specs">{{ item.specsText }}</span>
          </div>
          <div class="item-right">
            <span class="item-qty">x{{ item.quantity }}</span>
            <span class="item-price">¥{{ (item.productPrice * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <van-cell-group inset>
        <van-field
          v-model="remark"
          label="备注"
          type="textarea"
          placeholder="请输入备注信息（选填）"
          rows="2"
          maxlength="200"
          show-word-limit
        />
      </van-cell-group>

      <div class="checkout-summary">
        <span>共 {{ cartStore.totalCount }} 件，合计：</span>
        <span class="price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
      </div>
    </div>

    <van-submit-bar
      :price="cartStore.totalPrice * 100"
      button-text="提交订单"
      :loading="submitting"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'

const router = useRouter()
const cartStore = useCartStore()
const remark = ref('')
const submitting = ref(false)

async function handleSubmit() {
  if (cartStore.items.length === 0) {
    showFailToast('购物车为空')
    return
  }

  submitting.value = true
  try {
    const { data: res } = await createOrder({
      items: cartStore.items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
        specs: JSON.stringify(item.specs),
      })),
      remark: remark.value || undefined,
    })
    cartStore.clearCart()
    showSuccessToast('下单成功')
    router.push('/order')
  } catch (err) {
    showFailToast((err as Error).message || '下单失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.checkout-content {
  padding: 12px 0 80px;
}

.order-items {
  background: #fff;
  margin-bottom: 12px;

  .checkout-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;

    &:not(:last-child) {
      border-bottom: 1px solid #f5f5f5;
    }

    .item-info {
      flex: 1;
      min-width: 0;

      .item-name { font-size: 15px; }
      .item-specs { font-size: 12px; color: #969799; margin-top: 2px; }
    }

    .item-right {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-shrink: 0;

      .item-qty { font-size: 13px; color: #969799; }
      .item-price { font-size: 14px; color: #323233; }
    }
  }
}

.checkout-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px;
  font-size: 14px;

  .price {
    color: #ee0a24;
    font-size: 18px;
    font-weight: 600;
    margin-left: 4px;
  }
}
</style>
