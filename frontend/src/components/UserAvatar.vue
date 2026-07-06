<template>
  <el-avatar :size="size" :src="resolvedSrc">
    <span v-if="!resolvedSrc" class="avatar-letter">{{ firstLetter }}</span>
  </el-avatar>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  src?: string
  size?: number
  nickname?: string
}>(), {
  size: 40,
  nickname: '',
})

const firstLetter = computed(() => {
  if (props.nickname) {
    return props.nickname.charAt(0).toUpperCase()
  }
  return 'U'
})

const resolvedSrc = computed(() => {
  if (!props.src) return ''
  if (props.src.startsWith('http://') || props.src.startsWith('https://') || props.src.startsWith('data:')) {
    return props.src
  }
  return 'http://localhost:8080' + (props.src.startsWith('/') ? '' : '/') + props.src
})
</script>

<style scoped>
.avatar-letter {
  font-size: 16px;
  font-weight: 500;
}
</style>