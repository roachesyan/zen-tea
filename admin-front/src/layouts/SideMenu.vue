<template>
  <a-menu
    theme="dark"
    mode="inline"
    :selected-keys="selectedKeys"
    @click="handleMenuClick"
  >
    <a-menu-item key="/dashboard">
      <template #icon><DashboardOutlined /></template>
      <span>数据看板</span>
    </a-menu-item>
    <a-menu-item key="/category">
      <template #icon><AppstoreOutlined /></template>
      <span>分类管理</span>
    </a-menu-item>
    <a-menu-item key="/product">
      <template #icon><ShoppingOutlined /></template>
      <span>商品管理</span>
    </a-menu-item>
    <a-menu-item key="/order">
      <template #icon><FileTextOutlined /></template>
      <span>订单管理</span>
    </a-menu-item>
  </a-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DashboardOutlined,
  AppstoreOutlined,
  ShoppingOutlined,
  FileTextOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()

const selectedKeys = computed(() => [route.path])

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}
</script>
