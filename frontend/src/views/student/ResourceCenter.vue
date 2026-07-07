<script setup lang="ts">
import { formatDateTime } from '@/utils/time'
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'
import Pagination from '@/components/Pagination.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import PreviewDialog from '@/components/PreviewDialog.vue'
import CollectFolderSelect from '@/components/CollectFolderSelect.vue'
import FileUpload from '@/components/FileUpload.vue'
import { resourceApi } from '@/api/resource'
import { adminApi } from '@/api/admin'
import { usePagination } from '@/composables/usePagination'
import type { Resource, Course } from '@/types'

const route = useRoute()

const { pageNum, pageSize, total, reset, handlePageChange, handleSizeChange } =
  usePagination(12)

const resources = ref<Resource[]>([])
const courses = ref<Course[]>([])
const selectedCourseId = ref<number | undefined>(undefined)
const selectedResourceType = ref<number | undefined>(undefined)
const searchKeyword = ref('')
const uploadDialogVisible = ref(false)
const loading = ref(false)

// 详情弹窗
const detailDialogVisible = ref(false)
const detailResource = ref<Resource | null>(null)

// 预览弹窗
const previewDialogVisible = ref(false)
const previewResource = ref<Resource | null>(null)

// 收藏弹窗
const collectDialogVisible = ref(false)
const collectTargetType = ref(2)
const collectTargetId = ref(0)

const resourceTypeOptions = [
  { label: '试卷', value: 0 },
  { label: '习题', value: 1 },
  { label: '笔记', value: 2 },
  { label: '课件', value: 3 },
]

function getResourceTypeLabel(type: number | null): string {
  if (type === null || type === undefined) return '未知'
  const found = resourceTypeOptions.find(o => o.value === type)
  return found ? found.label : '未知'
}

async function fetchCourses() {
  try {
    const res: any = await adminApi.getCourseList()
    courses.value = res || []
  } catch {
    // error handled by interceptor
  }
}

async function fetchResources() {
  loading.value = true
  try {
    const res: any = await resourceApi.getList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      courseId: selectedCourseId.value,
      keyword: searchKeyword.value || undefined,
      resourceType: selectedResourceType.value,
    })
    resources.value = res.records || []
    total.value = res.total
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

function onCourseFilter(courseId: number | undefined) {
  selectedCourseId.value = courseId
  reset()
  fetchResources()
}

function onTypeFilter() {
  reset()
  fetchResources()
}

function onSearch() {
  reset()
  fetchResources()
}

function onCourseChange(courseId: number | undefined) {
  selectedCourseId.value = courseId
  reset()
  fetchResources()
}

function onUploadSuccess() {
  uploadDialogVisible.value = false
  reset()
  fetchResources()
}

function onUploadError() {
  // error handled by component
}

function onResourceClick(resource: Resource) {
  detailResource.value = resource
  detailDialogVisible.value = true
}

function onResourcePreview(resource: Resource) {
  previewResource.value = resource
  previewDialogVisible.value = true
}

async function onResourceCollect(resource: Resource) {
  try {
    const res = await userApi.isCollectedAny(2, resource.id)
    if (res) {
      await ElMessageBox.confirm('确定取消收藏该资源？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userApi.removeCollectByTarget(2, resource.id)
      ElMessage.success('已取消收藏')
      fetchResources()
      return
    }
  } catch {
    // 未收藏或取消失败，继续打开收藏对话框
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

watch([pageNum, pageSize], () => {
  fetchResources()
})

onMounted(() => {
  fetchCourses()
  // 从URL参数读取keyword
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword as string
  }
  fetchResources()
})
</script>

<template>
  <div class="resource-container">
    <AppHeader />
    <div class="resource-body">
      <AppSidebar @course-change="onCourseChange" />
      <main class="resource-main">
        <div class="resource-toolbar">
          <div class="toolbar-left">
            <el-select
              v-model="selectedCourseId"
              placeholder="全部课程"
              clearable
              style="width: 180px"
              @change="onCourseFilter"
            >
              <el-option
                v-for="c in courses"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              />
            </el-select>
            <el-select
              v-model="selectedResourceType"
              placeholder="资料类型"
              clearable
              style="width: 140px"
              @change="onTypeFilter"
            >
              <el-option
                v-for="t in resourceTypeOptions"
                :key="t.value"
                :label="t.label"
                :value="t.value"
              />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文件名..."
              clearable
              style="width: 220px"
              @keyup.enter="onSearch"
            >
              <template #append>
                <el-button @click="onSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
          <el-button type="primary" @click="uploadDialogVisible = true">
            上传资源
          </el-button>
        </div>

        <el-row :gutter="16" v-loading="loading">
          <el-col
            v-for="r in resources"
            :key="r.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <ResourceCard :resource="r" @click="onResourceClick" @preview="onResourcePreview" @collect="onResourceCollect" />
          </el-col>
        </el-row>

        <el-empty v-if="!loading && resources.length === 0" description="暂无资源" />

        <div v-if="total > pageSize" class="resource-pagination">
          <Pagination
            :total="total"
            :page-num="pageNum"
            :page-size="pageSize"
            @update:page-num="handlePageChange"
            @update:page-size="handleSizeChange"
          />
        </div>

        <!-- 上传弹窗 -->
        <el-dialog
          v-model="uploadDialogVisible"
          title="上传资源"
          width="500px"
          destroy-on-close
        >
          <FileUpload
            :course-id="selectedCourseId"
            @success="onUploadSuccess"
            @error="onUploadError"
          />
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
      </main>
    </div>
  </div>
</template>

<style scoped>
.resource-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.resource-body {
  display: flex;
  padding-top: 60px;
}

.resource-main {
  flex: 1;
  margin-left: 220px;
  padding: 24px;
}

.resource-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.resource-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
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