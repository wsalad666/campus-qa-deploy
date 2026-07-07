<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import QuestionCard from '@/components/QuestionCard.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import { qaApi } from '@/api/qa'
import { resourceApi } from '@/api/resource'
import { userApi } from '@/api/user'
import type { Question, Resource, User } from '@/types'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const questions = ref<Question[]>([])
const resources = ref<Resource[]>([])
const users = ref<User[]>([])
const loading = ref(false)
const activeTab = ref<'all' | 'question' | 'resource' | 'user'>('all')

async function doSearch() {
  const kw = (route.query.keyword as string) || ''
  keyword.value = kw
  if (!kw.trim()) return
  loading.value = true
  try {
    const [qaRes, resourceRes, userRes] = await Promise.all([
      qaApi.getQuestionList({ pageNum: 1, pageSize: 10, keyword: kw }).catch(() => ({ records: [] })),
      resourceApi.getList({ pageNum: 1, pageSize: 10, keyword: kw }).catch(() => ({ records: [] })),
      userApi.searchUsers(kw).catch(() => []),
    ])
    questions.value = (qaRes as any)?.records || []
    resources.value = (resourceRes as any)?.records || []
    users.value = Array.isArray(userRes) ? userRes : []
  } finally {
    loading.value = false
  }
}

function goToQuestion(id: number) { router.push(`/student/qa/${id}`) }
function goToResource(id: number) { router.push(`/student/resource/${id}`) }
function goToUser(id: number) { router.push(`/student/user/${id}`) }

watch(() => route.query.keyword, () => { doSearch() })
onMounted(() => { doSearch() })
</script>

<template>
  <div class="search-page">
    <AppHeader />
    <div class="search-body">
      <div class="search-header">
        <h2>搜索结果：{{ keyword }}</h2>
        <el-tabs v-model="activeTab" class="search-tabs">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane :label="`问题 (${questions.length})`" name="question" />
          <el-tab-pane :label="`资源 (${resources.length})`" name="resource" />
          <el-tab-pane :label="`用户 (${users.length})`" name="user" />
        </el-tabs>
      </div>

      <div v-loading="loading" class="search-results">
        <template v-if="activeTab === 'all' || activeTab === 'question'">
          <section v-if="questions.length > 0" class="result-section">
            <h3 class="section-title">问题 ({{ questions.length }})</h3>
            <QuestionCard
              v-for="q in (activeTab === 'all' ? questions.slice(0, 5) : questions)"
              :key="q.id"
              :question="q"
            />
            <el-button v-if="activeTab === 'all' && questions.length > 5" text type="primary" @click="activeTab = 'question'">
              查看全部 {{ questions.length }} 个问题
            </el-button>
          </section>
        </template>

        <template v-if="activeTab === 'all' || activeTab === 'resource'">
          <section v-if="resources.length > 0" class="result-section">
            <h3 class="section-title">资源 ({{ resources.length }})</h3>
            <el-row :gutter="16">
              <el-col v-for="r in (activeTab === 'all' ? resources.slice(0, 4) : resources)" :key="r.id" :xs="24" :sm="12" :md="8">
                <ResourceCard :resource="r" layout="list" @click="goToResource(r.id)" />
              </el-col>
            </el-row>
            <el-button v-if="activeTab === 'all' && resources.length > 4" text type="primary" @click="activeTab = 'resource'">
              查看全部 {{ resources.length }} 个资源
            </el-button>
          </section>
        </template>

        <template v-if="activeTab === 'all' || activeTab === 'user'">
          <section v-if="users.length > 0" class="result-section">
            <h3 class="section-title">用户 ({{ users.length }})</h3>
            <div class="user-list">
              <div v-for="u in (activeTab === 'all' ? users.slice(0, 6) : users)" :key="u.id" class="user-item" @click="goToUser(u.id)">
                <el-avatar :size="40">{{ (u.nickname || u.username)?.charAt(0) }}</el-avatar>
                <span class="user-name">{{ u.nickname || u.username }}</span>
              </div>
            </div>
            <el-button v-if="activeTab === 'all' && users.length > 6" text type="primary" @click="activeTab = 'user'">
              查看全部 {{ users.length }} 个用户
            </el-button>
          </section>
        </template>

        <el-empty v-if="!loading && questions.length === 0 && resources.length === 0 && users.length === 0" description="未找到相关内容" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.search-body {
  max-width: 900px;
  margin: 0 auto;
  padding: 80px 24px 24px;
}
.search-header h2 {
  font-size: 20px;
  margin: 0 0 16px;
  color: #303133;
}
.search-tabs {
  margin-bottom: 20px;
}
.result-section {
  margin-bottom: 28px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}
.user-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  min-width: 160px;
}
.user-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.user-name {
  font-size: 14px;
  color: #303133;
}
</style>
