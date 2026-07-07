<template>
  <AdminLayout title="管理员后台">
    <div class="dashboard">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="card in cards" :key="card.label">
          <el-card shadow="hover" class="stat-card" :class="{ clickable: card.route }" @click="goTo(card.route)">
            <div class="stat-card-content">
              <div class="stat-card-left">
                <div class="stat-card-label">{{ card.label }}</div>
                <div class="stat-card-value">{{ card.value }}</div>
              </div>
              <div class="stat-card-icon" :style="{ backgroundColor: card.bgColor }">
                <el-icon :size="28">
                  <component :is="card.icon" />
                </el-icon>
              </div>
            </div>
            <div v-if="card.route" class="stat-card-hint">点击查看详情 →</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, QuestionFilled, Edit, Folder, Download } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import type { Statistics } from '@/types'
import AdminLayout from './AdminLayout.vue'

const router = useRouter()

const statistics = ref<Statistics>({
  totalUsers: 0,
  todayNewUsers: 0,
  totalQuestions: 0,
  todayQuestions: 0,
  totalResources: 0,
  totalDownloads: 0,
})

function goTo(route?: string) {
  if (route) router.push(route)
}

const cards = computed(() => [
  {
    label: '总注册学生数',
    value: statistics.value.totalUsers,
    icon: User,
    bgColor: '#409EFF',
    route: '/admin/users',
  },
  {
    label: '今日新增用户',
    value: statistics.value.todayNewUsers,
    icon: UserFilled,
    bgColor: '#67C23A',
    route: '/admin/users',
  },
  {
    label: '总提问数量',
    value: statistics.value.totalQuestions,
    icon: QuestionFilled,
    bgColor: '#E6A23C',
    route: '/admin/questions',
  },
  {
    label: '今日提问数',
    value: statistics.value.todayQuestions,
    icon: Edit,
    bgColor: '#A855F7',
    route: '/admin/questions',
  },
  {
    label: '总上传资源数量',
    value: statistics.value.totalResources,
    icon: Folder,
    bgColor: '#06B6D4',
    route: '/admin/resources',
  },
  {
    label: '总下载次数',
    value: statistics.value.totalDownloads,
    icon: Download,
    bgColor: '#F56C6C',
    route: '/admin/resources',
  },
])

onMounted(async () => {
  try {
    const data = await adminApi.getStatistics() as Statistics
    statistics.value = data
  } catch {
    // error handled by interceptor
  }
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.stat-card.clickable {
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card.clickable:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

.stat-card-hint {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  opacity: 0;
  transition: opacity 0.2s;
}

.stat-card.clickable:hover .stat-card-hint {
  opacity: 1;
  color: #409EFF;
}

.stat-card-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-card-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
</style>