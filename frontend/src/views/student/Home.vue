<script setup lang="ts">
import { formatDateTime } from '@/utils/time'
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import QuestionCard from '@/components/QuestionCard.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import PreviewDialog from '@/components/PreviewDialog.vue'
import CollectFolderSelect from '@/components/CollectFolderSelect.vue'
import { qaApi } from '@/api/qa'
import { resourceApi } from '@/api/resource'
import { userApi } from '@/api/user'
import type { Question, Resource } from '@/types'

const router = useRouter()

const hotQuestions = ref<Question[]>([])
const latestResources = ref<Resource[]>([])
const currentCourseIds = ref<number[]>([])

// 资料详情弹窗
const detailDialogVisible = ref(false)
const detailResource = ref<Resource | null>(null)

// 预览弹窗
const previewDialogVisible = ref(false)
const previewResource = ref<Resource | null>(null)

// 收藏弹窗
const collectDialogVisible = ref(false)
const collectTargetType = ref(2)
const collectTargetId = ref(0)

function onResourceClick(resource: Resource) {
  detailResource.value = resource
  detailDialogVisible.value = true
}

function onResourcePreview(resource: Resource) {
  previewResource.value = resource
  previewDialogVisible.value = true
}

async function onResourceCollect(resource: Resource) {
  const res = await userApi.isCollectedAny(2, resource.id).catch(() => null)
  if (res) {
    try {
      await ElMessageBox.confirm('确定取消收藏该资源？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userApi.removeCollectByTarget(2, resource.id)
      ElMessage.success('已取消收藏')
      fetchLatestResources()
    } catch {
      // 用户取消
    }
    return
  }
  collectTargetType.value = 2
  collectTargetId.value = resource.id
  collectDialogVisible.value = true
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

async function fetchHotQuestions(courseIds?: number[]) {
  try {
    const res: any = await qaApi.getQuestionList({
      pageNum: 1,
      pageSize: 4,
      sort: 'hot',
      courseIds,
    })
    hotQuestions.value = res.records || []
  } catch {
    // error handled by interceptor
  }
}

async function fetchLatestResources(courseIds?: number[]) {
  try {
    const res: any = await resourceApi.getList({
      pageNum: 1,
      pageSize: 4,
      courseIds,
    })
    latestResources.value = res.records || []
  } catch {
    // error handled by interceptor
  }
}

function onCourseChange(courseIds: number[]) {
  currentCourseIds.value = courseIds
  fetchHotQuestions(courseIds)
  fetchLatestResources(courseIds)
}

function goToQaList() {
  router.push('/student/qa')
}

function goToResourceCenter() {
  router.push('/student/resource')
}

onMounted(() => {
  fetchHotQuestions()
  fetchLatestResources()
})
</script>

<template>
  <div class="home-container">
    <AppHeader />
    <div class="home-body">
      <AppSidebar @course-change="onCourseChange" />
      <main class="home-main">
        <section class="welcome-banner">
          <div class="banner-content">
            <h2 class="banner-title">欢迎来到校园互助答疑平台</h2>
            <p class="banner-desc">在这里提问、分享、互助成长</p>
            <div class="banner-actions">
              <el-button type="primary" size="large" @click="goToQaList">去提问</el-button>
              <el-button size="large" plain @click="goToResourceCenter">找资料</el-button>
            </div>
          </div>
          <div class="banner-decor">💡🎓📖</div>
        </section>
        <section class="home-section">
          <div class="section-header">
            <h3>🔥 热门提问</h3>
            <el-button type="primary" size="small" @click="goToQaList">查看更多 →</el-button>
          </div>
          <el-row :gutter="16">
            <el-col
              v-for="q in hotQuestions"
              :key="q.id"
              :xs="24"
              :sm="12"
              :md="12"
              :lg="12"
            >
              <QuestionCard :question="q" />
            </el-col>
          </el-row>
          <el-empty v-if="hotQuestions.length === 0" description="暂无热门提问" />
        </section>

        <section class="home-section">
          <div class="section-header">
            <h3>📁 最新资源</h3>
            <el-button type="primary" size="small" @click="goToResourceCenter">查看更多 →</el-button>
          </div>
          <el-row :gutter="16">
            <el-col
              v-for="r in latestResources"
              :key="r.id"
              :xs="24"
              :sm="12"
              :md="12"
              :lg="12"
            >
              <ResourceCard :resource="r" @click="onResourceClick" @preview="onResourcePreview" @collect="onResourceCollect" />
            </el-col>
          </el-row>
          <el-empty v-if="latestResources.length === 0" description="暂无最新资源" />
        </section>
      </main>
    </div>

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

    <!-- 文件预览弹窗 -->
    <PreviewDialog
      v-model="previewDialogVisible"
      :title="previewResource?.title"
      :file-url="previewResource?.fileUrl"
      :file-type="previewResource?.fileType"
    />

    <!-- 收藏文件夹选择弹窗 -->
    <CollectFolderSelect
      v-model="collectDialogVisible"
      :target-type="collectTargetType"
      :target-id="collectTargetId"
    />
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.home-body {
  display: flex;
  padding-top: 64px;
}

.home-main {
  flex: 1;
  margin-left: 220px;
  padding: 24px;
}

.welcome-banner {
  background: linear-gradient(135deg, #4f8ef7 0%, #8b5cf6 50%, #a855f7 100%);
  border-radius: 16px;
  padding: 36px 40px;
  margin-bottom: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.banner-content {
  position: relative;
  z-index: 1;
}

.banner-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
}

.banner-desc {
  font-size: 14px;
  opacity: 0.85;
  margin: 0 0 20px;
}

.banner-actions .el-button--default {
  background: rgba(255,255,255,0.15) !important;
  border-color: rgba(255,255,255,0.4) !important;
  color: #fff !important;
}

.banner-decor {
  font-size: 40px;
  letter-spacing: 12px;
  opacity: 0.6;
}

.home-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
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