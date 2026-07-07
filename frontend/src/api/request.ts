import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '',
  timeout: 30000,
})

request.interceptors.request.use(
  (config) => {
    const role = localStorage.getItem('userRole')
    const token = role === 'admin'
      ? localStorage.getItem('adminToken')
      : localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  async (response) => {
    // 检查是否需要续期token（管理员端）
    const newToken = response.headers['x-new-token']
    if (newToken && localStorage.getItem('userRole') === 'admin') {
      localStorage.setItem('adminToken', newToken)
    }
    
    // 如果是 blob 类型（如文件下载），先检查是否为 JSON 错误响应
    if (response.config.responseType === 'blob') {
      const contentType: string = String(response.headers['content-type'] || '')
      if (contentType.includes('application/json')) {
        try {
          const text = await response.data.text()
          const data = JSON.parse(text)
          if (data.code !== 200) {
            ElMessage.error(data.message || '下载失败')
            return Promise.reject(new Error(data.message || '下载失败'))
          }
        } catch {
          // 解析失败，继续按文件处理
        }
      }
      return response.data
    }
    const data = response.data
    if (data.code === 200) {
      return data.data
    }
    if (data.code === 401) {
      const isAdmin = localStorage.getItem('userRole') === 'admin'
      // 只清除当前角色的 token，避免一个角色过期导致另一个角色也被强制退出
      if (isAdmin) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('userRole')
      } else {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
      window.location.href = isAdmin ? '/admin/login' : '/student/login'
      return Promise.reject(new Error(data.message || '未登录'))
    }
    if (data.code === 403) {
      ElMessage.error(data.message || '无权限')
      return Promise.reject(new Error(data.message || '无权限'))
    }
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      // 根据请求 URL 判断是管理员还是学生端的 401，只清除对应 token
      const isAdminUrl = error.config?.url?.startsWith('/api/admin/')
      if (isAdminUrl) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('userRole')
      } else {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
    }
    // 如果是 blob 下载失败，尝试解析错误信息
    if (error.config?.responseType === 'blob' && error.response?.data) {
      ElMessage.error('下载失败，请重试')
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || '服务器异常')
    return Promise.reject(error)
  }
)

export default request