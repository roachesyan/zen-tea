<template>
  <div class="empty-state">
    <div class="empty-illustration">
      <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="60" cy="60" r="50" fill="#f5ebe0" />
        <path d="M40 55h40v30a4 4 0 0 1-4 4H44a4 4 0 0 1-4-4V55z" fill="#c9a882" />
        <path d="M50 45c0-5.5 4.5-10 10-10s10 4.5 10 10v10H50V45z" fill="#d4915e" />
        <circle cx="55" cy="67" r="3" fill="#f5ebe0" opacity="0.7" />
        <circle cx="65" cy="72" r="2" fill="#f5ebe0" opacity="0.5" />
        <path d="M38 55h44" stroke="#a0785a" stroke-width="2" stroke-linecap="round" />
      </svg>
    </div>
    <div class="empty-text">{{ description }}</div>
    <div class="empty-hint">敬请期待更多商品上架</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  description?: string
  image?: string
}>()
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.empty-state {
  padding: 48px 0 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-illustration {
  width: 120px;
  height: 120px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
  color: $text-secondary;
  margin-bottom: 4px;
}

.empty-hint {
  font-size: 12px;
  color: $tea-primary-light;
}
</style>
