<template>
  <el-dialog
    v-model="visible"
    :title="title"
    fullscreen
    destroy-on-close
    :close-on-click-modal="false"
    class="preview-dialog"
  >
    <div class="preview-container">
      <!-- Loading 遮罩层（绝对定位，不阻止组件渲染） -->
      <div v-if="loading" class="preview-loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>正在加载预览...</p>
      </div>

      <!-- 加载失败 -->
      <div v-else-if="loadError" class="preview-unsupported">
        <el-icon :size="48"><WarningFilled /></el-icon>
        <p>预览加载失败</p>
        <p class="preview-hint">{{ errorMsg }}</p>
        <el-button type="primary" @click="download">下载文件</el-button>
      </div>

      <!-- 不支持预览 -->
      <div v-else-if="!supported" class="preview-unsupported">
        <el-icon :size="48"><WarningFilled /></el-icon>
        <p>该文件格式不支持在线预览</p>
        <p class="preview-hint">支持格式：PDF、Word、Excel、PPT、TXT、图片</p>
        <el-button type="primary" @click="download">下载文件</el-button>
      </div>

      <!-- PDF：用浏览器原生 iframe 渲染，最可靠 -->
      <iframe
        v-if="visible && fileType === 'pdf' && !loadError && supported"
        :src="previewUrl"
        class="preview-iframe"
        @load="onRendered"
        @error="onError"
      />

      <!-- 图片：直接用 img 加载 -->
      <img
        v-if="visible && isImage && !loadError && supported"
        :src="previewUrl"
        class="preview-image-img"
        alt="preview"
        @load="onRendered"
        @error="onError"
      />

      <!-- TXT：fetch后直接显示文本 -->
      <div v-if="visible && fileType === 'txt' && txtContent" class="preview-txt">
        <pre>{{ txtContent }}</pre>
      </div>

      <!-- DOCX -->
      <vue-office-docx
        v-if="visible && (fileType === 'docx' || fileType === 'doc') && !loadError && supported"
        :src="previewUrl"
        class="preview-office"
        @rendered="onRendered"
        @error="onError"
      />

      <!-- XLSX -->
      <vue-office-excel
        v-if="visible && (fileType === 'xlsx' || fileType === 'xls') && !loadError && supported"
        :src="previewUrl"
        class="preview-office"
        @rendered="onRendered"
        @error="onError"
      />

      <!-- PPTX -->
      <vue-office-pptx
        v-if="visible && (fileType === 'pptx' || fileType === 'ppt') && !loadError && supported"
        :src="previewUrl"
        class="preview-office"
        @rendered="onRendered"
        @error="onError"
      />
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭预览</el-button>
      <el-button type="primary" @click="download">
        <el-icon style="margin-right: 4px"><Download /></el-icon>
        下载文件
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Loading, WarningFilled, Download } from '@element-plus/icons-vue'
import VueOfficeDocx from '@vue-office/docx'
import VueOfficeExcel from '@vue-office/excel'
import VueOfficePptx from '@vue-office/pptx'

const props = defineProps<{
  modelValue: boolean
  title?: string
  fileUrl?: string
  fileType?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const loading = ref(true)
const loadError = ref(false)
const errorMsg = ref('')
const txtContent = ref('')

const imageTypes = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg']
const isImage = computed(() => imageTypes.includes(props.fileType?.toLowerCase() || ''))
const supported = computed(() => {
  const t = props.fileType?.toLowerCase() || ''
  return ['pdf', 'docx', 'doc', 'xlsx', 'xls', 'pptx', 'ppt', 'txt'].includes(t) || isImage.value
})

const fileType = computed(() => props.fileType?.toLowerCase() || '')

// 统一用相对URL走Vite代理，避免跨域
const previewUrl = computed(() => {
  if (!props.fileUrl) return ''
  if (props.fileUrl.startsWith('http')) {
    try {
      return new URL(props.fileUrl).pathname
    } catch {
      return props.fileUrl
    }
  }
  if (props.fileUrl.startsWith('/uploads/')) return props.fileUrl
  if (props.fileUrl.startsWith('uploads/')) return '/' + props.fileUrl
  return '/uploads/' + props.fileUrl.replace(/^\/+/, '')
})

// 用于下载的完整URL
const downloadUrl = computed(() => {
  if (!props.fileUrl) return ''
  if (props.fileUrl.startsWith('http')) return props.fileUrl
  return '' + (props.fileUrl.startsWith('/') ? '' : '/') + props.fileUrl
})

let timeoutId: ReturnType<typeof setTimeout> | null = null

async function loadFile() {
  // 重置状态
  loading.value = true
  loadError.value = false
  errorMsg.value = ''
  txtContent.value = ''

  // 清除之前的超时定时器
  if (timeoutId) {
    clearTimeout(timeoutId)
    timeoutId = null
  }

  if (!props.fileUrl) {
    loading.value = false
    loadError.value = true
    errorMsg.value = '文件地址为空'
    return
  }

  // TXT：需要先fetch文本内容
  if (fileType.value === 'txt') {
    try {
      const res = await fetch(previewUrl.value)
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      txtContent.value = await res.text()
      loading.value = false
    } catch (e: any) {
      loading.value = false
      loadError.value = true
      errorMsg.value = e.message || '网络请求失败'
    }
    return
  }

  // PDF/图片/DOCX/XLSX/PPTX：组件已渲染，等待 @load/@rendered 事件
  // 设置15秒超时兜底
  timeoutId = setTimeout(() => {
    if (loading.value) {
      loading.value = false
      loadError.value = true
      errorMsg.value = '预览组件超时未响应，请尝试下载'
    }
  }, 15000)
}

function onRendered() {
  loading.value = false
  if (timeoutId) {
    clearTimeout(timeoutId)
    timeoutId = null
  }
}

function onError() {
  loading.value = false
  loadError.value = true
  errorMsg.value = '预览组件渲染失败，请尝试下载'
  if (timeoutId) {
    clearTimeout(timeoutId)
    timeoutId = null
  }
}

function download() {
  if (downloadUrl.value) {
    const a = document.createElement('a')
    a.href = downloadUrl.value
    a.download = props.title || ''
    a.target = '_blank'
    a.click()
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    loadFile()
  }
})
</script>

<style>
.preview-dialog .el-dialog__body {
  padding: 0 !important;
  height: calc(100vh - 120px);
  overflow: auto;
}
</style>

<style scoped>
.preview-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: auto;
}

.preview-loading,
.preview-unsupported {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #909399;
  background: #f0f2f5;
  z-index: 10;
}

.preview-loading p,
.preview-unsupported p {
  margin-top: 12px;
  font-size: 14px;
}

.preview-hint {
  font-size: 12px !important;
  color: #c0c4cc !important;
}

.preview-unsupported .el-button {
  margin-top: 16px;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-image-img {
  max-width: 100%;
  height: auto;
  display: block;
}

.preview-office {
  width: 100%;
  height: 100%;
}

.preview-txt {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 20px;
  background: #fff;
}

.preview-txt pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}
</style>
