<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">校园问答 · 管理后台</div>
      <el-menu
        router
        :default-active="route.path"
        class="admin-menu"
        background-color="#3b82b8"
        text-color="#e8f0f8"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/admin/reports">
          <el-icon><WarningFilled /></el-icon>
          <el-badge :value="pendingReportCount" :hidden="pendingReportCount === 0" :max="99" class="report-badge">
            <span>举报工单</span>
          </el-badge>
        </el-menu-item>
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/admin/courses">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/questions">
          <el-icon><QuestionFilled /></el-icon>
          <span>提问管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/answers">
          <el-icon><ChatLineSquare /></el-icon>
          <span>回答管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/resources">
          <el-icon><FolderOpened /></el-icon>
          <span>资源管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户列表</span>
        </el-menu-item>
        <el-menu-item index="/admin/ban-logs">
          <el-icon><Document /></el-icon>
          <span>处罚日志</span>
        </el-menu-item>
        <el-menu-item @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="admin-header-left">
          <span class="admin-header-title">{{ title }}</span>
        </div>
        <div class="admin-header-right">
          <el-avatar :size="32" :src="resolveAvatarUrl(adminStore.adminInfo?.avatar)">
            {{ adminStore.adminInfo?.nickname?.charAt(0) || '管' }}
          </el-avatar>
          <span class="admin-nickname" @click="goToProfile" style="cursor: pointer">{{ adminStore.adminInfo?.nickname || '管理员' }}</span>
        </div>
      </el-header>
      <el-main class="admin-main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { adminApi } from '@/api/admin'
import { ElMessageBox } from 'element-plus'
import {
  DataAnalysis, Reading, QuestionFilled, FolderOpened, ChatLineSquare,
  User, WarningFilled, Document, SwitchButton, UserFilled
} from '@element-plus/icons-vue'

defineProps<{ title: string }>()

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const pendingReportCount = ref(0)
let pollTimer: ReturnType<typeof setInterval> | null = null

async function fetchPendingCount() {
  try {
    const res: any = await adminApi.getPendingReportCount()
    pendingReportCount.value = res ?? 0
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchPendingCount()
  pollTimer = setInterval(fetchPendingCount, 30000)
})
onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    adminStore.logout()
    router.push('/admin/login')
  }).catch(() => {})
}

function goToProfile() {
  const userId = localStorage.getItem('adminUserId')
  if (userId) {
    router.push(`/student/user/${userId}`)
  }
}

function resolveAvatarUrl(avatar?: string): string {
  if (!avatar) return ''
  if (avatar.startsWith('http://') || avatar.startsWith('https://') || avatar.startsWith('data:')) return avatar
  return '' + (avatar.startsWith('/') ? '' : '/') + avatar
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background-color: #3b82b8;
  overflow-y: auto;
}

.admin-logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background-color: #2d6a9e;
  white-space: nowrap;
}

.admin-menu {
  border-right: none;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.admin-header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.admin-header-right {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

.admin-nickname {
  margin-left: 4px;
}

.admin-main {
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.report-badge {
  margin-left: 4px;
}
</style>