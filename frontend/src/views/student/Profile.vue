<script setup lang="ts">
import { formatDateTime } from '@/utils/time'
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import Pagination from '@/components/Pagination.vue'
import QuestionCard from '@/components/QuestionCard.vue'
import ResourceCard from '@/components/ResourceCard.vue'
import PreviewDialog from '@/components/PreviewDialog.vue'
import CollectFolderSelect from '@/components/CollectFolderSelect.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import { qaApi } from '@/api/qa'
import { resourceApi } from '@/api/resource'
import { userApi } from '@/api/user'
import { useAuth } from '@/composables/useAuth'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'
import { usePagination } from '@/composables/usePagination'
import type { Question, Resource, User } from '@/types'

const { requireAuth } = useAuth()
const userStore = useUserStore()
const adminStore = useAdminStore()
const router = useRouter()

const isAdmin = localStorage.getItem('userRole') === 'admin'
const currentUserId = isAdmin ? Number(localStorage.getItem('adminUserId')) : (userStore.userInfo?.id || 0)

const userInfo = ref<User | null>(null)
const editNickname = ref('')
const editSignature = ref('')
const profileLoading = ref(false)
const activeTab = ref('questions')
const loading = ref(false)
const followActionLoading = ref<number | null>(null)

// Resource detail dialog
const detailDialogVisible = ref(false)
const detailResource = ref<Resource | null>(null)

// Preview dialog
const previewDialogVisible = ref(false)
const previewResource = ref<Resource | null>(null)

// Collect dialog
const collectDialogVisible = ref(false)
const collectTargetType = ref(2)
const collectTargetId = ref(0)

function onResourceClick(resource: Resource) {
  detailResource.value = resource
  detailDialogVisible.value = true
}

function onResourcePreview(resource: Resource) {
  previewResource.value = resource
  previewDialogVisible.value = true
}

async function onResourceCollect(resource: Resource) {
  const res = await userApi.isCollectedAny(2, resource.id).catch(() => null)
  if (res) {
    try {
      await ElMessageBox.confirm('确定取消收藏该资源？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userApi.removeCollectByTarget(2, resource.id)
      ElMessage.success('已取消收藏')
      fetchTabData()
    } catch {
      // 用户取消
    }
    return
  }
  collectTargetType.value = 2
  collectTargetId.value = resource.id
  collectDialogVisible.value = true
}

function formatFileSize(bytes: number): string {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

function formatTime(time: string) { return formatDateTime(time) }

function getResourceTypeLabel(type: number | null): string {
  const labels = ['试卷', '习题', '笔记', '课件']
  if (type === null || type === undefined) return '未知'
  return labels[type] || '未知'
}

// Avatar dialog
const avatarDialogVisible = ref(false)
const avatarFile = ref<File | null>(null)
const avatarPreviewUrl = ref('')
const avatarUploading = ref(false)
const avatarInputRef = ref<HTMLInputElement>()

const pag = usePagination(10)
const questions = ref<Question[]>([])
const resources = ref<Resource[]>([])
const followList = ref<User[]>([])
const fansList = ref<User[]>([])

async function saveProfile() {
  profileLoading.value = true
  try {
    await userApi.updateProfile({ nickname: editNickname.value, signature: editSignature.value })
    if (userInfo.value) {
      userInfo.value.nickname = editNickname.value
      userInfo.value.signature = editSignature.value
    }
    if (!isAdmin && userStore.userInfo) {
      userStore.userInfo.nickname = editNickname.value
      userStore.userInfo.signature = editSignature.value
      userStore.setUserInfo(userStore.userInfo)
    }
    ElMessage.success('保存成功')
  } catch { /* handled by interceptor */ }
  finally { profileLoading.value = false }
}

async function fetchTabData() {
  loading.value = true
  try {
    const { pageNum, pageSize } = pag
    if (activeTab.value === 'questions') {
      const res: any = await qaApi.getQuestionList({ pageNum: pageNum.value, pageSize: pageSize.value })
      const records = (res.records || []) as Question[]
      questions.value = records.filter((q: Question) => q.userId === currentUserId)
      pag.total.value = questions.value.length
    } else if (activeTab.value === 'resources') {
      const res: any = await resourceApi.getMyResources({ pageNum: pageNum.value, pageSize: pageSize.value })
      resources.value = res.records || []
      pag.total.value = res.total
    } else if (activeTab.value === 'follows') {
      const res: any = await userApi.getFollowList()
      followList.value = res || []
      pag.total.value = followList.value.length
    } else if (activeTab.value === 'fans') {
      const res: any = await userApi.getFansList()
      fansList.value = res || []
      pag.total.value = fansList.value.length
    }
  } catch { /* handled by interceptor */ }
  finally { loading.value = false }
}

async function handleUnfollow(userId: number) {
  followActionLoading.value = userId
  try {
    await userApi.toggleFollow({ followedId: userId })
    ElMessage.success('已取消关注')
    fetchTabData()
  } catch { /* handled by interceptor */ }
  finally { followActionLoading.value = null }
}

async function handleFollowBack(userId: number) {
  followActionLoading.value = userId
  try {
    await userApi.toggleFollow({ followedId: userId })
    ElMessage.success('已关注')
    fetchTabData()
  } catch { /* handled by interceptor */ }
  finally { followActionLoading.value = null }
}

async function handleDeleteResource(resourceId: number) {
  try {
    await ElMessageBox.confirm(
      '确定要删除该资源吗？删除后文件将从服务器移除，其他用户无法再下载。',
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
  } catch {
    return // 用户取消
  }
  try {
    await resourceApi.deleteResource(resourceId)
    ElMessage.success('资源已删除')
    fetchTabData()
  } catch {
    // error handled by interceptor
  }
}

function goToUserHome(userId: number) {
  router.push(`/student/user/${userId}`)
}

function openAvatarDialog() {
  avatarFile.value = null
  avatarPreviewUrl.value = ''
  avatarDialogVisible.value = true
}

function onAvatarFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  const allowedTypes = ['image/jpeg', 'image/png']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG 格式的图片')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  avatarFile.value = file
  avatarPreviewUrl.value = URL.createObjectURL(file)
}

async function uploadAvatar() {
  if (!avatarFile.value || avatarUploading.value) return
  avatarUploading.value = true
  try {
    const res: any = await userApi.uploadAvatar(avatarFile.value)
    const avatarPath = res
    if (userInfo.value) {
      userInfo.value.avatar = avatarPath
    }
    if (!isAdmin && userStore.userInfo) {
      userStore.userInfo.avatar = avatarPath
      userStore.setUserInfo(userStore.userInfo)
    }
    ElMessage.success('头像更新成功')
    avatarDialogVisible.value = false
  } catch {
    // error handled by interceptor
  } finally {
    avatarUploading.value = false
  }
}

watch(activeTab, () => { pag.reset(); fetchTabData() })
watch([pag.pageNum, pag.pageSize], () => { fetchTabData() })

onMounted(async () => {
  if (isAdmin) {
    // 管理员：从API加载用户信息
    if (currentUserId) {
      try {
        const res = await userApi.getProfile(currentUserId) as any
        const info = res.user || res
        userInfo.value = info
        editNickname.value = info.nickname || ''
        editSignature.value = info.signature || ''
      } catch { /* ignore */ }
    }
    fetchTabData()
    return
  }
  if (!requireAuth()) return
  userStore.restoreFromStorage()
  const info = userStore.userInfo
  if (info) {
    userInfo.value = info
    editNickname.value = info.nickname || ''
    editSignature.value = info.signature || ''
  }
  fetchTabData()
})
</script>

<template>
  <div class="profile-container">
    <AppHeader />
    <main class="profile-main">
      <div class="profile-card">
        <div class="profile-banner">
          <div class="banner-pattern"></div>
        </div>
        <div class="profile-card-top">
          <div class="avatar-wrapper" @click="openAvatarDialog">
            <UserAvatar :src="userInfo?.avatar" :size="80" :nickname="userInfo?.nickname" />
            <div class="avatar-overlay">
              <el-icon :size="20"><Plus /></el-icon>
            </div>
          </div>
          <div class="profile-edit-area">
            <div class="edit-field">
              <label class="field-label">昵称</label>
              <el-input v-model="editNickname" placeholder="设置昵称" maxlength="20" show-word-limit size="large" />
            </div>
            <div class="edit-field">
              <label class="field-label">个性签名</label>
              <el-input v-model="editSignature" placeholder="设置个性签名" maxlength="50" show-word-limit size="large" />
            </div>
          </div>
        </div>
        <div class="profile-card-actions">
          <el-button type="primary" :loading="profileLoading" @click="saveProfile">💾 保存修改</el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="💬 我的提问" name="questions">
          <div v-loading="loading">
            <QuestionCard v-for="q in questions" :key="q.id" :question="q" />
            <el-empty v-if="!loading && questions.length === 0" description="暂无提问" />
            <Pagination v-if="pag.total.value > pag.pageSize.value"
              :total="pag.total.value" :page-num="pag.pageNum.value" :page-size="pag.pageSize.value"
              @update:page-num="pag.handlePageChange" @update:page-size="pag.handleSizeChange" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="📁 我的资源" name="resources">
          <div v-loading="loading">
            <ResourceCard
              v-for="r in resources"
              :key="r.id"
              layout="list"
              :resource="r"
              show-delete
              @click="onResourceClick"
              @delete="handleDeleteResource"
              @preview="onResourcePreview"
              @collect="onResourceCollect"
            />
            <el-empty v-if="!loading && resources.length === 0" description="暂无资源" />
            <Pagination v-if="pag.total.value > pag.pageSize.value"
              :total="pag.total.value" :page-num="pag.pageNum.value" :page-size="pag.pageSize.value"
              @update:page-num="pag.handlePageChange" @update:page-size="pag.handleSizeChange" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="👀 我的关注" name="follows">
          <div v-loading="loading">
            <div v-for="u in followList" :key="u.id" class="user-card-item">
              <div class="user-card-left" @click="goToUserHome(u.id)" style="cursor: pointer">
                <UserAvatar :src="u.avatar" :size="40" :nickname="u.nickname" />
                <div class="user-card-info">
                  <span class="user-card-name">{{ u.nickname || u.username }}</span>
                  <span class="user-card-signature">{{ u.signature || '暂无签名' }}</span>
                </div>
              </div>
              <el-button size="small" :loading="followActionLoading === u.id" @click="handleUnfollow(u.id)">取消关注</el-button>
            </div>
            <el-empty v-if="!loading && followList.length === 0" description="暂无关注" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="❤️ 我的粉丝" name="fans">
          <div v-loading="loading">
            <div v-for="u in fansList" :key="u.id" class="user-card-item">
              <div class="user-card-left" @click="goToUserHome(u.id)" style="cursor: pointer">
                <UserAvatar :src="u.avatar" :size="40" :nickname="u.nickname" />
                <div class="user-card-info">
                  <span class="user-card-name">{{ u.nickname || u.username }}</span>
                  <span class="user-card-signature">{{ u.signature || '暂无签名' }}</span>
                </div>
              </div>
              <el-button
                v-if="u.isFollowed"
                size="small"
                :loading="followActionLoading === u.id"
                @click="handleUnfollow(u.id)"
              >取消关注</el-button>
              <el-button
                v-else
                size="small"
                type="primary"
                :loading="followActionLoading === u.id"
                @click="handleFollowBack(u.id)"
              >回关</el-button>
            </div>
            <el-empty v-if="!loading && fansList.length === 0" description="暂无粉丝" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </main>

    <!-- Avatar upload dialog -->
    <el-dialog v-model="avatarDialogVisible" title="更换头像" width="400px" destroy-on-close>
      <div class="avatar-dialog-content">
        <div class="avatar-preview">
          <el-avatar
            :size="120"
            :src="avatarPreviewUrl || (userInfo?.avatar ? '' + userInfo?.avatar : '')"
          >
            <span class="avatar-letter-lg">{{ editNickname?.charAt(0) || 'U' }}</span>
          </el-avatar>
        </div>
        <div class="avatar-upload-area">
          <input
            ref="avatarInputRef"
            type="file"
            accept="image/jpeg,image/png"
            @change="onAvatarFileChange"
          />
          <p class="upload-hint">支持 JPG、PNG 格式，最大 5MB</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="avatarUploading" :disabled="!avatarFile" @click="uploadAvatar">
          上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 文件预览弹窗 -->
    <PreviewDialog
      v-model="previewDialogVisible"
      :title="previewResource?.title"
      :file-url="previewResource?.fileUrl"
      :file-type="previewResource?.fileType"
    />

    <!-- 收藏文件夹选择弹窗 -->
    <CollectFolderSelect
      v-model="collectDialogVisible"
      :target-type="collectTargetType"
      :target-id="collectTargetId"
    />

    <!-- 资料详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="资料详情"
      width="520px"
      destroy-on-close
    >
      <template v-if="detailResource">
        <div class="detail-body">
          <div class="detail-row">
            <span class="detail-label">文件名称</span>
            <span class="detail-value">{{ detailResource.title }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">所属课程</span>
            <span class="detail-value">{{ detailResource.courseName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">资料类型</span>
            <span class="detail-value">
              <el-tag size="small" type="warning">{{ getResourceTypeLabel(detailResource.resourceType) }}</el-tag>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">文件格式</span>
            <span class="detail-value">{{ detailResource.fileType?.toUpperCase() }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">文件大小</span>
            <span class="detail-value">{{ formatFileSize(detailResource.fileSize) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">上传者</span>
            <span class="detail-value">{{ detailResource.userNickname }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">下载次数</span>
            <span class="detail-value">{{ detailResource.downloadCount }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">上传时间</span>
            <span class="detail-value">{{ formatTime(detailResource.createTime) }}</span>
          </div>
          <div class="detail-row" v-if="detailResource.description">
            <span class="detail-label">描述</span>
            <span class="detail-value desc">{{ detailResource.description }}</span>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-container { min-height: 100vh; background: #f5f7fa; }
.profile-main { max-width: 860px; margin: 0 auto; padding: 88px 24px 40px; }

.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 0 32px 28px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.profile-banner {
  position: relative;
  height: 120px;
  background: linear-gradient(135deg, #4f8ef7 0%, #7c3aed 50%, #a855f7 100%);
  margin: 0 -32px;
}

.banner-pattern {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.12) 0%, transparent 50%),
              radial-gradient(circle at 80% 30%, rgba(255,255,255,0.08) 0%, transparent 40%);
}

.profile-card-top {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-top: -40px;
  position: relative;
  z-index: 1;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
  border: 4px solid #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.profile-edit-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 44px;
}

.edit-field {
  display: flex;
  align-items: center;
  gap: 12px;
}

.field-label {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  width: 72px;
  flex-shrink: 0;
  text-align: right;
}

.profile-card-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.profile-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 0 20px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.user-card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  margin-bottom: 8px;
  border-radius: 10px;
  transition: background 0.2s;
}

.user-card-item:hover {
  background: #f8fafc;
}

.user-card-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-card-info {
  display: flex;
  flex-direction: column;
}

.user-card-name {
  font-size: 15px;
  font-weight: 500;
  color: #1e293b;
}

.user-card-signature {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.avatar-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.avatar-preview {
  display: flex;
  justify-content: center;
}

.avatar-letter-lg {
  font-size: 36px;
  font-weight: 500;
}

.avatar-upload-area {
  text-align: center;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.detail-body {
  padding: 0 8px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  width: 80px;
  flex-shrink: 0;
  color: #909399;
  font-size: 14px;
}

.detail-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
  word-break: break-all;
}

.detail-value.desc {
  color: #606266;
  line-height: 1.6;
}
</style>