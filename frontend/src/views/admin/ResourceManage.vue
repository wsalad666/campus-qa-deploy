<template>
  <AdminLayout title="资源管理">
    <div class="resource-manage">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索ID、标题、描述..."
          clearable
          style="width: 300px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
      </div>

      <el-table :data="resourceList" border stripe style="width: 100%" v-loading="loading" :row-class-name="rowClassName">
        <template #empty><el-empty description="暂无资源数据" /></template>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userNickname" label="上传者" width="120" />
        <el-table-column prop="fileType" label="文件类型" width="100" />
        <el-table-column label="资料类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ getResourceTypeLabel(row.resourceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="100">
          <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载次数" width="100" />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isOffline ? 'danger' : 'success'" size="small">
              {{ row.isOffline ? '已下架' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
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

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>

      <BanDialog v-model="banVisible" :user-id="banUserId" :user-nickname="banUserNickname" @done="fetchResources" />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { usePagination } from '@/composables/usePagination'
import type { Resource, PageResult } from '@/types'
import AdminLayout from './AdminLayout.vue'
import BanDialog from './BanDialog.vue'

const { pageNum, pageSize, total, handlePageChange, handleSizeChange } = usePagination()

const resourceList = ref<Resource[]>([])
const loading = ref(false)
const keyword = ref('')

const banVisible = ref(false)
const banUserId = ref(0)
const banUserNickname = ref('')

function rowClassName({ row }: { row: Resource }) {
  return row.isOffline ? 'offline-row' : ''
}

function getResourceTypeLabel(type: number | null): string {
  const labels = ['试卷', '习题', '笔记', '课件']
  if (type === null || type === undefined) return '未知'
  return labels[type] || '未知'
}

function formatFileSize(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i]
}

async function fetchResources() {
  loading.value = true
  try {
    const data = await adminApi.getResourceList({
      pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined,
    }) as PageResult<Resource>
    resourceList.value = data.records
    total.value = data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() {
  pageNum.value = 1
  fetchResources()
}

function handleOffline(row: Resource) {
  const action = row.isOffline ? '上架' : '下架'
  ElMessageBox.confirm(`确定要${action}「${row.title}」吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    await adminApi.offlineResource(row.id)
    ElMessage.success(`${action}成功`)
    await fetchResources()
  }).catch(() => {})
}

function handleBan(row: Resource) {
  banUserId.value = row.userId
  banUserNickname.value = row.userNickname
  banVisible.value = true
}

watch([pageNum, pageSize], () => fetchResources())
onMounted(() => fetchResources())
</script>

<style scoped>
.resource-manage { background: #fff; padding: 20px; border-radius: 4px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
:deep(.offline-row) { background-color: #f5f5f5; color: #c0c4cc; }
</style>