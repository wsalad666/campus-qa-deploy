<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import QuestionCard from '@/components/QuestionCard.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import { userApi } from '@/api/user'
import { qaApi } from '@/api/qa'
import { resourceApi } from '@/api/resource'
import { useUserStore } from '@/stores/user'
import type { QuestionVO, Resource } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userId = Number(route.params.userId)

const profile = ref<any>(null)
const loading = ref(false)
const followLoading = ref(false)
const activeTab = ref<'questions' | 'resources'>('questions')
const questions = ref<QuestionVO[]>([])
const resources = ref<Resource[]>([])
const contentLoading = ref(false)

const isSelf = () => userStore.userInfo?.id === userId

async function fetchProfile() {
  loading.value = true
  try {
    const res: any = await userApi.getProfile(userId)
    profile.value = res
  } catch {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

async function fetchContent() {
  contentLoading.value = true
  try {
    if (activeTab.value === 'questions') {
      const res: any = await qaApi.getQuestionList({ userId, pageNum: 1, pageSize: 20 })
      questions.value = res.records || []
    } else {
      const res: any = await resourceApi.getList({ userId, pageNum: 1, pageSize: 20 })
      resources.value = res.records || []
    }
  } catch {
    // handled
  } finally {
    contentLoading.value = false
  }
}

async function toggleFollow() {
  if (!profile.value) return
  followLoading.value = true
  try {
    await userApi.toggleFollow({ followedId: userId })
    profile.value.isFollowed = !profile.value.isFollowed
    profile.value.fansCount += profile.value.isFollowed ? 1 : -1
  } catch {
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

function goBack() {
  router.back()
}

function resolveAvatar(avatar: string | null): string {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return 'http://localhost:8080' + (avatar.startsWith('/') ? '' : '/') + avatar
}

onMounted(() => {
  fetchProfile()
  fetchContent()
})
</script>

<template>
  <div class="user-profile-container">
    <AppHeader />
    <main class="user-profile-main" v-loading="loading">
      <template v-if="profile">
        <div class="profile-header">
          <el-button text @click="goBack" class="back-btn">
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>

          <div class="profile-card">
            <div class="avatar-section">
              <el-avatar :size="80" :src="resolveAvatar(profile.user?.avatar)">
                {{ profile.user?.nickname?.charAt(0) }}
              </el-avatar>
            </div>
            <div class="info-section">
              <h2 class="nickname">{{ profile.user?.nickname }}</h2>
              <p class="signature">{{ profile.user?.signature || '这个人很懒，什么都没写' }}</p>
              <div class="stats">
                <span class="stat-item">
                  <strong>{{ profile.fansCount || 0 }}</strong> 粉丝
                </span>
                <span class="stat-item">
                  <strong>{{ profile.questionCount || 0 }}</strong> 提问
                </span>
                <span class="stat-item">
                  <strong>{{ profile.resourceCount || 0 }}</strong> 资源
                </span>
              </div>
              <div class="actions" v-if="!isSelf()">
                <el-button
                  :type="profile.isFollowed ? 'default' : 'primary'"
                  :loading="followLoading"
                  @click="toggleFollow"
                >
                  {{ profile.isFollowed ? '已关注' : '+ 关注' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="profile-content">
          <el-tabs v-model="activeTab" @tab-change="fetchContent">
            <el-tab-pane label="TA的提问" name="questions" />
            <el-tab-pane label="TA的资源" name="resources" />
          </el-tabs>

          <div v-loading="contentLoading" class="content-list">
            <template v-if="activeTab === 'questions'">
              <QuestionCard
                v-for="q in questions"
                :key="q.id"
                :question="q"
                @click="router.push(`/student/qa/${q.id}`)"
              />
              <el-empty v-if="!contentLoading && questions.length === 0" description="暂无提问" />
            </template>
            <template v-else>
              <div class="resource-grid">
                <ResourceCard
                  v-for="r in resources"
                  :key="r.id"
                  :resource="r"
                  @click="() => {}"
                />
              </div>
              <el-empty v-if="!contentLoading && resources.length === 0" description="暂无资源" />
            </template>
          </div>
        </div>
      </template>

      <el-empty v-else-if="!loading" description="用户不存在" />
    </main>
  </div>
</template>

<style scoped>
.user-profile-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.user-profile-main {
  max-width: 860px;
  margin: 0 auto;
  padding: 80px 24px 40px;
}

.back-btn {
  margin-bottom: 16px;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  display: flex;
  gap: 24px;
  align-items: center;
}

.avatar-section {
  flex-shrink: 0;
}

.info-section {
  flex: 1;
}

.nickname {
  margin: 0 0 8px;
  font-size: 22px;
  color: #303133;
}

.signature {
  margin: 0 0 16px;
  font-size: 14px;
  color: #909399;
}

.stats {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.stat-item {
  font-size: 14px;
  color: #606266;
}

.stat-item strong {
  color: #303133;
  margin-right: 4px;
}

.actions {
  display: flex;
  gap: 12px;
}

.profile-content {
  margin-top: 24px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  min-height: 200px;
}

.content-list {
  min-height: 100px;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
</style>