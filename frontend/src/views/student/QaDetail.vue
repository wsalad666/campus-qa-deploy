<script setup lang="ts">
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile, UploadRawFile } from 'element-plus'
import {
  Star,
  View,
  ChatLineSquare,
  WarningFilled,
  Plus,
  Pointer,
} from '@element-plus/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import AnswerCard from '@/components/AnswerCard.vue'
import ImagePreview from '@/components/ImagePreview.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import CollectFolderSelect from '@/components/CollectFolderSelect.vue'
import SimilarQuestions from '@/components/SimilarQuestions.vue'
import { qaApi } from '@/api/qa'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { QuestionDetail } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const questionId = Number(route.params.id)

const detail = ref<QuestionDetail | null>(null)
const loading = ref(false)
const answerContent = ref('')
const answerLoading = ref(false)
const answerUploadFiles = ref<UploadFile[]>([])
const favoriteLoading = ref(false)
const collectDialogVisible = ref(false)
const likeLoading = ref(false)
const reportLoading = ref(false)
const reportDialogVisible = ref(false)
const reportTargetType = ref<1 | 2>(1) // 1=提问, 2=回答
const reportTargetId = ref(0)
const reportTargetTitle = ref('')
const reportReason = ref('')
const reportSubmitting = ref(false)
const notFound = ref(false)
const isAdminViewer = localStorage.getItem('userRole') === 'admin'

// 提问管理相关
const questionActionLoading = ref<'delete' | 'close' | 'hide' | 'unhide' | 'reopen' | null>(null)

const isQuestionOwner = computed(() => {
  return userStore.userInfo?.id === detail.value?.userId
})

const hasOtherAnswers = computed(() => {
  if (!detail.value?.answers) return false
  return detail.value.answers.some(a => a.userId !== detail.value?.userId)
})

// 图片预览
const previewVisible = ref(false)
const previewImages = ref<string[]>([])
const previewIndex = ref(0)

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

function resolveImageUrl(src: string): string {
  if (!src) return ''
  if (src.startsWith('http://') || src.startsWith('https://') || src.startsWith('data:')) return src
  return '' + (src.startsWith('/') ? '' : '/') + src
}

function resolveContentImages(html: string): string {
  if (!html) return ''
  return html.replace(/<img[^>]+src=["']([^"']+)["']/gi, (match, src) => {
    return match.replace(src, resolveImageUrl(src))
  })
}

function openImagePreview(images: string[], index: number) {
  previewImages.value = images.map(resolveImageUrl)
  previewIndex.value = index
  previewVisible.value = true
}

function goToUser(userId: number) {
  router.push(`/student/user/${userId}`)
}

function setupImageClick(container: HTMLElement | null, images: string[]) {
  if (!container) return
  const imgs = container.querySelectorAll('img')
  imgs.forEach((img, i) => {
    img.style.cursor = 'pointer'
    img.addEventListener('click', () => openImagePreview(images, i))
  })
}

watch(detail, async () => {
  await nextTick()
  if (detail.value) {
    const questionEl = document.querySelector('.question-content') as HTMLElement
    if (questionEl) {
      setupImageClick(questionEl, extractImages(detail.value.content))
    }
  }
})

async function fetchDetail() {
  loading.value = true
  notFound.value = false
  try {
    const res: any = await qaApi.getQuestionDetail(questionId)
    detail.value = res
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!detail.value || favoriteLoading.value) return
  if (detail.value.isFavorited) {
    // 已收藏：确认取消收藏
    try {
      await ElMessageBox.confirm('确定取消收藏？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
      favoriteLoading.value = true
      await userApi.toggleFavorite({ targetId: questionId, type: 1 })
      detail.value.isFavorited = false
      detail.value.favoriteCount = Math.max(0, (detail.value.favoriteCount || 0) - 1)
      ElMessage.success('已取消收藏')
    } catch { /* user cancelled or error */ }
    finally { favoriteLoading.value = false }
  } else {
    // 未收藏：打开收藏文件夹选择
    collectDialogVisible.value = true
  }
}

function onCollectDone() {
  // Refresh detail to update favorite status
  fetchDetail()
}

async function toggleLike() {
  if (!detail.value || likeLoading.value) return
  likeLoading.value = true
  try {
    const res: any = await qaApi.likeQuestion(questionId)
    detail.value.isLiked = res
    ElMessage.success(detail.value.isLiked ? '点赞成功' : '已取消点赞')
    await fetchDetail()
  } catch {
    // error handled by interceptor
  } finally {
    likeLoading.value = false
  }
}

function openReportDialog(type: 1 | 2, targetId: number, title: string) {
  reportTargetType.value = type
  reportTargetId.value = targetId
  reportTargetTitle.value = title
  reportReason.value = ''
  reportDialogVisible.value = true
}

async function submitReport() {
  if (!reportReason.value.trim()) {
    ElMessage.warning('请填写举报原因')
    return
  }
  reportSubmitting.value = true
  try {
    await qaApi.submitReport({
      targetType: reportTargetType.value,
      targetId: reportTargetId.value,
      reason: reportReason.value.trim(),
    })
    ElMessage.success('举报成功，管理员会尽快处理')
    reportDialogVisible.value = false
  } catch {
    // error handled by interceptor
  } finally {
    reportSubmitting.value = false
  }
}

async function handleDeleteQuestion() {
  if (!detail.value) return
  try {
    await ElMessageBox.confirm(
      '确定要彻底删除该提问吗？此操作不可恢复。',
      '确认删除',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  questionActionLoading.value = 'delete'
  try {
    await qaApi.deleteQuestion(questionId)
    ElMessage.success('提问已删除')
    router.push('/student/qa')
  } catch {
    // error handled by interceptor
  } finally {
    questionActionLoading.value = null
  }
}

async function handleCloseQuestion() {
  if (!detail.value) return
  try {
    await ElMessageBox.confirm(
      '关闭提问后，其他用户将无法再添加新的回答，已有回答会保留。确定要关闭吗？',
      '确认关闭',
      { confirmButtonText: '确定关闭', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  questionActionLoading.value = 'close'
  try {
    await qaApi.closeQuestion(questionId)
    detail.value.status = 2
    ElMessage.success('提问已关闭')
  } catch {
    // error handled by interceptor
  } finally {
    questionActionLoading.value = null
  }
}

async function handleHideQuestion() {
  if (!detail.value) return
  try {
    await ElMessageBox.confirm(
      '设为私密后，该提问仅你自己可见，已有回答会保留。确定要设为私密吗？',
      '确认设为私密',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  questionActionLoading.value = 'hide'
  try {
    await qaApi.hideQuestion(questionId)
    detail.value.status = 3
    ElMessage.success('已设为私密')
  } catch {
    // error handled by interceptor
  } finally {
    questionActionLoading.value = null
  }
}

async function handleReopenQuestion() {
  if (!detail.value) return
  try {
    await ElMessageBox.confirm(
      '重新打开后，其他用户将可以继续回答。确定要重新打开吗？',
      '确认重新打开',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }
  questionActionLoading.value = 'reopen'
  try {
    await qaApi.reopenQuestion(questionId)
    detail.value.status = 0
    ElMessage.success('已重新打开')
  } catch {
    // error handled by interceptor
  } finally {
    questionActionLoading.value = null
  }
}

async function handleUnhideQuestion() {
  if (!detail.value) return
  questionActionLoading.value = 'unhide'
  try {
    await qaApi.unhideQuestion(questionId)
    detail.value.status = 0
    ElMessage.success('已取消私密')
  } catch {
    // error handled by interceptor
  } finally {
    questionActionLoading.value = null
  }
}


function beforeUpload(rawFile: UploadRawFile) {
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  if (!isImage) {
    ElMessage.error('仅支持 JPG、PNG 格式')
    return false
  }
  const isLt5M = rawFile.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function submitAnswer() {
  if (!answerContent.value.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }
  answerLoading.value = true
  try {
    const imageUrls: string[] = []
    for (const f of answerUploadFiles.value) {
      if (f.raw) {
        const path = await qaApi.uploadImage(f.raw)
        imageUrls.push(path)
      }
    }
    await qaApi.answerQuestion({
      questionId,
      content: answerContent.value,
      imageUrls: imageUrls.length > 0 ? imageUrls : undefined,
    })
    ElMessage.success('回答成功')
    answerContent.value = ''
    answerUploadFiles.value = []
    fetchDetail()
  } catch {
    // error handled by interceptor
  } finally {
    answerLoading.value = false
  }
}

async function handleAccept(answerId: number) {
  if (!detail.value) return
  const target = detail.value.answers.find((a) => a.id === answerId)
  if (!target) return
  const wasAccepted = target.isAccepted === 1
  try {
    await qaApi.acceptAnswer(answerId)
    if (wasAccepted) {
      // 取消采纳
      ElMessage.success('已取消采纳')
      detail.value.adoptAnswerId = null
      detail.value.answers.forEach((a) => {
        a.isAccepted = 0
      })
    } else {
      // 采纳当前回答
      ElMessage.success('已采纳回答')
      detail.value.adoptAnswerId = answerId
      detail.value.answers.forEach((a) => {
        a.isAccepted = a.id === answerId ? 1 : 0
      })
    }
  } catch {
    // error handled by interceptor
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="qadetail-container">
    <AppHeader />
    <main class="qadetail-main" v-loading="loading">
      <template v-if="notFound">
        <el-result icon="warning" title="问题不存在或已下架" sub-title="该问题可能已被管理员下架或删除">
          <template #extra>
            <el-button type="primary" @click="router.push(isAdminViewer ? '/admin/reports' : '/student/qa')">
              {{ isAdminViewer ? '返回' : '返回问答列表' }}
            </el-button>
          </template>
        </el-result>
      </template>
      <template v-else-if="detail">
        <div class="qa-detail-layout">
          <div class="qa-detail-content">
            <div class="question-detail">
              <div class="question-header">
                <h2 class="question-title">{{ detail.title }}</h2>
                <div class="question-meta">
                  <el-tag size="small" type="info">{{ detail.courseName }}</el-tag>
                  <span class="meta-user" @click="goToUser(detail.userId)">
                    <el-avatar :size="20" :src="resolveImageUrl(detail.userAvatar || '')" class="user-avatar" />
                    {{ detail.userNickname }}
                  </span>
                  <span class="meta-time">{{ detail.createTime }}</span>
                  <span class="meta-stats">
                    <el-icon><View /></el-icon> {{ detail.viewCount }}
                  </span>
                  <span class="meta-stats">
                    <el-icon><ChatLineSquare /></el-icon> {{ detail.answerCount }}
                  </span>
                  <span class="meta-stats likes">
                    <el-icon><Pointer /></el-icon> {{ detail.likeCount || 0 }}
                  </span>
                  <span class="meta-stats favorites">
                    <el-icon><Star /></el-icon> {{ detail.favoriteCount || 0 }}
                  </span>
                  <el-button
                    :type="detail.isLiked ? 'primary' : 'default'"
                    size="small"
                    :loading="likeLoading"
                    @click="toggleLike"
                  >
                    <el-icon><Pointer /></el-icon>
                    {{ detail.isLiked ? '已点赞' : '点赞' }}
                  </el-button>
                  <el-button
                    :type="detail.isFavorited ? 'warning' : 'default'"
                    size="small"
                    :loading="favoriteLoading"
                    @click="toggleFavorite"
                  >
                    <el-icon><Star /></el-icon>
                    {{ detail.isFavorited ? '已收藏' : '收藏' }}
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    plain
                    @click="openReportDialog(1, detail.id, detail.title)"
                  >
                    <el-icon><WarningFilled /></el-icon>
                    举报
                  </el-button>

                  <!-- 提问者管理按钮 -->
                  <template v-if="isQuestionOwner">
                    <!-- 已关闭 -->
                    <template v-if="detail.status === 2">
                      <el-tag type="info" size="small">已关闭</el-tag>
                      <el-button
                        type="success"
                        size="small"
                        :loading="questionActionLoading === 'reopen'"
                        @click="handleReopenQuestion"
                      >
                        重新打开
                      </el-button>
                      <el-button
                        type="info"
                        size="small"
                        :loading="questionActionLoading === 'hide'"
                        @click="handleHideQuestion"
                      >
                        设为私密
                      </el-button>
                      <el-button
                        type="danger"
                        size="small"
                        :loading="questionActionLoading === 'delete'"
                        @click="handleDeleteQuestion"
                      >
                        删除提问
                      </el-button>
                    </template>
                    <!-- 已设私密 -->
                    <template v-else-if="detail.status === 3">
                      <el-tag type="warning" size="small">已设为私密</el-tag>
                      <el-button
                        type="success"
                        size="small"
                        :loading="questionActionLoading === 'unhide'"
                        @click="handleUnhideQuestion"
                      >
                        取消私密
                      </el-button>
                      <el-button
                        type="warning"
                        size="small"
                        :loading="questionActionLoading === 'close'"
                        @click="handleCloseQuestion"
                      >
                        关闭提问
                      </el-button>
                    </template>
                    <!-- 正常/已解决 且 无其他人回答 -->
                    <template v-else-if="!hasOtherAnswers">
                      <el-button
                        type="danger"
                        size="small"
                        :loading="questionActionLoading === 'delete'"
                        @click="handleDeleteQuestion"
                      >
                        删除提问
                      </el-button>
                    </template>
                    <!-- 正常/已解决 且 有其他人回答 -->
                    <template v-else>
                      <el-button
                        type="warning"
                        size="small"
                        :loading="questionActionLoading === 'close'"
                        @click="handleCloseQuestion"
                      >
                        关闭提问
                      </el-button>
                      <el-button
                        type="info"
                        size="small"
                        :loading="questionActionLoading === 'hide'"
                        @click="handleHideQuestion"
                      >
                        设为私密
                      </el-button>
                    </template>
                  </template>
                </div>
              </div>
              <div class="question-content" v-html="resolveContentImages(detail.content)" />

              <!-- 问题图片 -->
              <div v-if="detail.imageUrls?.length" class="question-images">
                <div
                  v-for="(img, idx) in detail.imageUrls"
                  :key="idx"
                  class="question-image-item"
                  @click="openImagePreview(detail.imageUrls, idx)"
                >
                  <img :src="resolveImageUrl(img)" alt="图片" />
                </div>
              </div>
            </div>

            <el-divider />

            <div class="answer-section">
              <h3 class="section-title">
                回答（{{ detail.answers?.length || 0 }}）
              </h3>
              <div v-if="detail.answers?.length">
                <AnswerCard
                  v-for="a in detail.answers"
                  :key="a.id"
                  :answer="a"
                  :question-user-id="detail.userId"
                  :is-accepted="a.isAccepted === 1"
                  :has-other-adopted="detail.adoptAnswerId != null && detail.adoptAnswerId !== a.id"
                  @accept="handleAccept(a.id)"
                  @report="openReportDialog(2, a.id, a.content.substring(0, 30) + '...')"
                  @refresh="fetchDetail"
                />
              </div>
              <el-empty v-else description="暂无回答，快来抢沙发吧" />

              <div class="answer-input">
                <h4>发表回答</h4>
                <RichTextEditor v-model="answerContent" />
                <div class="answer-upload">
                  <el-upload
                    v-model:file-list="answerUploadFiles"
                    :auto-upload="false"
                    :before-upload="beforeUpload"
                    list-type="picture-card"
                    accept="image/jpeg,image/png"
                    multiple
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <p class="upload-hint">支持 JPG、PNG 格式，每张最大 5MB，提交时自动上传</p>
                </div>
                <el-button
                  type="primary"
                  :loading="answerLoading"
                  class="submit-answer-btn"
                  @click="submitAnswer"
                >
                  提交回答
                </el-button>
              </div>
            </div>
          </div>

          <!-- 右侧相似问题推荐 -->
          <aside class="qa-detail-sidebar">
            <SimilarQuestions
              :course-id="detail.courseId"
              :exclude-id="questionId"
              :title="detail.title"
              :content="detail.content"
              mode="sidebar"
            />
          </aside>
        </div>
      </template>
    </main>

    <ImagePreview
      v-model:visible="previewVisible"
      :images="previewImages"
      :initial-index="previewIndex"
    />

    <!-- 收藏文件夹选择弹窗 -->
    <CollectFolderSelect
      v-model="collectDialogVisible"
      :target-type="1"
      :target-id="questionId"
      @done="onCollectDone"
    />

    <!-- 举报弹窗 -->
    <el-dialog
      v-model="reportDialogVisible"
      title="举报"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form label-width="80px">
        <el-form-item label="举报类型">
          <el-tag :type="reportTargetType === 1 ? 'warning' : 'info'">
            {{ reportTargetType === 1 ? '提问' : '回答' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="举报内容">
          <span class="report-target-title">{{ reportTargetTitle }}</span>
        </el-form-item>
        <el-form-item label="举报原因" required>
          <el-input
            v-model="reportReason"
            type="textarea"
            :rows="4"
            maxlength="512"
            show-word-limit
            placeholder="请详细描述举报原因（如：广告、色情、辱骂、侵权等）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="reportSubmitting" @click="submitReport">
          确认举报
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.qadetail-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.qadetail-main {
  max-width: 1100px;
  margin: 0 auto;
  padding: 80px 24px 40px;
}

.qa-detail-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.qa-detail-content {
  flex: 1;
  min-width: 0;
}

.qa-detail-sidebar {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.question-detail {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.question-header {
  margin-bottom: 16px;
}

.question-title {
  font-size: 22px;
  color: #303133;
  margin: 0 0 12px;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-time {
  font-size: 13px;
  color: #909399;
}

.meta-user {
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-avatar {
  vertical-align: middle;
}

.meta-user:hover {
  text-decoration: underline;
}

.meta-stats {
  display: flex;
  align-items: center;
  gap: 4px;
}

.question-content {
  padding: 16px 0;
  line-height: 1.8;
  font-size: 15px;
  color: #303133;
}

.question-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 8px 0;
  cursor: pointer;
}

.answer-section {
  margin-top: 24px;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 16px;
}

.answer-input {
  margin-top: 32px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.answer-input h4 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #303133;
}

.submit-answer-btn {
  margin-top: 12px;
}

.question-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.question-image-item {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #ebeef5;
}

.question-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.answer-upload {
  margin-top: 12px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.report-target-title {
  color: #606266;
  word-break: break-all;
}
</style>
