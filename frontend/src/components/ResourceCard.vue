<template>
  <el-card class="resource-card" shadow="hover" @click="emit('click', resource)">
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
          type="success"
          size="small"
          plain
          @click.stop="emit('collect', resource)"
        >
          <el-icon style="margin-right: 2px"><Star /></el-icon>
          收藏
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
</template>

<script setup lang="ts">
import { formatDateTime } from '@/utils/time'
import { ref, computed } from 'vue'
import { formatDateTime } from '@/utils/time'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/time'
import { Star, Download } from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/time'
import { resourceApi } from '@/api/resource'
import { formatDateTime } from '@/utils/time'
import type { Resource } from '@/types'

const props = defineProps<{
  resource: Resource
  showDelete?: boolean
}>()

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

function formatTime(time: string) { return formatDateTime(time) }天前`
  return d.toLocaleDateString('zh-CN')
}
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
</style>