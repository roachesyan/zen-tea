<template>
  <div class="page-header">
    <h2>{{ title }}</h2>
    <div class="actions">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string }>()
</script>

<style scoped lang="less">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  .actions {
    display: flex;
    gap: 8px;
  }
}
</style>
