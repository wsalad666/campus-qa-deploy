<template>
  <el-card class="question-card" shadow="hover" @click="goDetail">
    <div class="card-decor"></div>
    <div class="card-header">
      <div class="user-info" @click.stop="goToUser">
        <el-avatar :size="32" :src="resolveAvatarUrl(question.userAvatar)">
          {{ question.userNickname?.charAt(0) || 'U' }}
        </el-avatar>
        <span class="nickname">{{ question.userNickname }}</span>
      </div>
      <el-tag size="small" class="course-tag">📖 {{ question.courseName }}</el-tag>
    </div>

    <h3 class="title">{{ question.title }}</h3>
    <p class="content-preview">{{ truncatedContent }}</p>

    <div class="card-footer">
      <div class="stats">
        <span class="stat-item">
          <span class="stat-icon stat-icon-view"><el-icon><View /></el-icon></span>
          {{ question.viewCount }}
        </span>
        <span class="stat-item">
          <span class="stat-icon stat-icon-answer"><el-icon><ChatLineSquare /></el-icon></span>
          {{ question.answerCount }}
        </span>
        <span class="stat-item">
          <span class="stat-icon stat-icon-like"><el-icon><Pointer /></el-icon></span>
          {{ question.likeCount || 0 }}
        </span>
        <span class="stat-item">
          <span class="stat-icon stat-icon-fav"><el-icon><Star /></el-icon></span>
          {{ question.favoriteCount || 0 }}
        </span>
      </div>
      <span class="time">🕐 {{ formatTime(question.createTime) }}</span>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { formatRelativeTime } from '@/utils/time'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { View, ChatLineSquare, Pointer, Star } from '@element-plus/icons-vue'
import type { Question } from '@/types'

const props = defineProps<{
  question: Question
}>()

const router = useRouter()

const truncatedContent = computed(() => {
  const text = props.question.content.replace(/<[^>]+>/g, '')
  return text.length > 100 ? text.slice(0, 100) + '...' : text
})

function goDetail() {
  router.push(`/student/qa/${props.question.id}`)
}

function goToUser() {
  router.push(`/student/user/${props.question.userId}`)
}

function resolveAvatarUrl(avatar?: string): string {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) return avatar
  return '' + (avatar.startsWith('/') ? '' : '/') + avatar
}

function formatTime(time: string) { return formatRelativeTime(time) }
</script>

<style scoped>
.question-card {
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 12px;
  position: relative;
  overflow: visible;
}

.question-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1) !important;
}

.card-decor {
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 4px;
  border-radius: 2px;
  background: linear-gradient(180deg, #4f8ef7, #8b5cf6);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-info:hover .nickname {
  color: #4f8ef7;
}

.user-info .nickname {
  font-size: 13px;
  color: #666;
}

.course-tag {
  background: rgba(79, 142, 247, 0.08) !important;
  color: #4f8ef7 !important;
  border-color: rgba(79, 142, 247, 0.15) !important;
  font-size: 12px !important;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
  line-height: 1.4;
}

.content-preview {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin: 0 0 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats {
  display: flex;
  gap: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.stat-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  font-size: 12px;
}

.stat-icon-view {
  background: rgba(79, 142, 247, 0.1);
  color: #4f8ef7;
}

.stat-icon-answer {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.stat-icon-like {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.stat-icon-fav {
  background: rgba(236, 72, 153, 0.1);
  color: #ec4899;
}

.time {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
