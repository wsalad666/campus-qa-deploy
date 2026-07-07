<template>
  <AdminLayout title="课程分类管理">
    <div class="course-manage">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程名称..."
            clearable
            style="width: 260px"
            @keyup.enter="fetchCourses"
            @clear="fetchCourses"
          >
            <template #append>
              <el-button @click="fetchCourses">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增课程</el-button>
      </div>

      <el-table :data="courseList" border stripe style="width: 100%">
        <template #empty>
          <el-empty description="暂无课程数据" />
        </template>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑课程' : '新增课程'"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="课程名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入课程名称" />
          </el-form-item>
          <el-form-item label="课程描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入课程描述"
            />
          </el-form-item>
          <el-form-item label="图标" prop="icon">
            <el-input v-model="form.icon" placeholder="请输入图标标识" />
          </el-form-item>
          <el-form-item label="父级课程" prop="parentId">
            <el-input-number v-model="form.parentId" :min="0" placeholder="0 表示顶级课程" />
          </el-form-item>
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="form.sortOrder" :min="0" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import type { Course } from '@/types'
import AdminLayout from './AdminLayout.vue'

const courseList = ref<Course[]>([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  description: '',
  icon: '',
  parentId: 0,
  sortOrder: 0,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: ['blur', 'change'] }],
}

function resetForm() {
  form.name = ''
  form.description = ''
  form.icon = ''
  form.parentId = 0
  form.sortOrder = 0
  editingId.value = null
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Course) {
  isEdit.value = true
  editingId.value = row.id
  form.name = row.name
  form.description = row.description || ''
  form.icon = row.icon || ''
  form.parentId = row.parentId || 0
  form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editingId.value) {
      await adminApi.updateCourse(editingId.value, { ...form })
      ElMessage.success('课程更新成功')
    } else {
      await adminApi.addCourse({ ...form })
      ElMessage.success('课程添加成功')
    }
    dialogVisible.value = false
    await fetchCourses()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

function handleDelete(row: Course) {
  ElMessageBox.confirm(`确定要删除课程「${row.name}」吗？删除后不可恢复。`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await adminApi.deleteCourse(row.id)
      ElMessage.success('删除成功')
      await fetchCourses()
    } catch {
      // error handled by interceptor
    }
  }).catch(() => {})
}

async function fetchCourses() {
  try {
    const keyword = searchKeyword.value.trim() || undefined
    const data = await adminApi.getCourseList(keyword) as Course[]
    courseList.value = data
  } catch {
    // error handled by interceptor
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.course-manage {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>