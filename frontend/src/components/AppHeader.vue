<template>
  <header class="app-header">
    <div class="header-left">
      <router-link to="/student/home" class="logo">
        <span class="logo-icon">🎓</span>
        <span class="logo-text">校园互助答疑</span>
      </router-link>
    </div>

    <el-menu
      :default-active="activeMenu"
      mode="horizontal"
      :ellipsis="false"
      router
      class="header-nav"
    >
      <el-menu-item index="/student/home">🏠 首页</el-menu-item>
      <el-menu-item index="/student/qa">💬 问答广场</el-menu-item>
      <el-menu-item index="/student/resource">📁 资源中心</el-menu-item>
      <el-menu-item index="/student/collect">⭐ 我的收藏</el-menu-item>
      <el-menu-item index="/student/profile">👤 个人中心</el-menu-item>
    </el-menu>

    <!-- 全局搜索框 -->
    <div class="header-search">
      <el-autocomplete
        v-model="searchKeyword"
        :fetch-suggestions="searchSuggestions"
        :trigger-on-focus="false"
        placeholder="搜索用户、问题或资料..."
        :popper-class="'search-popper'"
        clearable
        @select="handleSearchSelect"
        @keyup.enter="handleSearchEnter"
      >
        <template #default="{ item }">
          <div class="search-item">
            <el-icon v-if="item.type === 'user'" class="search-icon"><User /></el-icon>
            <el-icon v-else-if="item.type === 'resource'" class="search-icon"><Document /></el-icon>
            <el-icon v-else class="search-icon"><Search /></el-icon>
            <span class="search-label">{{ item.label }}</span>
            <el-tag v-if="item.type === 'user'" size="small" type="info">用户</el-tag>
            <el-tag v-else-if="item.type === 'resource'" size="small" type="warning">资料</el-tag>
            <el-tag v-else size="small" type="primary">问题</el-tag>
          </div>
        </template>
      </el-autocomplete>
    </div>

    <div v-if="userStore.isLoggedIn" class="header-notification" @click="notificationVisible = true">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
        <el-icon :size="22"><Bell /></el-icon>
      </el-badge>
    </div>

    <div class="header-right">
      <template v-if="userStore.isLoggedIn && userStore.userInfo">
        <el-dropdown trigger="click">
          <div class="user-info">
            <UserAvatar :src="userStore.userInfo.avatar" :size="36" :nickname="userStore.userInfo.nickname" />
            <span class="nickname">{{ userStore.userInfo.nickname }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/student/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item @click="router.push('/student/collect')">我的收藏</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <el-button v-else type="primary" size="small" @click="router.push('/student/login')">
        登录
      </el-button>
    </div>
    <NotificationDrawer v-model="notificationVisible" @refresh="fetchUnreadCount" />
  </header>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { qaApi } from '@/api/qa'
import { resourceApi } from '@/api/resource'
import { Bell } from '@element-plus/icons-vue'
import UserAvatar from '@/components/UserAvatar.vue'
import NotificationDrawer from '@/components/NotificationDrawer.vue'
import type { User, Question, Resource } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/student/qa')) return '/student/qa'
  if (path.startsWith('/student/resource')) return '/student/resource'
  if (path.startsWith('/student/collect')) return '/student/collect'
  if (path.startsWith('/student/profile')) return '/student/profile'
  return '/student/home'
})

const searchKeyword = ref('')
const unreadCount = ref(0)
const notificationVisible = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null

interface SearchSuggestion {
  type: 'user' | 'question' | 'resource'
  label: string
  value: string
  id: number
}

async function searchSuggestions(queryString: string, cb: (results: SearchSuggestion[]) => void) {
  if (!queryString || queryString.trim().length === 0) {
    cb([])
    return
  }
  const keyword = queryString.trim()
  try {
    const [userRes, qaRes, resourceRes] = await Promise.all([
      userApi.searchUsers(keyword).catch(() => []),
      qaApi.getQuestionList({ pageNum: 1, pageSize: 5, keyword }).catch(() => ({ records: [] })),
      resourceApi.getList({ pageNum: 1, pageSize: 5, keyword }).catch(() => ({ records: [] })),
    ])
    const users: User[] = Array.isArray(userRes) ? userRes : []
    const questions: Question[] = (qaRes as any)?.records || []
    const resources: Resource[] = (resourceRes as any)?.records || []

    const suggestions: SearchSuggestion[] = [
      ...users.map(u => ({
        type: 'user' as const,
        label: u.nickname || u.username,
        value: u.nickname || u.username,
        id: u.id,
      })),
      ...questions.map(q => ({
        type: 'question' as const,
        label: q.title,
        value: q.title,
        id: q.id,
      })),
      ...resources.map(r => ({
        type: 'resource' as const,
        label: r.title,
        value: r.title,
        id: r.id,
      })),
    ]
    cb(suggestions)
  } catch {
    cb([])
  }
}

function handleSearchSelect(item: SearchSuggestion) {
  searchKeyword.value = ''
  if (item.type === 'user') {
    router.push(`/student/user/${item.id}`)
  } else if (item.type === 'resource') {
    router.push(`/student/resource?keyword=${encodeURIComponent(item.label)}`)
  } else {
    router.push(`/student/qa/${item.id}`)
  }
}

function handleSearchEnter() {
  if (!searchKeyword.value.trim()) return
  router.push(`/student/qa?keyword=${encodeURIComponent(searchKeyword.value.trim())}`)
  searchKeyword.value = ''
}

function handleLogout() {
  userStore.logout()
  router.push('/student/home')
}

async function fetchUnreadCount() {
  if (!userStore.isLoggedIn) return
  try {
    const res: any = await qaApi.getUnreadCount()
    unreadCount.value = res ?? 0
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
})
onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 64px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.header-left {
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  margin-right: 24px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #4f8ef7, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-nav {
  flex: 0 0 auto;
  border-bottom: none !important;
}

.header-nav .el-menu-item {
  height: 64px;
  line-height: 64px;
}

.header-search {
  flex: 1;
  margin: 0 24px;
  max-width: 400px;
}

.header-right {
  flex-shrink: 0;
  margin-left: 16px;
}

.header-notification {
  margin-right: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 6px;
  border-radius: 8px;
  transition: background 0.2s;
}
.header-notification .el-icon {
  font-size: 22px;
  color: #606266;
}
.header-notification:hover {
  background: rgba(79, 142, 247, 0.08);
}
.header-notification:hover .el-icon {
  color: #4f8ef7;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: rgba(79, 142, 247, 0.08);
}

.user-info .nickname {
  font-size: 14px;
  color: #333;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-icon {
  color: #909399;
  flex-shrink: 0;
}

.search-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

<style>
/* 全局样式，不加 scoped */
.search-popper .el-autocomplete-suggestion__list {
  max-height: 300px;
}
</style>