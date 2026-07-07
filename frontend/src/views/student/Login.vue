<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'
import loginBg from '@/assets/login-bg.jpg'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive({
  studentNo: '',
  password: '',
})

const registerForm = reactive({
  studentNo: '',
  username: '',
  password: '',
  confirmPassword: '',
})

const registerRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: ['blur', 'change'] }],
  username: [{ required: true, message: '请输入用户名', trigger: ['blur', 'change'] }],
  password: [
    { required: true, message: '请输入密码', trigger: ['blur', 'change'] },
    { min: 6, message: '密码至少6位', trigger: ['blur', 'change'] },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: ['blur', 'change'] },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change'],
    },
  ],
}

const loginFormRef = ref()
const registerFormRef = ref()

const loginRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: ['blur', 'change'] }],
  password: [{ required: true, message: '请输入密码', trigger: ['blur', 'change'] }],
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loginLoading.value = true
  try {
    const res: any = await userApi.login({
      studentNo: loginForm.studentNo,
      password: loginForm.password,
    })
    userStore.setToken(res.token)
    userStore.setUserInfo({
      id: res.userId,
      username: res.username,
      nickname: res.nickname,
      avatar: res.avatar,
    } as any)
    ElMessage.success('登录成功')
    router.push('/student/home')
  } catch {
    // error handled by interceptor
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  registerLoading.value = true
  try {
    await userApi.register({
      studentNo: registerForm.studentNo,
      username: registerForm.username,
      password: registerForm.password,
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    registerForm.studentNo = ''
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
  } catch {
    // error handled by interceptor
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <div class="login-wrapper" :style="{ backgroundImage: `url(${loginBg})` }">
    <div class="login-card">
      <h2 class="login-title"><span class="title-icon">🎓</span> <span class="title-text">校园问答平台</span></h2>
      <p class="login-subtitle">知识共享 · 互助成长</p>
      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-width="0"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="studentNo">
              <el-input
                v-model="loginForm.studentNo"
                placeholder="请输入学号"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loginLoading"
                class="login-btn"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="0"
            size="large"
          >
            <el-form-item prop="studentNo">
              <el-input
                v-model="registerForm.studentNo"
                placeholder="请输入学号"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                prefix-icon="EditPen"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="registerLoading"
                class="login-btn"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="login-footer">
        <router-link to="/admin/login">管理员登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-wrapper {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
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
  padding: 40px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
}


.title-icon {
  font-size: 28px;
}

.title-text {
  background: linear-gradient(135deg, #4f8ef7, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  text-align: center;
  font-size: 14px;
  color: #909399;
  margin: -16px 0 20px;
}

.login-title {
  text-align: center;
  margin-bottom: 24px;
  font-size: 24px;
  color: #303133;
}

.login-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
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