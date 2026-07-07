<template>
  <!-- Card layout -->
  <el-card v-if="layout === 'card'" class="resource-card" shadow="hover" @click="emit('click', resource)">
    <div class="card-top-bar"></div>
    <div class="resource-header">
      <div class="file-icon" :class="fileIconClass">
        <span class="file-emoji">{{ fileEmoji }}</span>
      </div>
      <div class="resource-info">
        <h3 class="title">{{ resource.title }}</h3>
        <div class="meta">
          <span class="uploader">{{ resource.userNickname }}</span>
          <span class="size">{{ formatFileSize(resource.fileSize) }}</span>
          <span class="downloads">
            <el-icon><Download /></el-icon>
            {{ resource.downloadCount }}
          </span>
        </div>
      </div>
    </div>

    <div class="resource-footer">
      <span class="time">{{ formatTime(resource.createTime) }}</span>
      <div class="footer-actions">
        <el-button
          v-if="showDelete"
          type="danger"
          size="small"
          plain
          @click.stop="emit('delete', resource.id)"
        >
          删除
        </el-button>
        <el-button
          v-if="isPreviewable"
          type="warning"
          size="small"
          plain
          @click.stop="emit('preview', resource)"
        >
          预览
        </el-button>
        <el-button
          :type="resource.isFavorited ? 'warning' : 'success'"
          size="small"
          plain
          @click.stop="emit('collect', resource)"
        >
          <el-icon style="margin-right: 2px"><Star /></el-icon>
          {{ resource.isFavorited ? '取消收藏' : '收藏' }}
        </el-button>
        <el-button
          type="primary"
          size="small"
          :loading="downloading"
          @click.stop="handleDownload"
        >
          <el-icon style="margin-right: 2px"><Download /></el-icon>{{ downloading ? '下载中...' : '下载' }}
        </el-button>
      </div>
    </div>
  </el-card>

  <!-- List layout -->
  <div v-else class="resource-list-row" @click="emit('click', resource)">
    <div class="list-cell icon-cell">
      <span class="file-emoji-sm">{{ fileEmoji }}</span>
    </div>
    <div class="list-cell title-cell">
      <span class="list-title">{{ resource.title }}</span>
    </div>
    <div class="list-cell type-cell">
      <el-tag size="small" :type="tagType">{{ typeLabel }}</el-tag>
    </div>
    <div class="list-cell format-cell">
      <span class="list-meta">{{ resource.fileType?.toUpperCase() || '-' }}</span>
    </div>
    <div class="list-cell size-cell">
      <span class="list-meta">{{ formatFileSize(resource.fileSize) }}</span>
    </div>
    <div class="list-cell uploader-cell">
      <span class="list-meta">{{ resource.userNickname }}</span>
    </div>
    <div class="list-cell downloads-cell">
      <span class="list-meta">{{ resource.downloadCount }}</span>
    </div>
    <div class="list-cell time-cell">
      <span class="list-meta">{{ formatTime(resource.createTime) }}</span>
    </div>
    <div class="list-cell actions-cell" @click.stop>
      <el-button
        v-if="showDelete"
        type="danger"
        size="small"
        plain
        @click="emit('delete', resource.id)"
      >
        删除
      </el-button>
      <el-button
        v-if="isPreviewable"
        type="warning"
        size="small"
        plain
        @click="emit('preview', resource)"
      >
        预览
      </el-button>
      <el-button
        :type="resource.isFavorited ? 'warning' : 'success'"
        size="small"
        plain
        @click="emit('collect', resource)"
      >
        <el-icon style="margin-right: 2px"><Star /></el-icon>
        {{ resource.isFavorited ? '取消' : '收藏' }}
      </el-button>
      <el-button
        type="primary"
        size="small"
        :loading="downloading"
        @click="handleDownload"
      >
        <el-icon style="margin-right: 2px"><Download /></el-icon>{{ downloading ? '下载中' : '下载' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatDateTime } from '@/utils/time'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, Download } from '@element-plus/icons-vue'
import { resourceApi } from '@/api/resource'
import type { Resource } from '@/types'

const props = withDefaults(defineProps<{
  resource: Resource
  showDelete?: boolean
  layout?: 'card' | 'list'
}>(), {
  layout: 'card'
})

const emit = defineEmits<{
  (e: 'click', resource: Resource): void
  (e: 'delete', resourceId: number): void
  (e: 'preview', resource: Resource): void
  (e: 'collect', resource: Resource): void
}>()

const downloading = ref(false)

const fileEmoji = computed(() => {
  const t = props.resource.fileType?.toLowerCase() || ''
  if (t === 'pdf') return '📄'
  if (['doc', 'docx'].includes(t)) return '📝'
  if (['ppt', 'pptx'].includes(t)) return '📊'
  if (['xls', 'xlsx'].includes(t)) return '📈'
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(t)) return '🖼️'
  if (t === 'txt') return '📃'
  return '📁'
})

const fileIconClass = computed(() => {
  const t = props.resource.fileType?.toLowerCase() || ''
  if (t === 'pdf') return 'file-pdf'
  if (['doc', 'docx'].includes(t)) return 'file-doc'
  if (['ppt', 'pptx'].includes(t)) return 'file-ppt'
  if (['xls', 'xlsx'].includes(t)) return 'file-xls'
  return 'file-default'
})

const previewableTypes = ['pdf', 'docx', 'doc', 'xlsx', 'xls', 'pptx', 'ppt', 'txt', 'jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp']
const isPreviewable = computed(() => {
  const t = props.resource.fileType?.toLowerCase() || ''
  return previewableTypes.includes(t)
})

const typeLabel = computed(() => {
  const labels = ['试卷', '习题', '笔记', '课件']
  return labels[props.resource.resourceType ?? 0] || '未知'
})

const tagType = computed(() => {
  const types: any[] = ['', 'success', 'warning', '']
  return types[props.resource.resourceType ?? 0] || ''
})

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

async function handleDownload() {
  downloading.value = true
  try {
    const blob = await resourceApi.download(props.resource.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = (props.resource.title || 'download') + '.' + (props.resource.fileType || '')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    props.resource.downloadCount++
    ElMessage.success('下载成功')
  } catch {
    ElMessage.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

function formatTime(time: string) { return formatDateTime(time) }
</script>

<style scoped>
.resource-card {
  margin-bottom: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.card-top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #4f8ef7, #8b5cf6, #ec4899);
}

.resource-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.file-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-emoji {
  font-size: 28px;
}

.file-default { background: #eff6ff; }
.file-pdf { background: #fef2f2; }
.file-doc { background: #eff6ff; }
.file-ppt { background: #fef9c3; }
.file-xls { background: #f0fdf4; }

.resource-info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.downloads {
  display: flex;
  align-items: center;
  gap: 2px;
}

.resource-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.time {
  font-size: 12px;
  color: #c0c4cc;
}

/* ========== List layout ========== */
.resource-list-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  margin-bottom: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.resource-list-row:hover {
  border-color: #4f8ef7;
  box-shadow: 0 2px 8px rgba(79, 142, 247, 0.12);
  transform: translateY(-1px);
}

.list-cell {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.icon-cell {
  width: 40px;
  justify-content: center;
}

.file-emoji-sm {
  font-size: 22px;
}

.title-cell {
  flex: 1;
  min-width: 120px;
  overflow: hidden;
}

.list-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-cell {
  width: 60px;
  justify-content: center;
}

.format-cell {
  width: 56px;
  justify-content: center;
}

.size-cell {
  width: 72px;
  justify-content: center;
}

.uploader-cell {
  width: 80px;
  justify-content: center;
}

.downloads-cell {
  width: 48px;
  justify-content: center;
}

.time-cell {
  width: 140px;
  justify-content: center;
}

.list-meta {
  font-size: 13px;
  color: #64748b;
}

.actions-cell {
  display: flex;
  gap: 4px;
  flex-wrap: nowrap;
}
</style>