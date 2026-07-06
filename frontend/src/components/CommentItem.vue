<template>
  <div class="comment-item" :class="{ 'is-child': depth > 0 }">
    <div class="comment-header">
      <el-avatar :size="28" :src="resolveAvatarUrl(comment.userAvatar)" class="comment-avatar">
        {{ comment.userNickname?.charAt(0) || 'U' }}
      </el-avatar>
      <span class="comment-user" @click="goToUser(comment.userId)">{{ comment.userNickname }}</span>
      <span v-if="comment.replyToNickname" class="reply-to">
        回复 <span class="reply-target">{{ comment.replyToNickname }}</span>
      </span>
      <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
      <el-dropdown v-if="isCommentOwner" trigger="click" class="comment-more">
        <el-button size="small" text type="info" :icon="MoreFilled" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleDelete">
              <el-icon><Delete /></el-icon>
              删除评论
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="comment-content">{{ comment.content }}</div>
    <div class="comment-actions">
      <el-button size="small" text type="primary" @click="showReplyInput = !showReplyInput">
        回复
      </el-button>
    </div>

    <!-- 回复输入框 -->
    <div v-if="showReplyInput" class="reply-input">
      <el-input
        v-model="replyText"
        :placeholder="`回复 ${comment.userNickname}...`"
        maxlength="500"
        size="small"
      >
        <template #append>
          <el-button :loading="replyLoading" @click="handleReplySubmit">
            发送
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 子评论 -->
    <div v-if="comment.children?.length" class="child-comments">
      <CommentItem
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        :answer-id="answerId"
        :depth="depth + 1"
        @reply="(parentId: number, replyToId: number, nickname: string) => emit('reply', parentId, replyToId, nickname)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { MoreFilled, Delete } from '@element-plus/icons-vue'
import { qaApi } from '@/api/qa'
import { useUserStore } from '@/stores/user'
import type { Comment } from '@/types'

const props = defineProps<{
  comment: Comment
  answerId: number
  depth?: number
}>()

const emit = defineEmits<{
  (e: 'reply', parentId: number, replyToId: number, nickname: string): void
  (e: 'deleted'): void
}>()

const router = useRouter()
const userStore = useUserStore()

const showReplyInput = ref(false)
const replyText = ref('')
const replyLoading = ref(false)
const deleteLoading = ref(false)

const isCommentOwner = computed(() => {
  return userStore.userInfo?.id === props.comment.userId
})

function goToUser(userId: number) {
  router.push(`/student/user/${userId}`)
}

function resolveAvatarUrl(avatar?: string): string {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) return avatar
  return 'http://localhost:8080' + (avatar.startsWith('/') ? '' : '/') + avatar
}

async function handleReplySubmit() {
  const text = replyText.value.trim()
  if (!text) return
  replyLoading.value = true
  try {
    await qaApi.addComment({
      answerId: props.answerId,
      content: text,
      parentId: props.comment.parentId > 0 ? props.comment.parentId : props.comment.id,
      replyToId: props.comment.userId,
    })
    replyText.value = ''
    showReplyInput.value = false
    // 通知父组件刷新
    emit('reply', props.comment.id, props.comment.userId, props.comment.userNickname)
  } catch {
    // ignore
  } finally {
    replyLoading.value = false
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
  } catch {
    return // 用户取消
  }
  deleteLoading.value = true
  try {
    await qaApi.deleteComment(props.comment.id)
    emit('deleted')
  } catch {
    // error handled by interceptor
  } finally {
    deleteLoading.value = false
  }
}

function formatTime(time: string) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.comment-item {
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-item.is-child {
  margin-left: 24px;
  padding-left: 12px;
  border-left: 2px solid #e8e8e8;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-avatar {
  flex-shrink: 0;
  cursor: pointer;
}

.comment-more {
  margin-left: auto;
  flex-shrink: 0;
}

.comment-user {
  font-size: 13px;
  font-weight: 500;
  color: #409eff;
  cursor: pointer;
}

.comment-user:hover {
  text-decoration: underline;
}

.reply-to {
  font-size: 12px;
  color: #909399;
}

.reply-target {
  color: #409eff;
}

.comment-time {
  font-size: 11px;
  color: #c0c4cc;
}

.comment-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 4px;
}

.comment-actions {
  margin-bottom: 4px;
}

.reply-input {
  margin: 8px 0;
}

.child-comments {
  margin-top: 4px;
}
</style>