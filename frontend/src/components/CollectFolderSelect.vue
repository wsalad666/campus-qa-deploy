<template>
  <el-dialog
    v-model="visible"
    title="选择收藏夹"
    width="420px"
    destroy-on-close
    :close-on-click-modal="false"
  >
    <div class="collect-folder-list" v-loading="loading">
      <el-checkbox-group v-model="checkedFolders">
        <div
          v-for="folder in folders"
          :key="folder.id"
          class="folder-item"
        >
          <el-checkbox :value="folder.id">
            <span class="folder-name">{{ folder.folderName }}</span>
            <span class="folder-count">({{ folder.count }})</span>
          </el-checkbox>
        </div>
      </el-checkbox-group>
    </div>

    <div class="new-folder-area" v-if="!showNewFolderInput">
      <el-button text type="primary" @click="showNewFolderInput = true">
        + 新建收藏夹
      </el-button>
    </div>
    <div class="new-folder-area" v-else>
      <el-input
        v-model="newFolderName"
        placeholder="输入文件夹名称"
        size="small"
        style="width: 200px"
        @keyup.enter="createFolder"
      />
      <el-button size="small" type="primary" :loading="createLoading" @click="createFolder">确定</el-button>
      <el-button size="small" @click="showNewFolderInput = false; newFolderName = ''">取消</el-button>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定收藏</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import type { CollectFolder } from '@/types'

const props = defineProps<{
  modelValue: boolean
  targetType: number
  targetId: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'done'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const loading = ref(false)
const folders = ref<CollectFolder[]>([])
const checkedFolders = ref<number[]>([])
const submitLoading = ref(false)
const showNewFolderInput = ref(false)
const newFolderName = ref('')
const createLoading = ref(false)

async function fetchFolders() {
  loading.value = true
  try {
    const res: any = await userApi.getCollectFolders()
    folders.value = res || []
    // 默认勾选"默认收藏"文件夹
    const defaultFolder = folders.value.find(f => f.folderName === '默认收藏')
    if (defaultFolder) {
      checkedFolders.value = [defaultFolder.id]
    }
  } catch {
    ElMessage.error('加载收藏夹失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    await userApi.addCollect({
      folderIds: checkedFolders.value,
      targetType: props.targetType,
      targetId: props.targetId,
    })
    ElMessage.success('收藏成功')
    emit('done')
    visible.value = false
  } catch {
    ElMessage.error('收藏失败')
  } finally {
    submitLoading.value = false
  }
}

async function createFolder() {
  const name = newFolderName.value.trim()
  if (!name) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  createLoading.value = true
  try {
    const newFolder: any = await userApi.createCollectFolder(name)
    showNewFolderInput.value = false
    newFolderName.value = ''
    await fetchFolders()
    // 将新创建的文件夹加入勾选
    if (newFolder?.id) {
      checkedFolders.value.push(newFolder.id)
    }
    ElMessage.success('创建成功')
  } catch {
    ElMessage.error('创建失败')
  } finally {
    createLoading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    showNewFolderInput.value = false
    newFolderName.value = ''
    fetchFolders()
  }
})
</script>

<style scoped>
.collect-folder-list {
  max-height: 300px;
  overflow-y: auto;
}

.folder-item {
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.folder-name {
  font-size: 14px;
}

.folder-count {
  font-size: 12px;
  color: #909399;
}

.new-folder-area {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>