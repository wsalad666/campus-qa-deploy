<template>
  <div class="file-upload">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px" label-position="top">
      <el-form-item label="所属课程" prop="courseId">
        <el-select
          v-model="formData.courseId"
          placeholder="请选择课程"
          :loading="courseLoading"
          style="width: 100%"
        >
          <el-option
            v-for="c in courses"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="资料类型" prop="resourceType">
        <el-select
          v-model="formData.resourceType"
          placeholder="请选择资料类型"
          style="width: 100%"
        >
          <el-option label="试卷" :value="0" />
          <el-option label="习题" :value="1" />
          <el-option label="笔记" :value="2" />
          <el-option label="课件" :value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="文件标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入文件标题" maxlength="100" />
      </el-form-item>

      <el-form-item label="文件描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入文件描述（选填）"
          maxlength="500"
        />
      </el-form-item>

      <el-form-item label="选择文件" prop="file">
        <div class="file-input-wrapper">
          <input
            ref="fileInputRef"
            type="file"
            accept=".pdf,.doc,.docx,.jpg,.png"
            @change="handleFileChange"
          />
          <p v-if="selectedFile" class="file-name">
            {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})
          </p>
          <p class="file-hint">支持 .pdf, .doc, .docx, .jpg, .png 格式，最大 100MB</p>
        </div>
      </el-form-item>

      <el-form-item v-if="uploadPercent > 0 && uploadPercent < 100">
        <el-progress :percentage="uploadPercent" :status="uploadStatus" />
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          :loading="uploading"
          :disabled="!canUpload"
          @click="handleUpload"
        >
          上传
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import { resourceApi } from '@/api/resource'
import type { Course } from '@/types'

const props = defineProps<{
  courseId?: number
}>()

const emit = defineEmits<{
  (e: 'success'): void
  (e: 'error', msg: string): void
}>()

const formRef = ref<FormInstance>()
const fileInputRef = ref<HTMLInputElement>()
const courses = ref<Course[]>([])
const courseLoading = ref(false)
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const uploadPercent = ref(0)
const uploadStatus = ref<'success' | 'exception' | 'warning' | ''>('')

const formData = reactive({
  courseId: props.courseId || '' as number | string,
  resourceType: null as number | null,
  title: '',
  description: '',
  file: null as File | null,
})

const rules: FormRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: ['blur', 'change'] }],
  resourceType: [{ required: true, message: '请选择资料类型', trigger: ['blur', 'change'] }],
  title: [{ required: true, message: '请输入文件标题', trigger: ['blur', 'change'] }],
  file: [{ required: true, message: '请选择文件', trigger: 'change' }],
}

const canUpload = computed(() => {
  return formData.courseId && formData.resourceType !== null && formData.title.trim() && selectedFile.value
})

onMounted(async () => {
  courseLoading.value = true
  try {
    const res = await adminApi.getCourseList()
    courses.value = (Array.isArray(res) ? res : res?.records) || []
  } catch {
    courses.value = []
  } finally {
    courseLoading.value = false
  }
})

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    emit('error', '文件大小不能超过 100MB')
    return
  }

  selectedFile.value = file
  formData.file = file
  formRef.value?.validateField('file')
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

async function handleUpload() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  uploading.value = true
  uploadPercent.value = 0
  uploadStatus.value = ''

  const formDataUpload = new FormData()
  formDataUpload.append('courseId', String(formData.courseId))
  formDataUpload.append('resourceType', String(formData.resourceType))
  formDataUpload.append('title', formData.title.trim())
  formDataUpload.append('description', formData.description.trim())
  formDataUpload.append('file', selectedFile.value!)

  try {
    uploadPercent.value = 50
    await resourceApi.upload(formDataUpload)
    uploadPercent.value = 100
    uploadStatus.value = 'success'
    emit('success')

    formData.title = ''
    formData.description = ''
    formData.resourceType = null
    selectedFile.value = null
    formData.file = null
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  } catch {
    uploadPercent.value = 0
    uploadStatus.value = 'exception'
    emit('error', '上传失败，请重试')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.file-upload {
  max-width: 500px;
}

.file-input-wrapper {
  width: 100%;
}

.file-input-wrapper input[type='file'] {
  width: 100%;
}

.file-name {
  font-size: 13px;
  color: #409eff;
  margin: 4px 0 0;
}

.file-hint {
  font-size: 12px;
  color: #c0c4cc;
  margin: 4px 0 0;
}
</style>