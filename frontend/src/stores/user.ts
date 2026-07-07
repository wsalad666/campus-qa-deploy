import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)
  const isLoggedIn = ref(false)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
    localStorage.setItem('userRole', 'student')
    isLoggedIn.value = true
  }

  function setUserInfo(info: User) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    localStorage.removeItem('userInfo')
  }

  function restoreFromStorage() {
    const t = localStorage.getItem('token')
    const role = localStorage.getItem('userRole')
    const info = localStorage.getItem('userInfo')
    if (t && role === 'student') {
      token.value = t
      isLoggedIn.value = true
      if (info) {
        try {
          const parsed = JSON.parse(info)
          // Normalize: backend LoginResponse returns userId, but frontend User type uses id
          if (parsed.userId != null && parsed.id == null) {
            parsed.id = parsed.userId
          }
          userInfo.value = parsed
        } catch { /* ignore */ }
      }
    }
  }

  return { token, userInfo, isLoggedIn, setToken, setUserInfo, logout, restoreFromStorage }
})