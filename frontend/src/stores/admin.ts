import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('adminToken') || '')
  const adminInfo = ref<{ adminId: number; username: string; nickname: string; avatar: string; userId: number } | null>(null)
  const isLoggedIn = ref(false)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('adminToken', t)
    localStorage.setItem('userRole', 'admin')
    isLoggedIn.value = true
  }

  function setAdminInfo(info: { adminId: number; username: string; nickname: string; avatar: string; userId: number }) {
    adminInfo.value = info
    if (info.userId) {
      localStorage.setItem('adminUserId', String(info.userId))
    }
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('adminToken')
    localStorage.removeItem('userRole')
  }

  function restoreFromStorage() {
    const t = localStorage.getItem('adminToken')
    const role = localStorage.getItem('userRole')
    if (t && role === 'admin') {
      token.value = t
      isLoggedIn.value = true
    }
  }

  return { token, adminInfo, isLoggedIn, setToken, setAdminInfo, logout, restoreFromStorage }
})