<template>
  <AdminLayout title="举报工单管理">
    <div class="manage-page">
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="处理状态" clearable style="width: 140px" @change="handleSearch">
          <el-option label="待处理" :value="0" />
          <el-option label="已处理-下架" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
        <el-input
          v-model="keyword"
          placeholder="搜索工单ID、举报原因..."
          clearable
          style="width: 300px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
      </div>

      <el-table :data="list" border stripe style="width: 100%" v-loading="loading">
        <template #empty><el-empty description="暂无举报工单" /></template>
        <el-table-column prop="id" label="工单ID" width="80" />
        <el-table-column prop="reporterNickname" label="举报人" width="120" />
        <el-table-column label="举报类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.targetType === 1 ? 'warning' : 'info'" size="small">
              {{ row.targetType === 1 ? '提问' : '回答' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetTitle" label="被举报内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reason" label="举报原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 0 ? '待处理' : row.status === 1 ? '已处理-下架' : '已驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="handleDetail(row)">
              <el-icon><View /></el-icon>
              查看问题详情
            </el-button>
            <el-button
              v-if="row.status === 0"
              size="small" type="success" link
              @click="handleProcess(row)"
            >处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page-num="pageNum" v-model:page-size="pageSize" :total="total" @change="fetchData" />

      <!-- 详情弹窗 -->
      <el-dialog v-model="detailVisible" title="举报工单详情" width="900px" top="5vh" class="report-detail-dialog">
        <template v-if="currentReport">
          <!-- 基础信息 -->
          <el-descriptions :column="3" border size="small" class="detail-section">
            <el-descriptions-item label="工单ID">{{ currentReport.id }}</el-descriptions-item>
            <el-descriptions-item label="举报人">{{ currentReport.reporterNickname }}</el-descriptions-item>
            <el-descriptions-item label="处理状态">
              <el-tag :type="currentReport.status === 0 ? 'danger' : currentReport.status === 1 ? 'success' : 'info'" size="small">
                {{ currentReport.status === 0 ? '待处理' : currentReport.status === 1 ? '已处理-下架' : '已驳回' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="举报类型">{{ currentReport.targetType === 1 ? '提问' : '回答' }}</el-descriptions-item>
            <el-descriptions-item label="举报时间">{{ currentReport.createTime }}</el-descriptions-item>
            <el-descriptions-item label="处理人">{{ currentReport.handlerNickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="举报原因" :span="3">{{ currentReport.reason }}</el-descriptions-item>
            <el-descriptions-item label="处理备注" :span="3">{{ currentReport.handleNote || '-' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 完整问答上下文 -->
          <template v-if="currentReport.questionId">
            <el-divider content-position="left">
              <span class="context-title">完整问答上下文</span>
            </el-divider>

            <!-- 提问区域 -->
            <div class="context-block" :class="{ 'is-reported-target': currentReport.targetType === 1 }">
              <div class="context-header">
                <span class="context-label">提问</span>
                <span class="context-author">{{ currentReport.questionUserNickname }}</span>
                <span class="context-time">{{ currentReport.questionCreateTime }}</span>
                <el-tag v-if="currentReport.targetType === 1" type="danger" size="small" effect="dark">被举报</el-tag>
              </div>
              <h4 class="context-question-title">{{ currentReport.questionTitle }}</h4>
              <div class="context-content" v-html="resolveContentImages(currentReport.questionContent)" />
            </div>

            <!-- 回答区域 -->
            <div v-if="currentReport.answers?.length" class="context-answers">
              <div class="context-header-secondary">
                全部回答（{{ currentReport.answers.length }}）
              </div>
              <div
                v-for="a in currentReport.answers"
                :key="a.id"
                class="context-answer-item"
                :class="{ 'is-reported-target': a.isReported }"
              >
                <div class="context-answer-header">
                  <span class="context-author">{{ a.userNickname }}</span>
                  <span class="context-time">{{ a.createTime }}</span>
                  <el-tag v-if="a.isAccepted === 1" type="success" size="small">已采纳</el-tag>
                  <el-tag v-if="a.isOffline === 1" type="info" size="small">已下架</el-tag>
                  <el-tag v-if="a.isReported" type="danger" size="small" effect="dark">被举报</el-tag>
                  <span class="context-stats">点赞 {{ a.likeCount || 0 }} · 评论 {{ a.commentCount || 0 }}</span>
                </div>
                <div class="context-content" v-html="resolveContentImages(a.content)" />
              </div>
            </div>
            <el-empty v-else description="暂无回答" :image-size="40" />

            <!-- 跳转按钮 -->
            <div class="context-footer">
              <el-button
                type="primary"
                size="small"
                :icon="Link"
                @click="openQuestionPage(currentReport.questionId)"
              >
                打开完整问答页面
              </el-button>
            </div>
          </template>
        </template>
      </el-dialog>

      <!-- 处理弹窗 -->
      <el-dialog v-model="processVisible" title="处理举报工单" width="500px" :close-on-click-modal="false">
        <el-form :model="processForm" label-width="100px">
          <el-form-item label="处理结果" required>
            <el-radio-group v-model="processForm.status">
              <el-radio :value="1">确认违规，下架内容</el-radio>
              <el-radio :value="2">驳回举报</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="额外禁言">
            <el-checkbox v-model="processForm.needBan" :disabled="processForm.status === 2">同时禁言用户</el-checkbox>
          </el-form-item>
          <template v-if="processForm.needBan && processForm.status === 1">
            <el-form-item label="禁言等级">
              <el-radio-group v-model="processForm.banType">
                <el-radio :value="1">轻度（3天）</el-radio>
                <el-radio :value="2">中度（7天）</el-radio>
                <el-radio :value="3">重度（永久）</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="禁言原因">
              <el-input v-model="processForm.banReason" type="textarea" :rows="2" placeholder="请输入禁言原因" />
            </el-form-item>
          </template>
          <el-form-item label="处理备注">
            <el-input v-model="processForm.handleNote" type="textarea" :rows="2" placeholder="可选" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="processVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitProcess">确认处理</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Link, View } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { usePagination } from '@/composables/usePagination'
import type { ReportVO, PageResult } from '@/types'
import AdminLayout from './AdminLayout.vue'
import Pagination from '@/components/Pagination.vue'

const { pageNum, pageSize, total, handlePageChange, handleSizeChange } = usePagination()

const list = ref<ReportVO[]>([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

const detailVisible = ref(false)
const currentReport = ref<ReportVO | null>(null)

const processVisible = ref(false)
const processingReportId = ref(0)
const submitting = ref(false)
const processForm = ref({
  status: 1,
  needBan: false,
  banType: 1,
  banReason: '',
  handleNote: '',
})

async function fetchData() {
  loading.value = true
  try {
    const data = await adminApi.getReportList({
      pageNum: pageNum.value, pageSize: pageSize.value,
      status: statusFilter.value, keyword: keyword.value || undefined,
    }) as PageResult<ReportVO>
    list.value = data.records
    // 待处理工单置顶
    list.value.sort((a, b) => {
      if (a.status === 0 && b.status !== 0) return -1
      if (a.status !== 0 && b.status === 0) return 1
      return 0
    })
    total.value = data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() {
  pageNum.value = 1
  fetchData()
}

async function handleDetail(row: ReportVO) {
  try {
    const data = await adminApi.getReportDetail(row.id) as ReportVO
    currentReport.value = data
    detailVisible.value = true
  } catch { /* handled */ }
}

function handleProcess(row: ReportVO) {
  processingReportId.value = row.id
  processForm.value = { status: 1, needBan: false, banType: 1, banReason: '', handleNote: '' }
  processVisible.value = true
}

async function submitProcess() {
  submitting.value = true
  try {
    await adminApi.handleReport(processingReportId.value, {
      status: processForm.value.status,
      handleNote: processForm.value.handleNote || undefined,
      banType: processForm.value.needBan && processForm.value.status === 1 ? processForm.value.banType : undefined,
      banReason: processForm.value.needBan && processForm.value.status === 1 ? processForm.value.banReason || undefined : undefined,
    })
    ElMessage.success('处理完成')
    processVisible.value = false
    await fetchData()
  } catch { /* handled */ }
  finally { submitting.value = false }
}

function openQuestionPage(questionId: number) {
  // 管理员在新标签页打开学生问答详情，使用admin token
  const adminUserId = localStorage.getItem('adminUserId')
  const url = adminUserId
    ? `http://localhost:5173/student/qa/${questionId}`
    : `http://localhost:5173/student/qa/${questionId}`
  window.open(url, '_blank')
}

function resolveContentImages(html: string): string {
  if (!html) return ''
  return html.replace(/<img[^>]+src=["']([^"']+)["']/gi, (match, src) => {
    if (src.startsWith('http://') || src.startsWith('https://') || src.startsWith('data:')) return match
    return match.replace(src, 'http://localhost:8080' + (src.startsWith('/') ? '' : '/') + src)
  })
}

watch([pageNum, pageSize], () => fetchData())
onMounted(() => fetchData())
</script>

<style scoped>
.manage-page { background: #fff; padding: 20px; border-radius: 4px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }

.detail-section { margin-bottom: 8px; }

.context-title { font-size: 14px; font-weight: 600; color: #303133; }

.context-block {
  padding: 16px;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 12px;
}
.context-block.is-reported-target {
  border-color: #f56c6c;
  background: #fef0f0;
}

.context-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.context-label {
  font-size: 12px;
  color: #909399;
  background: #e9e9eb;
  padding: 2px 8px;
  border-radius: 3px;
}
.context-author {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}
.context-time {
  font-size: 12px;
  color: #c0c4cc;
}
.context-question-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px;
}

.context-content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  max-height: 300px;
  overflow-y: auto;
}
.context-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 4px 0;
}

.context-answers {
  margin-top: 12px;
}
.context-header-secondary {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.context-answer-item {
  padding: 12px;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
}
.context-answer-item.is-reported-target {
  border-color: #f56c6c;
  background: #fef0f0;
}

.context-answer-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.context-stats {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: auto;
}

.context-footer {
  margin-top: 16px;
  text-align: center;
}
</style>