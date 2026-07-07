<template>
  <div class="login-wrapper" :style="{ backgroundImage: `url(${loginBg})` }">
    <el-card class="login-card">
      <template #header>
        <div class="login-card-header">
          <h2><span class="admin-icon">🛡️</span> 管理员登录</h2>
        </div>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="login-btn"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <router-link to="/student/login">学生登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { adminApi } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'
import type { AdminLoginResponse } from '@/types'
import loginBg from '@/assets/login-bg.jpg'

const router = useRouter()
const adminStore = useAdminStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: ['blur', 'change'] }],
  password: [{ required: true, message: '请输入密码', trigger: ['blur', 'change'] }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await adminApi.login(form) as AdminLoginResponse
    adminStore.setToken(data.token)
    adminStore.setAdminInfo({
      adminId: data.adminId,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar,
      userId: data.userId,
    })
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  overflow: hidden;
}

/* 有背景图时的暗色遮罩 */
.login-wrapper::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 0;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.login-card-header {
  text-align: center;
}

.login-card-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  background: linear-gradient(135deg, #4f8ef7, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.admin-icon {
  font-size: 24px;
  -webkit-text-fill-color: initial;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}

.login-footer a {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
}

.login-footer a:hover {
  color: #66b1ff;
}
</style>