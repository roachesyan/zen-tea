import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '数据看板', icon: 'DashboardOutlined' },
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/CategoryView.vue'),
        meta: { title: '分类管理', icon: 'AppstoreOutlined' },
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/ProductView.vue'),
        meta: { title: '商品管理', icon: 'ShoppingOutlined' },
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/OrderView.vue'),
        meta: { title: '订单管理', icon: 'FileTextOutlined' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
