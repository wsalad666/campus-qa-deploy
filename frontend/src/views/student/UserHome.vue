<script setup lang="ts">
import { formatDateTime } from '@/utils/time'
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import Pagination from '@/components/Pagination.vue'
import QuestionCard from '@/components/QuestionCard.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { qaApi } from '@/api/qa'
import { resourceApi } from '@/api/resource'
import { userApi } from '@/api/user'
import { usePagination } from '@/composables/usePagination'
import type { UserProfile, Question, Resource } from '@/types'
import { ElMessage } from 'element-plus'
import { Stamp } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userId = Number(route.params.id)

const isAdminViewer = localStorage.getItem('userRole') === 'admin'
const currentAdminUserId = isAdminViewer ? Number(localStorage.getItem('adminUserId')) : 0

const isOwnProfile = computed(() => {
  return isAdminViewer && currentAdminUserId === userId
})

const profile = ref<UserProfile | null>(null)
const activeTab = ref('questions')
const followLoading = ref(false)
const loading = ref(false)

const {
  pageNum: qPageNum,
  pageSize: qPageSize,
  total: qTotal,
  handlePageChange: qHandlePageChange,
  handleSizeChange: qHandleSizeChange,
  reset: qReset,
} = usePagination(10)

const {
  pageNum: rPageNum,
  pageSize: rPageSize,
  total: rTotal,
  handlePageChange: rHandlePageChange,
  handleSizeChange: rHandleSizeChange,
  reset: rReset,
} = usePagination(10)

const questions = ref<Question[]>([])
const resources = ref<Resource[]>([])

// Resource detail dialog
const detailDialogVisible = ref(false)
const detailResource = ref<Resource | null>(null)

function onResourceClick(resource: Resource) {
  detailResource.value = resource
  detailDialogVisible.value = true
}

function formatFileSize(bytes: number): string {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

function formatTime(time: string) { return formatDateTime(time) }

function getResourceTypeLabel(type: number | null): string {
  const labels = ['试卷', '习题', '笔记', '课件']
  if (type === null || type === undefined) return '未知'
  return labels[type] || '未知'
}

async function fetchProfile() {
  try {
    const res: any = await userApi.getProfile(userId)
    profile.value = res
  } catch {
    // error handled by interceptor
  }
}

async function fetchQuestions() {
  loading.value = true
  try {
    const res: any = await qaApi.getQuestionList({
      pageNum: qPageNum.value,
      pageSize: qPageSize.value,
    })
    const records = (res.records || []) as Question[]
    questions.value = records.filter((q) => q.userId === userId)
    qTotal.value = questions.value.length
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

async function fetchResources() {
  loading.value = true
  try {
    const res: any = await resourceApi.getMyResources({
      pageNum: rPageNum.value,
      pageSize: rPageSize.value,
    })
    resources.value = res.records || []
    rTotal.value = res.total
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

async function toggleFollow() {
  if (!profile.value || followLoading.value) return
  followLoading.value = true
  try {
    const res: any = await userApi.toggleFollow({ followedId: userId })
    profile.value.isFollowed = res
    ElMessage.success(profile.value.isFollowed ? '已关注' : '已取消关注')
  } catch {
    // error handled by interceptor
  } finally {
    followLoading.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'questions') {
    qReset()
    fetchQuestions()
  } else {
    rReset()
    fetchResources()
  }
})

watch([qPageNum, qPageSize], () => {
  if (activeTab.value === 'questions') fetchQuestions()
})

watch([rPageNum, rPageSize], () => {
  if (activeTab.value === 'resources') fetchResources()
})

onMounted(() => {
  fetchProfile()
  fetchQuestions()
})
</script>

<template>
  <div class="userhome-container">
    <AppHeader />
    <main class="userhome-main">
      <div v-if="profile" class="user-card">
        <div class="user-banner">
          <div class="banner-pattern"></div>
        </div>
        <div class="user-card-top">
          <div class="avatar-border">
            <UserAvatar :src="profile.user.avatar" :size="80" />
          </div>
          <div class="user-info">
            <h3 class="user-name">
              {{ profile.user.nickname || profile.user.username }}
              <el-tag v-if="profile.user.role === 1" type="warning" size="small" effect="dark" class="admin-badge">
                <el-icon style="margin-right: 2px"><Stamp /></el-icon>
                管理员
              </el-tag>
            </h3>
            <p class="user-signature">{{ profile.user.signature || '这个人很懒，什么都没写~' }}</p>
          </div>
          <div class="user-actions">
            <el-button
              :type="profile.isFollowed ? 'default' : 'primary'"
              :loading="followLoading"
              @click="toggleFollow"
            >
              {{ profile.isFollowed ? '✓ 已关注' : '+ 关注' }}
            </el-button>
            <el-button
              v-if="isOwnProfile"
              type="primary"
              plain
              @click="router.push('/student/profile')"
            >
              ✏️ 编辑资料
            </el-button>
          </div>
        </div>
        <div class="user-stats">
          <div class="stat-item">
            <span class="stat-icon">💬</span>
            <span class="stat-num">{{ profile.questionCount }}</span>
            <span class="stat-label">提问</span>
          </div>
          <div class="stat-item">
            <span class="stat-icon">📁</span>
            <span class="stat-num">{{ profile.resourceCount }}</span>
            <span class="stat-label">资源</span>
          </div>
          <div class="stat-item">
            <span class="stat-icon">❤️</span>
            <span class="stat-num">{{ profile.fansCount }}</span>
            <span class="stat-label">粉丝</span>
          </div>
          <div class="stat-item">
            <span class="stat-icon">👀</span>
            <span class="stat-num">{{ profile.followCount }}</span>
            <span class="stat-label">关注</span>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="userhome-tabs">
        <el-tab-pane label="💬 TA的提问" name="questions">
          <div v-loading="loading">
            <QuestionCard
              v-for="q in questions"
              :key="q.id"
              :question="q"
            />
            <el-empty v-if="!loading && questions.length === 0" description="暂无提问" />
            <div v-if="qTotal > qPageSize" class="tab-pagination">
              <Pagination
                :total="qTotal"
                :page-num="qPageNum"
                :page-size="qPageSize"
                @update:page-num="qHandlePageChange"
                @update:page-size="qHandleSizeChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="📁 TA的资源" name="resources">
          <div v-loading="loading">
            <el-row :gutter="16">
              <el-col
                v-for="r in resources"
                :key="r.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <ResourceCard :resource="r" @click="onResourceClick" />
              </el-col>
            </el-row>
            <el-empty v-if="!loading && resources.length === 0" description="暂无资源" />
            <div v-if="rTotal > rPageSize" class="tab-pagination">
              <Pagination
                :total="rTotal"
                :page-num="rPageNum"
                :page-size="rPageSize"
                @update:page-num="rHandlePageChange"
                @update:page-size="rHandleSizeChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </main>

    <!-- 资料详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="资料详情"
      width="520px"
      destroy-on-close
    >
      <template v-if="detailResource">
        <div class="detail-body">
          <div class="detail-row">
            <span class="detail-label">文件名称</span>
            <span class="detail-value">{{ detailResource.title }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">所属课程</span>
            <span class="detail-value">{{ detailResource.courseName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">资料类型</span>
            <span class="detail-value">
              <el-tag size="small" type="warning">{{ getResourceTypeLabel(detailResource.resourceType) }}</el-tag>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">文件格式</span>
            <span class="detail-value">{{ detailResource.fileType?.toUpperCase() }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">文件大小</span>
            <span class="detail-value">{{ formatFileSize(detailResource.fileSize) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">上传者</span>
            <span class="detail-value">{{ detailResource.userNickname }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">下载次数</span>
            <span class="detail-value">{{ detailResource.downloadCount }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">上传时间</span>
            <span class="detail-value">{{ formatTime(detailResource.createTime) }}</span>
          </div>
          <div class="detail-row" v-if="detailResource.description">
            <span class="detail-label">描述</span>
            <span class="detail-value desc">{{ detailResource.description }}</span>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.userhome-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.userhome-main {
  max-width: 860px;
  margin: 0 auto;
  padding: 88px 24px 40px;
}

.user-card {
  background: #fff;
  border-radius: 16px;
  padding: 0 32px 28px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.user-banner {
  position: relative;
  height: 120px;
  background: linear-gradient(135deg, #4f8ef7 0%, #7c3aed 50%, #a855f7 100%);
  margin: 0 -32px;
}

.banner-pattern {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.12) 0%, transparent 50%),
              radial-gradient(circle at 80% 30%, rgba(255,255,255,0.08) 0%, transparent 40%);
}

.user-card-top {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: -40px;
  position: relative;
  z-index: 1;
}

.avatar-border {
  flex-shrink: 0;
  border: 4px solid #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-info {
  flex: 1;
  padding-top: 44px;
}

.user-name {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-badge {
  font-weight: 500;
}

.user-signature {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.user-actions {
  padding-top: 44px;
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 24px;
  padding: 20px 0 0;
  border-top: 1px solid #f1f5f9;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 20px;
  margin-bottom: 2px;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #4f8ef7, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
}

.userhome-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 0 20px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.tab-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.detail-body {
  padding: 0 8px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  width: 80px;
  flex-shrink: 0;
  color: #909399;
  font-size: 14px;
}

.detail-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
  word-break: break-all;
}

.detail-value.desc {
  color: #606266;
  line-height: 1.6;
}
</style>