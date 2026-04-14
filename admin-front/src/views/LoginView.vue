<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <span class="logo-icon">🧋</span>
        <h1>ZenTea 管理后台</h1>
      </div>
      <a-form
        :model="formState"
        :rules="rules"
        @finish="handleLogin"
        layout="vertical"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formState.username"
            size="large"
            placeholder="用户名"
          >
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item name="password">
          <a-input-password
            v-model:value="formState.password"
            size="large"
            placeholder="密码"
          >
            <template #prefix><LockOutlined /></template>
          </a-input-password>
        </a-form-item>
        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            :loading="loading"
            block
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
})

const rules: Record<string, Rule[]> = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }],
}

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(formState.username, formState.password)
    message.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    message.error((err as Error).message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="less">
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    font-size: 48px;
    display: block;
    margin-bottom: 12px;
  }

  h1 {
    font-size: 24px;
    color: #333;
    margin: 0;
  }
}
</style>
