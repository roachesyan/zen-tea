<template>
  <a-layout-header class="header-bar">
    <div class="header-left">
      <MenuFoldOutlined
        v-if="!appStore.collapsed"
        class="trigger"
        @click="appStore.toggleCollapsed"
      />
      <MenuUnfoldOutlined
        v-else
        class="trigger"
        @click="appStore.toggleCollapsed"
      />
      <a-breadcrumb class="header-breadcrumb">
        <a-breadcrumb-item>
          <router-link to="/dashboard">首页</router-link>
        </a-breadcrumb-item>
        <a-breadcrumb-item v-if="route.meta.title">
          {{ route.meta.title }}
        </a-breadcrumb-item>
      </a-breadcrumb>
    </div>
    <div class="header-right">
      <a-dropdown>
        <span class="user-info">
          <UserOutlined />
          <span class="username">{{ authStore.user?.nickname || authStore.user?.username || '管理员' }}</span>
        </span>
        <template #overlay>
          <a-menu>
            <a-menu-item @click="handleLogout">
              <LogoutOutlined />
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { Modal } from 'ant-design-vue'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

function handleLogout() {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    async onOk() {
      await authStore.logout()
      router.push('/login')
    },
  })
}
</script>

<style scoped lang="less">
.header-bar {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 9;
  height: 64px;
  line-height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.trigger {
  font-size: 18px;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #1890ff;
  }
}

.header-breadcrumb {
  line-height: 64px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  .username {
    font-size: 14px;
  }
}
</style>
