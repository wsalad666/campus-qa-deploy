<template>
  <aside class="app-sidebar">
    <div class="sidebar-header">
      <span class="sidebar-title">📚 常用课程</span>
      <el-button
        type="primary"
        link
        :icon="Plus"
        class="add-btn"
        @click="dialogVisible = true"
        title="添加常用课程"
      />
    </div>

    <div v-if="favoriteCourses.length === 0" class="sidebar-empty">
      <p>暂无常用课程</p>
      <el-button type="primary" size="small" @click="dialogVisible = true">
        添加课程
      </el-button>
    </div>

    <el-menu
      v-else
      :default-active="activeCourseId"
      class="sidebar-menu"
      @select="handleSelect"
    >
      <el-menu-item
        v-for="course in favoriteCourses"
        :key="course.id"
        :index="String(course.id)"
      >
        <span class="course-name">{{ course.name }}</span>
        <el-button
          class="remove-btn"
          link
          :icon="Close"
          @click.stop="handleRemove(course.id)"
          title="移除"
        />
      </el-menu-item>
    </el-menu>

    <!-- 全部课程选择弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择常用课程"
      width="500px"
      :append-to-body="true"
      destroy-on-close
    >
      <div class="dialog-body">
        <p class="dialog-hint">勾选你需要的课程，它们将显示在左侧侧边栏中。</p>
        <el-checkbox-group v-model="selectedCourseIds" class="course-checkbox-group">
          <el-checkbox
            v-for="c in allCourses"
            :key="c.id"
            :value="c.id"
            :label="c.id"
            class="course-checkbox-item"
          >
            {{ c.name }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确认</el-button>
      </template>
    </el-dialog>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Plus, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import { userApi } from '@/api/user'
import type { Course } from '@/types'
import { useUserStore } from '@/stores/user'

const emit = defineEmits<{
  (e: 'course-change', courseId: number): void
}>()

const userStore = useUserStore()

const allCourses = ref<Course[]>([])
const favoriteCourses = ref<Course[]>([])
const activeCourseId = ref('')
const dialogVisible = ref(false)
const selectedCourseIds = ref<number[]>([])
const saving = ref(false)

// 默认热门课程名称（用于新用户预设）
const DEFAULT_COURSE_NAMES = ['高等数学', '程序设计', '大学英语']

async function fetchAllCourses() {
  try {
    const res = await adminApi.getCourseList()
    allCourses.value = (Array.isArray(res) ? res : res?.records) || []
  } catch {
    allCourses.value = []
  }
}

async function fetchFavoriteCourses() {
  if (!userStore.isLoggedIn) {
    // 未登录时显示默认课程
    setDefaultCourses()
    return
  }
  try {
    const res: any = await userApi.getFavoriteCourses()
    if (res && res.length > 0) {
      favoriteCourses.value = res
    } else {
      // 新用户，自动设置默认课程
      await setDefaultFavoriteCourses()
    }
  } catch {
    setDefaultCourses()
  }
}

function setDefaultCourses() {
  // 从未登录/异常时的降级：从全部课程中匹配默认课程
  const defaults = allCourses.value.filter(c => DEFAULT_COURSE_NAMES.includes(c.name))
  favoriteCourses.value = defaults.length > 0 ? defaults : allCourses.value.slice(0, 3)
}

async function setDefaultFavoriteCourses() {
  // 新用户自动设置默认常用课程
  const defaults = allCourses.value.filter(c => DEFAULT_COURSE_NAMES.includes(c.name))
  const courseIds = defaults.map(c => c.id)
  if (courseIds.length > 0) {
    try {
      await userApi.updateFavoriteCourses(courseIds)
      favoriteCourses.value = defaults
      return
    } catch { /* ignore */ }
  }
  // 降级：取前3个
  favoriteCourses.value = allCourses.value.slice(0, 3)
}

function openDialog() {
  selectedCourseIds.value = favoriteCourses.value.map(c => c.id)
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (!userStore.isLoggedIn) {
      // 未登录，仅本地更新
      favoriteCourses.value = allCourses.value.filter(c => selectedCourseIds.value.includes(c.id))
      ElMessage.success('已更新常用课程')
      dialogVisible.value = false
      return
    }
    await userApi.updateFavoriteCourses(selectedCourseIds.value)
    favoriteCourses.value = allCourses.value.filter(c => selectedCourseIds.value.includes(c.id))
    ElMessage.success('已更新常用课程')
    dialogVisible.value = false
  } catch {
    ElMessage.error('更新失败，请重试')
  } finally {
    saving.value = false
  }
}

function handleSelect(index: string) {
  activeCourseId.value = index
  emit('course-change', Number(index))
}

async function handleRemove(courseId: number) {
  const newFavorites = favoriteCourses.value.filter(c => c.id !== courseId)
  if (!userStore.isLoggedIn) {
    favoriteCourses.value = newFavorites
    return
  }
  try {
    await userApi.updateFavoriteCourses(newFavorites.map(c => c.id))
    favoriteCourses.value = newFavorites
  } catch {
    ElMessage.error('移除失败')
  }
}

// 监听 dialogVisible 变化，打开时初始化选中项
watch(dialogVisible, (val) => {
  if (val) {
    selectedCourseIds.value = favoriteCourses.value.map(c => c.id)
  }
})

onMounted(async () => {
  await fetchAllCourses()
  await fetchFavoriteCourses()
})
</script>

<style scoped>
.app-sidebar {
  width: 220px;
  min-height: calc(100vh - 64px);
  background: rgba(245, 247, 250, 0.88);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-right: 1px solid rgba(228, 231, 237, 0.6);
  padding: 0;
  flex-shrink: 0;
  position: fixed;
  top: 64px;
  left: 0;
  bottom: 0;
  overflow-y: auto;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 8px;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #909399;
}

.add-btn {
  font-size: 18px;
  padding: 2px;
}

.sidebar-empty {
  text-align: center;
  padding: 24px 16px;
  color: #c0c4cc;
  font-size: 13px;
}

.sidebar-empty p {
  margin: 0 0 12px;
}

.sidebar-menu {
  border-right: none !important;
  background: transparent;
}

.sidebar-menu .el-menu-item {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  border-radius: 8px;
  margin: 2px 8px;
  transition: all 0.2s;
}

.course-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-btn {
  visibility: hidden;
  font-size: 14px;
  padding: 2px;
  color: #c0c4cc;
}

.sidebar-menu .el-menu-item:hover .remove-btn {
  visibility: visible;
}

.remove-btn:hover {
  color: #f56c6c;
}

.dialog-body {
  padding: 0 4px;
}

.dialog-hint {
  font-size: 13px;
  color: #909399;
  margin: 0 0 16px;
}

.course-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-checkbox-item {
  margin-right: 0;
  padding: 8px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  transition: border-color 0.2s;
}

.course-checkbox-item:hover {
  border-color: #409eff;
}

.course-checkbox-item.is-checked {
  border-color: #409eff;
  background: #ecf5ff;
}
</style>