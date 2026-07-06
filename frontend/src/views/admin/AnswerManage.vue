<template>
  <AdminLayout title="回答管理">
    <div class="manage-page">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索回答ID、内容..."
          clearable
          style="width: 300px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
      </div>

      <el-table :data="list" border stripe style="width: 100%" v-loading="loading" :row-class-name="rowClassName">
        <template #empty><el-empty description="暂无回答" /></template>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="questionTitle" label="所属问题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userNickname" label="回答者" width="120" />
        <el-table-column prop="content" label="回答内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="likeCount" label="点赞" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column label="采纳" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isAccepted" type="success" size="small">已采纳</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isOffline ? 'danger' : 'success'" size="small">
              {{ row.isOffline ? '已下架' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isOffline"
              size="small" type="danger" link
              @click="handleOffline(row)"
            >下架</el-button>
            <el-button
              v-else
              size="small" type="success" link
              @click="handleOffline(row)"
            >上架</el-button>
            <el-button
              size="small" type="warning" link
              @click="handleBan(row)"
            >禁言用户</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page-num="pageNum" v-model:page-size="pageSize" :total="total" @change="fetchData" />

      <BanDialog v-model="banVisible" :user-id="banUserId" :user-nickname="banUserNickname" @done="fetchData" />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { usePagination } from '@/composables/usePagination'
import type { AnswerManageVO, PageResult } from '@/types'
import AdminLayout from './AdminLayout.vue'
import Pagination from '@/components/Pagination.vue'
import BanDialog from './BanDialog.vue'

const { pageNum, pageSize, total, handlePageChange, handleSizeChange } = usePagination()

const list = ref<AnswerManageVO[]>([])
const loading = ref(false)
const keyword = ref('')

const banVisible = ref(false)
const banUserId = ref(0)
const banUserNickname = ref('')

function rowClassName({ row }: { row: AnswerManageVO }) {
  return row.isOffline ? 'offline-row' : ''
}

async function fetchData() {
  loading.value = true
  try {
    const data = await adminApi.getAnswerList({
      pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined,
    }) as PageResult<AnswerManageVO>
    list.value = data.records
    total.value = data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() {
  pageNum.value = 1
  fetchData()
}

function handleOffline(row: AnswerManageVO) {
  const action = row.isOffline ? '上架' : '下架'
  ElMessageBox.confirm(`确定要${action}该回答吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    await adminApi.offlineAnswer(row.id)
    ElMessage.success(`${action}成功`)
    await fetchData()
  }).catch(() => {})
}

function handleBan(row: AnswerManageVO) {
  banUserId.value = row.userId
  banUserNickname.value = row.userNickname
  banVisible.value = true
}

watch([pageNum, pageSize], () => fetchData())
onMounted(() => fetchData())
</script>

<style scoped>
.manage-page { background: #fff; padding: 20px; border-radius: 4px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
:deep(.offline-row) { background-color: #f5f5f5; color: #c0c4cc; }
</style>