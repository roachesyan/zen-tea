<template>
  <a-layout class="admin-layout">
    <a-layout-sider
      v-model:collapsed="appStore.collapsed"
      :trigger="null"
      collapsible
      :width="220"
      :collapsed-width="80"
      class="layout-sider"
    >
      <div class="logo">
        <span class="logo-icon">🧋</span>
        <span v-if="!appStore.collapsed" class="logo-text">ZenTea 管理</span>
      </div>
      <SideMenu />
    </a-layout-sider>
    <a-layout>
      <HeaderBar />
      <a-layout-content class="layout-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import SideMenu from './SideMenu.vue'
import HeaderBar from './HeaderBar.vue'

const appStore = useAppStore()
</script>

<style scoped lang="less">
.admin-layout {
  height: 100vh;
}

.layout-sider {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .logo-icon {
    font-size: 28px;
  }

  .logo-text {
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    white-space: nowrap;
  }
}

.layout-content {
  margin: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: 280px;
  overflow-y: auto;
}
</style>
