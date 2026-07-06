<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { resourceApi } from '@/api/resource'
import { ElMessage } from 'element-plus'
import type { Resource } from '@/types'

const route = useRoute()
const router = useRouter()
const resource = ref<Resource | null>(null)
const loading = ref(false)
const downloading = ref(false)

const fileId = Number(route.params.id)

async function fetchDetail() {
  loading.value = true
  try {
    const res: any = await resourceApi.getDetail(fileId)
    resource.value = res
  } catch {
    ElMessage.error('加载资源详情失败')
  } finally {
    loading.value = false
  }
}

async function handleDownload() {
  downloading.value = true
  try {
    const blob = await resourceApi.download(fileId)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = resource.value ? `${resource.value.title}.${resource.value.fileType}` : 'download'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    if (resource.value) resource.value.downloadCount++
    ElMessage.success('下载成功')
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '下载失败，请重试'
    ElMessage.error(msg)
  } finally {
    downloading.value = false
  }
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

function getFileIcon(type: string) {
  const map: Record<string, string> = {
    pdf: '📄', doc: '📝', docx: '📝', ppt: '📊', pptx: '📊',
    xls: '📊', xlsx: '📊', jpg: '🖼️', png: '🖼️', mp4: '🎬', zip: '📦',
  }
  return map[type?.toLowerCase()] || '📎'
}

function getFileTypeColor(type: string): string {
  const map: Record<string, string> = {
    pdf: '#e74c3c', doc: '#2980b9', docx: '#2980b9',
    ppt: '#e67e22', pptx: '#e67e22', xls: '#27ae60', xlsx: '#27ae60',
    jpg: '#8e44ad', png: '#8e44ad', mp4: '#c0392b', zip: '#7f8c8d',
  }
  return map[type?.toLowerCase()] || '#909399'
}

function getCategoryLabel(category: string): string {
  const map: Record<string, string> = {
    exam: '期末试卷', courseware: '课件', review: '复习资料', homework: '作业', other: '其他',
  }
  return map[category] || ''
}

function getCategoryColor(category: string): string {
  const map: Record<string, string> = {
    exam: '#e74c3c', courseware: '#409eff', review: '#e67e22', homework: '#27ae60', other: '#909399',
  }
  return map[category] || '#909399'
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="detail-container">
    <AppHeader />
    <main class="detail-main" v-loading="loading">
      <template v-if="resource">
        <div class="detail-card">
          <!-- 返回按钮 -->
          <div class="back-row">
            <el-button text @click="router.push('/student/resource')">
              <el-icon><ArrowLeft /></el-icon> 返回资源列表
            </el-button>
          </div>

          <!-- 文件图标 + 标题 -->
          <div class="detail-header">
            <div class="file-icon-large" :style="{ background: getFileTypeColor(resource.fileType) + '18', color: getFileTypeColor(resource.fileType) }">
              <span class="icon-text">{{ getFileIcon(resource.fileType) }}</span>
            </div>
            <div class="header-info">
              <h1 class="detail-title">{{ resource.title }}</h1>
              <div class="detail-meta">
                <el-tag v-if="getCategoryLabel(resource.category)" :color="getCategoryColor(resource.category)" effect="dark" size="small" style="border: none">
                  {{ getCategoryLabel(resource.category) }}
                </el-tag>
                <el-tag :color="getFileTypeColor(resource.fileType)" effect="dark" size="small" style="border: none">
                  {{ resource.fileType?.toUpperCase() }}
                </el-tag>
                <span class="meta-item">上传者：{{ resource.userNickname }}</span>
                <span class="meta-item">所属课程：{{ resource.courseName }}</span>
              </div>
            </div>
          </div>

          <!-- 描述 -->
          <div class="detail-section" v-if="resource.description">
            <h3>资源描述</h3>
            <p class="description-text">{{ resource.description }}</p>
          </div>

          <!-- 文件信息 -->
          <div class="detail-section">
            <h3>文件信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="板块分类">
                <el-tag v-if="getCategoryLabel(resource.category)" :color="getCategoryColor(resource.category)" effect="dark" size="small" style="border: none">
                  {{ getCategoryLabel(resource.category) }}
                </el-tag>
                <span v-else style="color: #909399">未分类</span>
              </el-descriptions-item>
              <el-descriptions-item label="文件类型">
                <el-tag :color="getFileTypeColor(resource.fileType)" effect="dark" size="small" style="border: none">
                  {{ resource.fileType?.toUpperCase() }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="文件大小">{{ formatFileSize(resource.fileSize) }}</el-descriptions-item>
              <el-descriptions-item label="下载次数">{{ resource.downloadCount }} 次</el-descriptions-item>
              <el-descriptions-item label="上传时间">{{ resource.createTime }}</el-descriptions-item>
              <el-descriptions-item label="上传者">{{ resource.userNickname }}</el-descriptions-item>
              <el-descriptions-item label="所属课程">{{ resource.courseName }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 下载按钮 -->
          <div class="download-area">
            <el-button type="primary" size="large" :loading="downloading" @click="handleDownload">
              <el-icon><Download /></el-icon>
              {{ downloading ? '下载中...' : '立即下载' }}
            </el-button>
            <span class="download-hint">{{ formatFileSize(resource.fileSize) }} · 已下载 {{ resource.downloadCount }} 次</span>
          </div>
        </div>
      </template>

      <el-empty v-if="!loading && !resource" description="资源不存在或已被下架" />
    </main>
  </div>
</template>

<style scoped>
.detail-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.detail-main {
  max-width: 800px;
  margin: 0 auto;
  padding: 84px 24px 24px;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.back-row {
  margin-bottom: 20px;
}

.detail-header {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 28px;
}

.file-icon-large {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-text {
  font-size: 36px;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.detail-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 12px;
  word-break: break-word;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 14px;
  color: #606266;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.description-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.download-area {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.download-hint {
  font-size: 13px;
  color: #909399;
}
</style>
