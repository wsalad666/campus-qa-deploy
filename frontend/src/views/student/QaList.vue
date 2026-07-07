<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import Pagination from '@/components/Pagination.vue'
import QuestionCard from '@/components/QuestionCard.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import SimilarQuestions from '@/components/SimilarQuestions.vue'
import { qaApi } from '@/api/qa'
import { adminApi } from '@/api/admin'
import { usePagination } from '@/composables/usePagination'
import type { Question, Course } from '@/types'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadRawFile } from 'element-plus'

const { pageNum, pageSize, total, reset, handlePageChange, handleSizeChange } =
  usePagination(10)

const questions = ref<Question[]>([])
const courses = ref<Course[]>([])
const selectedCourseId = ref<number | undefined>(undefined)
const selectedCourseIds = ref<number[]>([])
const keyword = ref('')
const route = useRoute()
const sort = ref<'time' | 'hot'>('time')
const loading = ref(false)

const publishDialogVisible = ref(false)
const publishLoading = ref(false)
const publishForm = ref({
  courseId: undefined as number | undefined,
  title: '',
  content: '',
})
const publishImageUrls = ref<string[]>([])
const publishUploadFiles = ref<UploadFile[]>([])

const publishFormRef = ref()
const publishRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: ['blur', 'change'] }],
  title: [{ required: true, message: '请输入问题标题', trigger: ['blur', 'change'] }],
  content: [{ required: true, message: '请输入问题内容', trigger: ['blur', 'change'] }],
}

async function fetchCourses() {
  try {
    const res: any = await adminApi.getCourseList()
    courses.value = res || []
  } catch {
    // error handled by interceptor
  }
}

async function fetchQuestions() {
  loading.value = true
  try {
    const res: any = await qaApi.getQuestionList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      courseIds: selectedCourseIds.value,
      keyword: keyword.value || undefined,
      sort: sort.value,
    })
    questions.value = res.records || []
    total.value = res.total
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

function onCourseChangeFromSidebar(courseIds: number[]) {
  selectedCourseIds.value = [...courseIds]
  reset()
  fetchQuestions()
}

function onSearch() {
  reset()
  fetchQuestions()
}

function onSortChange() {
  reset()
  fetchQuestions()
}

function onCourseFilter() {
  reset()
  fetchQuestions()
}

function beforeAvatarUpload(rawFile: UploadRawFile) {
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

async function handleUploadSuccess(_response: any, uploadFile: UploadFile) {
  publishUploadFiles.value.push(uploadFile)
}

function handleUploadRemove(uploadFile: UploadFile) {
  const idx = publishUploadFiles.value.findIndex(f => f.uid === uploadFile.uid)
  if (idx > -1) {
    publishUploadFiles.value.splice(idx, 1)
  }
}

async function handlePublish() {
  const valid = await publishFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!publishForm.value.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!publishForm.value.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!publishForm.value.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  publishLoading.value = true
  try {
    const imageUrls: string[] = []
    for (const f of publishUploadFiles.value) {
      if (f.raw) {
        const path = await qaApi.uploadImage(f.raw)
        imageUrls.push(path)
      }
    }
    await qaApi.publishQuestion({
      courseId: publishForm.value.courseId,
      title: publishForm.value.title,
      content: publishForm.value.content,
      imageUrls: imageUrls.length > 0 ? imageUrls : undefined,
    })
    ElMessage.success('发布成功')
    publishDialogVisible.value = false
    publishForm.value = { courseId: undefined, title: '', content: '' }
    publishImageUrls.value = []
    publishUploadFiles.value = []
    reset()
    fetchQuestions()
  } catch {
    // error handled by interceptor
  } finally {
    publishLoading.value = false
  }
}

watch([pageNum, pageSize], () => {
  fetchQuestions()
})

onMounted(() => {
  fetchCourses()
  // 从URL读取搜索关键词
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
  }
  fetchQuestions()
})
</script>

<template>
  <div class="qalist-container">
    <AppHeader />
    <div class="qalist-body">
      <AppSidebar @course-change="onCourseChangeFromSidebar" />
      <main class="qalist-main">
        <div class="qalist-toolbar">
          <div class="toolbar-left">
            <el-input
              v-model="keyword"
              placeholder="搜索问题..."
              clearable
              style="width: 240px"
              @keyup.enter="onSearch"
            >
              <template #append>
                <el-button @click="onSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
            <el-radio-group v-model="sort" @change="onSortChange">
              <el-radio-button value="time">最新</el-radio-button>
              <el-radio-button value="hot">最热</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="primary" @click="publishDialogVisible = true">
            ✏️ 发布提问
          </el-button>
        </div>

        <div v-loading="loading" class="question-list">
          <QuestionCard
            v-for="q in questions"
            :key="q.id"
            :question="q"
          />
        </div>

        <el-empty v-if="!loading && questions.length === 0" description="暂无问题" />

        <div v-if="total > pageSize" class="qalist-pagination">
          <Pagination
            :total="total"
            :page-num="pageNum"
            :page-size="pageSize"
            @update:page-num="handlePageChange"
            @update:page-size="handleSizeChange"
          />
        </div>

        <el-dialog
          v-model="publishDialogVisible"
          title="发布提问"
          width="960px"
          destroy-on-close
        >
          <div class="publish-layout">
            <div class="publish-form">
              <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="80px">
                <el-form-item label="所属课程" prop="courseId">
                  <el-select
                    v-model="publishForm.courseId"
                    placeholder="请选择课程"
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
                <el-form-item label="问题标题" prop="title">
                  <el-input v-model="publishForm.title" placeholder="请输入问题标题" />
                </el-form-item>
                <el-form-item label="问题内容" prop="content">
                  <RichTextEditor v-model="publishForm.content" />
                </el-form-item>
                <el-form-item label="上传图片">
                  <el-upload
                    v-model:file-list="publishUploadFiles"
                    :auto-upload="false"
                    :before-upload="beforeAvatarUpload"
                    :on-success="handleUploadSuccess"
                    :on-remove="handleUploadRemove"
                    list-type="picture-card"
                    accept="image/jpeg,image/png"
                    multiple
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <p class="upload-hint">支持 JPG、PNG 格式，每张最大 5MB，提交时自动上传</p>
                </el-form-item>
              </el-form>
            </div>
            <div class="publish-sidebar" v-if="publishForm.courseId">
              <SimilarQuestions
                :course-id="publishForm.courseId"
                :title="publishForm.title"
                :content="publishForm.content"
                mode="sidebar"
              />
            </div>
          </div>
          <template #footer>
            <el-button @click="publishDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="publishLoading" @click="handlePublish">
              发布
            </el-button>
          </template>
        </el-dialog>
      </main>
    </div>
  </div>
</template>

<style scoped>
.qalist-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.qalist-body {
  display: flex;
  padding-top: 60px;
}

.qalist-main {
  flex: 1;
  margin-left: 220px;
  padding: 24px;
}

.qalist-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.question-list {
  min-height: 200px;
}

.qalist-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.publish-layout {
  display: flex;
  gap: 20px;
}

.publish-form {
  flex: 1;
  min-width: 0;
}

.publish-sidebar {
  width: 280px;
  flex-shrink: 0;
  border-left: 1px solid #ebeef5;
  padding-left: 16px;
  max-height: 500px;
  overflow-y: auto;
}
</style>