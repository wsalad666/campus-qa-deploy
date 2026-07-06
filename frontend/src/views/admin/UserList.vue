<template>
  <AdminLayout title="用户列表">
    <div class="manage-page">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名、昵称、学号、ID..."
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
        <template #empty><el-empty description="暂无用户" /></template>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column label="禁言状态" width="150">
          <template #default="{ row }">
            <template v-if="row.banEndTime && new Date(row.banEndTime) > new Date()">
              <el-tag type="danger" size="small">
                {{ row.banEndTime === null ? '永久禁言' : '禁言中' }}
              </el-tag>
              <div class="ban-reason">{{ row.banReason }}</div>
            </template>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="warning" link @click="handleBan(row)">禁言</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination v-model:page-num="pageNum" v-model:page-size="pageSize" :total="total" @change="fetchData" />

      <BanDialog v-model="banVisible" :user-id="banUserId" :user-nickname="banUserNickname" @done="onBanDone" />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { usePagination } from '@/composables/usePagination'
import type { UserManageVO, PageResult } from '@/types'
import AdminLayout from './AdminLayout.vue'
import Pagination from '@/components/Pagination.vue'
import BanDialog from './BanDialog.vue'

const { pageNum, pageSize, total, handlePageChange, handleSizeChange } = usePagination()

const list = ref<UserManageVO[]>([])
const loading = ref(false)
const keyword = ref('')

const banVisible = ref(false)
const banUserId = ref(0)
const banUserNickname = ref('')

async function fetchData() {
  loading.value = true
  try {
    const data = await adminApi.getUserList({
      pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined,
    }) as PageResult<UserManageVO>
    list.value = data.records
    total.value = data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() {
  pageNum.value = 1
  fetchData()
}

function handleBan(row: UserManageVO) {
  banUserId.value = row.id
  banUserNickname.value = row.nickname
  banVisible.value = true
}

function onBanDone() {
  nextTick(() => fetchData())
}

watch([pageNum, pageSize], () => fetchData())
onMounted(() => fetchData())
</script>

<style scoped>
.manage-page { background: #fff; padding: 20px; border-radius: 4px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.ban-reason { font-size: 12px; color: #f56c6c; margin-top: 4px; }
</style>