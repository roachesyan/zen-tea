<template>
  <div class="page login-page">
    <van-nav-bar title="登录" />

    <div class="login-header">
      <span class="logo-icon">🧋</span>
      <h2>禅茶 ZenTea</h2>
      <p>新鲜好茶，即点即享</p>
    </div>

    <div class="login-form">
      <template v-if="!isRegister">
        <van-cell-group inset>
          <van-field v-model="form.username" label="用户名" placeholder="请输入用户名" />
          <van-field v-model="form.password" type="password" label="密码" placeholder="请输入密码" />
        </van-cell-group>
        <div class="login-actions">
          <van-button round block type="primary" :loading="loading" @click="handleLogin">登录</van-button>
          <van-button round block plain @click="isRegister = true">没有账号？去注册</van-button>
        </div>
      </template>

      <template v-else>
        <van-cell-group inset>
          <van-field v-model="form.username" label="用户名" placeholder="请输入用户名" />
          <van-field v-model="form.password" type="password" label="密码" placeholder="请输入密码" />
          <van-field v-model="form.nickname" label="昵称" placeholder="请输入昵称（选填）" />
        </van-cell-group>
        <div class="login-actions">
          <van-button round block type="primary" :loading="loading" @click="handleRegister">注册</van-button>
          <van-button round block plain @click="isRegister = false">已有账号？去登录</van-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isRegister = ref(false)
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  nickname: '',
})

async function handleLogin() {
  if (!form.username || !form.password) {
    showFailToast('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    showSuccessToast('登录成功')
    const redirect = (route.query.redirect as string) || '/home'
    router.replace(redirect)
  } catch (err) {
    showFailToast((err as Error).message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!form.username || !form.password) {
    showFailToast('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await authStore.register(form.username, form.password)
    showSuccessToast('注册成功')
    const redirect = (route.query.redirect as string) || '/home'
    router.replace(redirect)
  } catch (err) {
    showFailToast((err as Error).message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-header {
  text-align: center;
  padding: 60px 0 40px;

  .logo-icon { font-size: 64px; display: block; margin-bottom: 12px; }
  h2 { font-size: 24px; margin: 0 0 8px; }
  p { font-size: 14px; color: #969799; margin: 0; }
}

.login-form {
  padding: 0 16px;
}

.login-actions {
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
