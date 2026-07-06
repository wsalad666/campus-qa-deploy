<template>
  <div class="similar-questions" :class="mode">
    <div v-if="mode === 'inline'" class="similar-header">
      <h3>相似相关提问</h3>
    </div>
    <div v-else class="similar-header-sidebar">
      <el-icon><InfoFilled /></el-icon>
      <span>相似相关问题</span>
    </div>

    <div v-loading="loading" class="similar-list">
      <div
        v-for="q in questions"
        :key="q.id"
        class="similar-item"
        @click="goToQuestion(q.id)"
      >
        <div class="similar-item-header">
          <h4 class="similar-title">{{ q.title }}</h4>
          <el-tag size="small" type="success">{{ Math.round(q.similarity) }}% 相似</el-tag>
        </div>
        <div class="similar-meta">
          <span><el-icon><View /></el-icon> {{ q.viewCount }}</span>
          <span><el-icon><ChatLineSquare /></el-icon> {{ q.answerCount }}</span>
        </div>
      </div>
      <div v-if="!loading && questions.length === 0 && hasSearched" class="similar-empty">
        <span v-if="mode === 'inline'">暂无相似问题</span>
        <span v-else>暂无相似问题</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { View, ChatLineSquare, InfoFilled } from '@element-plus/icons-vue'
import { qaApi } from '@/api/qa'
import type { SimilarQuestion } from '@/types'

const props = defineProps<{
  courseId: number
  excludeId?: number
  title?: string
  content?: string
  mode?: 'sidebar' | 'inline'
}>()

const router = useRouter()
const questions = ref<SimilarQuestion[]>([])
const loading = ref(false)
const hasSearched = ref(false)

let debounceTimer: ReturnType<typeof setTimeout> | null = null

async function fetchSimilar() {
  if (!props.courseId) return
  loading.value = true
  try {
    const res: any = await qaApi.getSimilarQuestions({
      courseId: props.courseId,
      excludeId: props.excludeId,
      title: props.title || undefined,
      content: props.content || undefined,
      limit: 5,
    })
    questions.value = res || []
    hasSearched.value = true
  } catch {
    questions.value = []
  } finally {
    loading.value = false
  }
}

function goToQuestion(id: number) {
  router.push(`/student/qa/${id}`)
}

// Debounced search when title/content changes
watch(
  () => [props.courseId, props.title, props.content],
  () => {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      fetchSimilar()
    }, 500)
  },
  { immediate: false }
)

onMounted(() => {
  if (props.courseId) fetchSimilar()
})
</script>

<style scoped>
.similar-questions.sidebar {
  width: 100%;
}
.similar-questions.inline {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-top: 24px;
}
.similar-header {
  margin-bottom: 16px;
}
.similar-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.similar-header-sidebar {
  font-size: 12px;
  color: #e6a23c;
  padding: 10px;
  background: #fdf6ec;
  border-radius: 6px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.similar-list {
  min-height: 40px;
}
.similar-item {
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.similar-item:hover {
  border-color: #409eff;
  background: #f5f7fa;
}
.similar-item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 6px;
}
.similar-title {
  margin: 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  flex: 1;
}
.similar-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}
.similar-meta span {
  display: flex;
  align-items: center;
  gap: 3px;
}
.similar-empty {
  font-size: 13px;
  color: #c0c4cc;
  text-align: center;
  padding: 20px 0;
}
</style>