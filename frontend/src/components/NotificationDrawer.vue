<template>
  <el-drawer
    :model-value="modelValue"
    direction="rtl"
    size="420px"
    title="消息中心"
    :append-to-body="true"
    :z-index="3000"
    @close="emit('update:modelValue', false)"
  >
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="互动提醒" name="interaction" />
      <el-tab-pane label="系统通知" name="system" />
    </el-tabs>

    <div class="notification-list" v-loading="loading">
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleItemClick(item)"
      >
        <span v-if="item.isRead === 0" class="unread-dot" />
        <el-icon class="type-icon" :size="20">
          <component :is="iconMap[item.type] || Bell" />
        </el-icon>
        <div class="item-body">
          <div class="item-header">
            <span class="item-title">
              <el-tag
                size="small"
                :type="getTypeTagType(item.type)"
                class="type-tag"
              >{{ getTypeLabel(item.type) }}</el-tag>
              {{ item.title }}
            </span>
            <span class="item-time">{{ formatTime(item.createTime) }}</span>
          </div>
          <p class="item-content">{{ item.content }}</p>
        </div>
        <el-icon
          v-if="item.isDeletable === 1"
          class="delete-btn"
          @click.stop="handleDelete(item)"
        >
          <Close />
        </el-icon>
      </div>

      <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" />
    </div>

    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        small
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        v-model:current-page="pageNum"
        @current-change="fetchNotifications"
      />
    </div>

    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleBatchRead">
          <el-icon><CircleCheck /></el-icon>
          全部已读
        </el-button>
        <el-button type="primary" @click="emit('update:modelValue', false)">关闭</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { formatDateTime } from '@/utils/time'
import { ref, computed, watch } from 'vue'
import { formatDateTime } from '@/utils/time'
import { useRouter } from 'vue-router'
import { formatDateTime } from '@/utils/time'
import { qaApi } from '@/api/qa'
import { formatDateTime } from '@/utils/time'
import type { NotificationVO } from '@/types'
import {
  Bell,
  ChatDotRound,
  Trophy,
  User,
  WarningFilled,
  Lock,
  CircleCheck,
  Close,
  Star,
} from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'refresh'): void
}>()

const router = useRouter()

const activeTab = ref('interaction')
const loading = ref(false)

const interactionList = ref<NotificationVO[]>([])
const interactionPageNum = ref(1)
const interactionTotal = ref(0)

const systemList = ref<NotificationVO[]>([])
const systemPageNum = ref(1)
const systemTotal = ref(0)

const pageSize = 10

const notifications = computed(() =>
  activeTab.value === 'interaction' ? interactionList.value : systemList.value
)

const pageNum = computed({
  get: () =>
    activeTab.value === 'interaction' ? interactionPageNum.value : systemPageNum.value,
  set: (val: number) => {
    if (activeTab.value === 'interaction') {
      interactionPageNum.value = val
    } else {
      systemPageNum.value = val
    }
  },
})

const total = computed(() =>
  activeTab.value === 'interaction' ? interactionTotal.value : systemTotal.value
)

const iconMap: Record<number, any> = {
  0: Star,
  1: CircleCheck,
  2: ChatDotRound,
  3: Trophy,
  4: User,
  5: WarningFilled,
  6: Lock,
  7: Bell,
}

const typeLabelMap: Record<number, string> = {
  0: '点赞提问',
  1: '点赞回答',
  2: '评论/回复',
  3: '采纳回答',
  4: '关注',
  5: '内容下架',
  6: '禁言通知',
  7: '举报反馈',
}

function getTypeLabel(type: number): string {
  return typeLabelMap[type] || '通知'
}

function getTypeTagType(type: number): 'success' | 'warning' | 'info' | 'danger' | '' {
  // 互动提醒 (0-4) 用暖色系；系统通知 (5-7) 用冷色系
  const map: Record<number, 'success' | 'warning' | 'info' | 'danger' | ''> = {
    0: 'warning',
    1: 'warning',
    2: '',
    3: 'success',
    4: '',
    5: 'danger',
    6: 'danger',
    7: 'info',
  }
  return map[type] || ''
}

async function fetchNotifications() {
  loading.value = true
  try {
    const type = activeTab.value === 'interaction' ? 0 : 1
    const res = await qaApi.getNotifications({
      pageNum: pageNum.value,
      pageSize,
      type,
    })
    const list = (res as any).records || []
    const totalCount = (res as any).total || 0
    if (activeTab.value === 'interaction') {
      interactionList.value = list
      interactionTotal.value = totalCount
    } else {
      systemList.value = list
      systemTotal.value = totalCount
    }
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  if (activeTab.value === 'interaction') {
    interactionPageNum.value = 1
  } else {
    systemPageNum.value = 1
  }
  fetchNotifications()
}

async function handleItemClick(item: NotificationVO) {
  if (item.isRead === 0) {
    try {
      await qaApi.markRead(item.id)
      item.isRead = 1
      emit('refresh')
    } catch {
      /* ignore */
    }
  }

  if (item.linkType != null && item.linkId != null) {
    if (item.linkType === 1) {
      await router.push(`/student/qa/${item.linkId}`)
    } else if (item.linkType === 2) {
      await router.push({ path: `/student/qa/${item.linkId}`, hash: '#answers' })
    } else if (item.linkType === 3) {
      await router.push(`/student/user/${item.linkId}`)
    }
  }

  emit('update:modelValue', false)
}

async function handleDelete(item: NotificationVO) {
  try {
    await qaApi.deleteNotification(item.id)
    if (activeTab.value === 'interaction') {
      interactionList.value = interactionList.value.filter((n) => n.id !== item.id)
      interactionTotal.value--
    } else {
      systemList.value = systemList.value.filter((n) => n.id !== item.id)
      systemTotal.value--
    }
    emit('refresh')
  } catch {
    /* ignore */
  }
}

async function handleBatchRead() {
  try {
    await qaApi.batchMarkRead()
    interactionList.value.forEach((n) => {
      n.isRead = 1
    })
    systemList.value.forEach((n) => {
      n.isRead = 1
    })
    emit('refresh')
  } catch {
    /* ignore */
  }
}

function formatTime(time: string): string {
  if (!time) return ''
  const d = new Date(time)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hour}:${min}`
}

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      fetchNotifications()
    }
  }
)
</script>

<style scoped>
.notification-list {
  min-height: 300px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: #f0f5ff;
}

.notification-item.unread:hover {
  background: #e6efff;
}

.unread-dot {
  position: absolute;
  left: 0;
  top: 18px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
}

.type-icon {
  flex-shrink: 0;
  margin-right: 12px;
  margin-top: 2px;
  color: #909399;
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
  overflow: hidden;
}

.type-tag {
  flex-shrink: 0;
}

.item-time {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.item-content {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.delete-btn {
  flex-shrink: 0;
  margin-left: 8px;
  margin-top: 2px;
  color: #c0c4cc;
  cursor: pointer;
  transition: color 0.2s;
}

.delete-btn:hover {
  color: #f56c6c;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px 0 0;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>