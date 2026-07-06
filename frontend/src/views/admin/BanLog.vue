<template>
  <AdminLayout title="处罚日志">
    <div class="manage-page">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户ID、处罚原因..."
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
        <template #empty><el-empty description="暂无处罚记录" /></template>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNickname" label="被处罚用户" width="120" />
        <el-table-column label="处罚等级" width="120">
          <template #default="{ row }">
            <el-tag :type="row.banType === 1 ? 'warning' : row.banType === 2 ? 'danger' : ''" size="small">
              {{ row.banType === 1 ? '轻度3天' : row.banType === 2 ? '中度7天' : '永久禁言' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="banReason" label="处罚原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="adminNickname" label="操作管理员" width="120" />
        <el-table-column label="来源" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.sourceType === 1 ? '举报' : row.sourceType === 2 ? '巡查' : '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="banStartTime" label="开始时间" width="180" />
        <el-table-column label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.banEndTime || '永久' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'info'" size="small">
              {{ row.isActive ? '生效中' : '已解除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.isActive"
              type="warning"
              size="small"
              @click="handleUnban(row)"
            >
              取消禁言
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page-num="pageNum" v-model:page-size="pageSize" :total="total" @change="fetchData" />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { usePagination } from '@/composables/usePagination'
import type { UserBanLogVO, PageResult } from '@/types'
import AdminLayout from './AdminLayout.vue'
import Pagination from '@/components/Pagination.vue'

const { pageNum, pageSize, total, handlePageChange, handleSizeChange } = usePagination()

const list = ref<UserBanLogVO[]>([])
const loading = ref(false)
const keyword = ref('')

async function fetchData() {
  loading.value = true
  try {
    const data = await adminApi.getBanLogList({
      pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined,
    }) as PageResult<UserBanLogVO>
    list.value = data.records
    total.value = data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() {
  pageNum.value = 1
  fetchData()
}

async function handleUnban(row: UserBanLogVO) {
  try {
    await ElMessageBox.confirm(
      `确定要取消对用户「${row.userNickname}」的禁言吗？`,
      '取消禁言',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await adminApi.unbanUser(row.userId)
    ElMessage.success('已取消禁言')
    fetchData()
  } catch {
    // 取消操作
  }
}

watch([pageNum, pageSize], () => fetchData())
onMounted(() => fetchData())
</script>

<style scoped>
.manage-page { background: #fff; padding: 20px; border-radius: 4px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
</style>