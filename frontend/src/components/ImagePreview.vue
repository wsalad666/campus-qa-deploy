<template>
  <el-dialog
    :model-value="visible"
    title="图片预览"
    width="80vw"
    :close-on-click-modal="true"
    destroy-on-close
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="image-preview-container">
      <div class="preview-main">
        <el-button
          v-if="images.length > 1"
          class="nav-btn prev-btn"
          circle
          :disabled="currentIndex <= 0"
          @click="prev"
        >
          <el-icon><ArrowLeft /></el-icon>
        </el-button>

        <div class="image-wrapper">
          <el-image
            :src="images[currentIndex]"
            fit="contain"
            :preview-src-list="images"
            :initial-index="currentIndex"
            hide-on-click-modal
          />
        </div>

        <el-button
          v-if="images.length > 1"
          class="nav-btn next-btn"
          circle
          :disabled="currentIndex >= images.length - 1"
          @click="next"
        >
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-if="images.length > 1" class="preview-counter">
        {{ currentIndex + 1 }} / {{ images.length }}
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  visible: boolean
  images: string[]
  initialIndex: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const currentIndex = ref(props.initialIndex)

watch(
  () => props.initialIndex,
  (val) => {
    currentIndex.value = val
  }
)

watch(
  () => props.visible,
  (val) => {
    if (val) {
      currentIndex.value = props.initialIndex
    }
  }
)

function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

function next() {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++
  }
}
</script>

<style scoped>
.image-preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-main {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  gap: 16px;
}

.nav-btn {
  flex-shrink: 0;
}

.image-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  max-height: 60vh;
}

.preview-counter {
  margin-top: 12px;
  font-size: 14px;
  color: #909399;
}
</style>