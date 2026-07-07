import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

export function useAuth() {
  const userStore = useUserStore()
  const router = useRouter()

  function requireAuth(): boolean {
    userStore.restoreFromStorage()
    if (!userStore.isLoggedIn || !userStore.token) {
      ElMessageBox.confirm('请先登录后再操作', '提示', {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        router.push('/student/login')
      }).catch(() => {})
      return false
    }
    return true
  }

  return { requireAuth }
}