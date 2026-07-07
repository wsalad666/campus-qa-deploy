<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import { userApi } from '@/api/user'
import type { CollectFolder, CollectItem } from '@/types'

const router = useRouter()

const folders = ref<CollectFolder[]>([])
const items = ref<CollectItem[]>([])
const selectedFolderId = ref<number | null>(null)
const loading = ref(false)
const activeTab = ref<'question' | 'resource'>('question')
const newFolderName = ref('')
const renameDialogVisible = ref(false)
const renameFolderId = ref(0)
const renameFolderName = ref('')
const moveDialogVisible = ref(false)
const moveTargetFolderId = ref<number | null>(null)
const movingItem = ref<CollectItem | null>(null)

async function fetchFolders() {
  try {
    const res: any = await userApi.getCollectFolders()
    folders.value = res || []
    if (folders.value.length > 0 && !selectedFolderId.value) {
      selectedFolderId.value = folders.value[0].id
    }
  } catch { /* handled */ }
}

async function fetchItems() {
  if (!selectedFolderId.value) return
  loading.value = true
  try {
    const targetType = activeTab.value === 'question' ? 1 : 2
    const res: any = await userApi.getCollectItems({ folderId: selectedFolderId.value, targetType })
    items.value = res || []
  } catch { /* handled */ }
  finally { loading.value = false }
}

async function createFolder() {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  try {
    await userApi.createCollectFolder(newFolderName.value.trim())
    newFolderName.value = ''
    ElMessage.success('创建成功')
    await fetchFolders()
  } catch { /* handled */ }
}

function openRenameDialog(folder: CollectFolder) {
  renameFolderId.value = folder.id
  renameFolderName.value = folder.folderName
  renameDialogVisible.value = true
}

async function handleRename() {
  if (!renameFolderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  try {
    await userApi.renameCollectFolder(renameFolderId.value, renameFolderName.value.trim())
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
    await fetchFolders()
  } catch { /* handled */ }
}

async function handleDeleteFolder(folder: CollectFolder) {
  try {
    if (folder.count > 0) {
      await ElMessageBox.confirm(
        `文件夹「${folder.folderName}」中有 ${folder.count} 个收藏内容，删除后内容将一并移除。确定删除？`,
        '确认删除',
        { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
      )
    } else {
      await ElMessageBox.confirm(
        `确定删除文件夹「${folder.folderName}」？`,
        '确认删除',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      )
    }
    await userApi.deleteCollectFolder(folder.id)
    ElMessage.success('已删除')
    if (selectedFolderId.value === folder.id) {
      selectedFolderId.value = folders.value.length > 1 ? folders.value[0].id : null
    }
    await fetchFolders()
  } catch {
    // user cancelled or error
  }
}

async function handleRemoveCollect(item: CollectItem) {
  try {
    await userApi.removeCollect(item.relationId)
    ElMessage.success('已取消收藏')
    await fetchItems()
    await fetchFolders()
  } catch { /* handled */ }
}

function openMoveDialog(item: CollectItem) {
  movingItem.value = item
  moveTargetFolderId.value = null
  moveDialogVisible.value = true
}

async function handleMove() {
  if (!movingItem.value || !moveTargetFolderId.value) return
  try {
    await userApi.moveCollect(movingItem.value.relationId, moveTargetFolderId.value)
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    await fetchItems()
    await fetchFolders()
  } catch { /* handled */ }
}

function goToQuestion(id: number) {
  router.push(`/student/qa/${id}`)
}

function goToResource(id: number) {
  router.push(`/student/resource/${id}`)
}

function selectFolder(id: number) {
  selectedFolderId.value = id
}

watch(selectedFolderId, () => { fetchItems() })
watch(activeTab, () => { fetchItems() })

onMounted(() => { fetchFolders() })
</script>

<template>
  <div class="my-collect-container">
    <AppHeader />
    <div class="my-collect-body">
      <!-- Left Sidebar -->
      <aside class="collect-sidebar">
        <div class="sidebar-header">
          <h3>我的收藏夹</h3>
        </div>
        <div class="folder-list">
          <div
            v-for="folder in folders"
            :key="folder.id"
            class="folder-item"
            :class="{ active: selectedFolderId === folder.id }"
            @click="selectFolder(folder.id)"
          >
            <div class="folder-info">
              <span class="folder-name">{{ folder.folderName }}</span>
              <span class="folder-count">{{ folder.count }}</span>
            </div>
            <div class="folder-actions" v-if="folder.folderName !== '默认收藏'">
              <el-button text size="small" @click.stop="openRenameDialog(folder)">重命名</el-button>
              <el-button text size="small" type="danger" @click.stop="handleDeleteFolder(folder)">删除</el-button>
            </div>
          </div>
        </div>
        <div class="new-folder-area">
          <el-input
            v-model="newFolderName"
            placeholder="新收藏夹名称"
            size="small"
            @keyup.enter="createFolder"
          />
          <el-button size="small" type="primary" @click="createFolder">+ 新建</el-button>
        </div>
      </aside>

      <!-- Right Content -->
      <main class="collect-content">
        <el-tabs v-model="activeTab" class="collect-tabs">
          <el-tab-pane label="收藏的提问" name="question" />
          <el-tab-pane label="收藏的学习资源" name="resource" />
        </el-tabs>

        <div v-loading="loading" class="items-list">
          <div v-for="item in items" :key="item.relationId" class="collect-item">
            <div class="item-main">
              <h4
                class="item-title clickable"
                @click="activeTab === 'question' ? goToQuestion(item.targetId) : goToResource(item.targetId)"
              >
                {{ item.title }}
              </h4>
              <p class="item-desc">{{ item.description?.replace(/<[^>]+>/g, '').substring(0, 100) || '' }}</p>
              <div class="item-meta">
                <span>{{ item.courseName }}</span>
                <span>{{ item.userNickname }}</span>
                <span>{{ item.createTime }}</span>
              </div>
            </div>
            <div class="item-actions">
              <el-button text size="small" type="primary" @click="openMoveDialog(item)">移动</el-button>
              <el-button text size="small" type="danger" @click="handleRemoveCollect(item)">取消收藏</el-button>
            </div>
          </div>
          <el-empty v-if="!loading && items.length === 0" description="暂无收藏内容" />
        </div>
      </main>
    </div>

    <!-- Rename Dialog -->
    <el-dialog v-model="renameDialogVisible" title="重命名文件夹" width="380px">
      <el-input v-model="renameFolderName" placeholder="新名称" @keyup.enter="handleRename" />
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRename">确认</el-button>
      </template>
    </el-dialog>

    <!-- Move Dialog -->
    <el-dialog v-model="moveDialogVisible" title="移动到其他文件夹" width="380px">
      <el-select v-model="moveTargetFolderId" placeholder="选择目标文件夹" style="width: 100%">
        <el-option
          v-for="f in folders.filter(f => f.id !== selectedFolderId)"
          :key="f.id"
          :label="f.folderName"
          :value="f.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="moveDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!moveTargetFolderId" @click="handleMove">确认移动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.my-collect-container {
  min-height: 100vh;
  background: #f5f7fa;
}
.my-collect-body {
  display: flex;
  padding-top: 60px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
}
.collect-sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid #ebeef5;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 0 16px 12px;
  border-bottom: 1px solid #ebeef5;
}
.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}
.folder-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.folder-item {
  padding: 10px 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.2s;
}
.folder-item:hover {
  background: #f5f7fa;
}
.folder-item.active {
  background: #ecf5ff;
  color: #409eff;
}
.folder-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.folder-name {
  font-size: 14px;
}
.folder-count {
  font-size: 12px;
  color: #909399;
  background: #f0f0f0;
  border-radius: 10px;
  padding: 1px 8px;
}
.folder-actions {
  display: none;
  gap: 4px;
}
.folder-item:hover .folder-actions {
  display: flex;
}
.new-folder-area {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 8px;
}
.collect-content {
  flex: 1;
  padding: 20px 24px;
  background: #fff;
}
.collect-tabs {
  margin-bottom: 16px;
}
.items-list {
  min-height: 200px;
}
.collect-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.collect-item:last-child {
  border-bottom: none;
}
.item-main {
  flex: 1;
  min-width: 0;
}
.item-title {
  margin: 0 0 6px;
  font-size: 15px;
  color: #303133;
}
.item-title.clickable {
  cursor: pointer;
  color: #409eff;
}
.item-title.clickable:hover {
  text-decoration: underline;
}
.item-desc {
  margin: 0 0 6px;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}
.item-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #c0c4cc;
}
.item-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 16px;
}
</style>