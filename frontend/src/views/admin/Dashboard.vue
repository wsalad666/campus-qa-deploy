<template>
  <AdminLayout title="管理员后台">
    <div class="dashboard">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" v-for="card in cards" :key="card.label">
          <el-card shadow="hover" class="stat-card">
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
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { User, UserFilled, QuestionFilled, Edit, Folder, Download } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import type { Statistics } from '@/types'
import AdminLayout from './AdminLayout.vue'

const statistics = ref<Statistics>({
  totalUsers: 0,
  todayNewUsers: 0,
  totalQuestions: 0,
  todayQuestions: 0,
  totalResources: 0,
  totalDownloads: 0,
})

const cards = computed(() => [
  {
    label: '总注册学生数',
    value: statistics.value.totalUsers,
    icon: User,
    bgColor: '#409EFF',
  },
  {
    label: '今日新增用户',
    value: statistics.value.todayNewUsers,
    icon: UserFilled,
    bgColor: '#67C23A',
  },
  {
    label: '总提问数量',
    value: statistics.value.totalQuestions,
    icon: QuestionFilled,
    bgColor: '#E6A23C',
  },
  {
    label: '今日提问数',
    value: statistics.value.todayQuestions,
    icon: Edit,
    bgColor: '#A855F7',
  },
  {
    label: '总上传资源数量',
    value: statistics.value.totalResources,
    icon: Folder,
    bgColor: '#06B6D4',
  },
  {
    label: '总下载次数',
    value: statistics.value.totalDownloads,
    icon: Download,
    bgColor: '#F56C6C',
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