<template>
  <el-card class="answer-card" :class="{ 'is-accepted': isAccepted }">
    <div class="answer-decor" :class="{ 'is-accepted': isAccepted }"></div>
    <template v-if="isAccepted">
      <el-badge value="提问者采纳・优质答案" class="accepted-badge" type="warning" />
    </template>

    <div class="answer-header">
      <div class="user-info" @click="goToUser(answer.userId)">
        <el-avatar :size="36" :src="resolveAvatarUrl(answer.userAvatar)">
          {{ answer.userNickname?.charAt(0) || 'U' }}
        </el-avatar>
        <span class="nickname">{{ answer.userNickname }}</span>
      </div>
      <span class="time">{{ formatTime(answer.createTime) }}</span>
    </div>

    <div class="answer-content" ref="contentRef" v-html="resolveContentImages(answer.content)" />

    <!-- 回答图片 -->
    <div v-if="answer.imageUrls?.length" class="answer-images">
      <div
        v-for="(img, idx) in answer.imageUrls"
        :key="idx"
        class="answer-image-item"
        @click="openAnswerImagePreview(answer.imageUrls, idx)"
      >
        <img :src="resolveImageUrl(img)" alt="图片" />
      </div>
    </div>

    <div class="answer-actions">
      <el-button
        :type="answer.isLiked ? 'primary' : 'default'"
        size="small"
        @click="handleLike"
        :loading="likeLoading"
      >
        <el-icon><Pointer /></el-icon>
        {{ answer.likeCount || 0 }}
      </el-button>

      <el-button size="small" @click="showComments = !showComments">
        <el-icon><ChatDotRound /></el-icon>
        {{ answer.commentCount || 0 }}
      </el-button>

      <el-button
        v-if="isOwner"
        :type="isAccepted ? 'warning' : 'success'"
        size="small"
        :disabled="!isAccepted && hasOtherAdopted"
        @click="emit('accept')"
      >
        {{ isAccepted ? '取消采纳' : '采纳' }}
      </el-button>

      <el-button
        type="danger"
        size="small"
        plain
        @click="emit('report')"
      >
        <el-icon><WarningFilled /></el-icon>
        举报
      </el-button>
    </div>

    <!-- 评论区 -->
    <div v-if="showComments" class="comment-section">
      <div class="comment-input">
        <el-input
          v-model="commentText"
          placeholder="写下你的评论..."
          maxlength="500"
          @keyup.enter="submitComment(0, 0, '')"
        >
          <template #append>
            <el-button :loading="commentLoading" @click="submitComment(0, 0, '')">
              发送
            </el-button>
          </template>
        </el-input>
      </div>

      <div v-if="answer.comments?.length" class="comment-list">
        <CommentItem
          v-for="c in answer.comments"
          :key="c.id"
          :comment="c"
          :answer-id="answer.id"
          @reply="handleReply"
          @deleted="fetchComments"
        />
      </div>
      <el-empty v-else description="暂无评论" :image-size="60" />
    </div>

    <ImagePreview
      v-model:visible="previewVisible"
      :images="previewImages"
      :initial-index="previewIndex"
    />
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, WarningFilled, Pointer } from '@element-plus/icons-vue'
import ImagePreview from '@/components/ImagePreview.vue'
import CommentItem from '@/components/CommentItem.vue'
import { useUserStore } from '@/stores/user'
import { qaApi } from '@/api/qa'
import type { Answer } from '@/types'

const props = defineProps<{
  answer: Answer
  questionUserId: number
  isAccepted: boolean
  hasOtherAdopted: boolean
}>()

const emit = defineEmits<{
  (e: 'accept'): void
  (e: 'report'): void
  (e: 'refresh'): void
}>()

const userStore = useUserStore()
const router = useRouter()
const likeLoading = ref(false)
const contentRef = ref<HTMLElement>()

// 图片预览
const previewVisible = ref(false)
const previewImages = ref<string[]>([])
const previewIndex = ref(0)

// 评论功能
const showComments = ref(false)
const commentText = ref('')
const commentLoading = ref(false)
const replyTarget = ref<{ parentId: number; replyToId: number } | null>(null)

function extractImages(html: string): string[] {
  if (!html) return []
  const imgs: string[] = []
  const regex = /<img[^>]+src=["']([^"']+)["']/gi
  let match
  while ((match = regex.exec(html)) !== null) {
    imgs.push(match[1])
  }
  return imgs
}

function setupImageClick() {
  const el = contentRef.value
  if (!el) return
  const images = extractImages(props.answer.content)
  if (images.length === 0) return
  const imgs = el.querySelectorAll('img')
  imgs.forEach((img, i) => {
    img.style.cursor = 'pointer'
    img.addEventListener('click', () => {
      previewImages.value = images
      previewIndex.value = i
      previewVisible.value = true
    })
  })
}

function resolveImageUrl(src: string): string {
  if (!src) return ''
  if (src.startsWith('http://') || src.startsWith('https://') || src.startsWith('data:')) return src
  return 'http://localhost:8080' + (src.startsWith('/') ? '' : '/') + src
}

function openAnswerImagePreview(images: string[], index: number) {
  previewImages.value = images.map(resolveImageUrl)
  previewIndex.value = index
  previewVisible.value = true
}

watch(() => props.answer.content, async () => {
  await nextTick()
  setupImageClick()
})

watch(contentRef, async () => {
  await nextTick()
  setupImageClick()
})

const isOwner = userStore.userInfo?.id === props.questionUserId

function goToUser(userId: number) {
  router.push(`/student/user/${userId}`)
}

function resolveAvatarUrl(avatar?: string): string {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) return avatar
  return 'http://localhost:8080' + (avatar.startsWith('/') ? '' : '/') + avatar
}

function resolveContentImages(html: string): string {
  if (!html) return ''
  return html.replace(/<img[^>]+src=["']([^"']+)["']/gi, (match, src) => {
    if (src.startsWith('http://') || src.startsWith('https://') || src.startsWith('data:')) return match
    return match.replace(src, 'http://localhost:8080' + (src.startsWith('/') ? '' : '/') + src)
  })
}

async function handleLike() {
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    const res: any = await qaApi.likeAnswer(props.answer.id)
    props.answer.isLiked = res
    // 重新拉取评论以获取正确计数，emit 事件让父组件刷新
    emit('refresh')
  } catch {
    // ignore
  } finally {
    likeLoading.value = false
  }
}

async function submitComment(parentId: number, replyToId: number, _replyToNickname: string) {
  const text = commentText.value.trim()
  if (!text) return
  commentLoading.value = true
  try {
    // 如果设置了回复目标，使用回复目标
    const target = replyTarget.value
    const finalParentId = target ? target.parentId : 0
    const finalReplyToId = target ? target.replyToId : 0

    await qaApi.addComment({
      answerId: props.answer.id,
      content: text,
      parentId: finalParentId || undefined,
      replyToId: finalReplyToId || undefined,
    })
    commentText.value = ''
    replyTarget.value = null
    // 增加评论计数
    props.answer.commentCount++
    // 刷新评论列表
    await fetchComments()
  } catch {
    // ignore
  } finally {
    commentLoading.value = false
  }
}

async function fetchComments() {
  try {
    const res: any = await qaApi.getQuestionDetail(props.answer.questionId)
    const updatedAnswer = res?.answers?.find((a: Answer) => a.id === props.answer.id)
    if (updatedAnswer) {
      props.answer.comments = updatedAnswer.comments
    }
  } catch {
    // ignore
  }
}

function handleReply(parentId: number, replyToId: number, replyToNickname: string) {
  showComments.value = true
  commentText.value = ''
  replyTarget.value = { parentId, replyToId }
  // 聚焦输入框
  setTimeout(() => {
    const input = document.querySelector('.comment-section .el-input__inner') as HTMLInputElement
    if (input) {
      input.focus()
      input.value = `@${replyToNickname} `
      commentText.value = `@${replyToNickname} `
    }
  }, 100)
  // 刷新评论列表
  fetchComments()
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
.answer-card {
  margin-bottom: 12px;
  position: relative;
  overflow: visible;
}

.answer-decor {
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 4px;
  border-radius: 2px;
  background: linear-gradient(180deg, #4f8ef7, #8b5cf6);
  z-index: 1;
}

.answer-decor.is-accepted {
  background: linear-gradient(180deg, #10b981, #059669);
}

.answer-card.is-accepted {
  overflow: visible;
}

.accepted-badge {
  position: absolute;
  top: 0;
  right: 16px;
  padding: 4px 12px;
  height: auto;
  line-height: 1.2;
  z-index: 1;
}

.answer-header {
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
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #c0c4cc;
}

.answer-content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  margin-bottom: 12px;
}

.answer-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.answer-actions {
  display: flex;
  gap: 8px;
}

.comment-section {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
}

.comment-input {
  margin-bottom: 16px;
}

.comment-list {
  margin-top: 8px;
}

.answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.answer-image-item {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #ebeef5;
}

.answer-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>