<template>
  <div class="empty-state">
    <van-empty :description="description" :image="image">
      <slot />
    </van-empty>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  description?: string
  image?: string
}>()
</script>

<style scoped lang="scss">
.empty-state {
  padding: 60px 0;
}
</style>
