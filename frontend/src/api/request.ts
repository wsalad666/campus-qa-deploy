import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
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
  (response) => {
    // 妫€鏌ユ槸鍚﹂渶瑕佺画鏈焧oken锛堢鐞嗗憳绔級
    const newToken = response.headers['x-new-token']
    if (newToken && localStorage.getItem('userRole') === 'admin') {
      localStorage.setItem('adminToken', newToken)
    }
    
    // 濡傛灉鏄?blob 绫诲瀷锛堝鏂囦欢涓嬭浇锛夛紝鐩存帴杩斿洖鍘熷鏁版嵁
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const data = response.data
    if (data.code === 200) {
      return data.data
    }
    if (data.code === 401) {
      const isAdmin = localStorage.getItem('userRole') === 'admin'
      // 鍙竻闄ゅ綋鍓嶈鑹茬殑 token锛岄伩鍏嶄竴涓鑹茶繃鏈熷鑷村彟涓€涓鑹蹭篃琚己鍒堕€€鍑?
      if (isAdmin) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('userRole')
      } else {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
      window.location.href = isAdmin ? '/admin/login' : '/student/login'
      return Promise.reject(new Error(data.message || '鏈櫥褰?))
    }
    if (data.code === 403) {
      ElMessage.error(data.message || '鏃犳潈闄?)
      return Promise.reject(new Error(data.message || '鏃犳潈闄?))
    }
    ElMessage.error(data.message || '璇锋眰澶辫触')
    return Promise.reject(new Error(data.message || '璇锋眰澶辫触'))
  },
  (error) => {
    if (error.response?.status === 401) {
      // 鏍规嵁璇锋眰 URL 鍒ゆ柇鏄鐞嗗憳杩樻槸瀛︾敓绔殑 401锛屽彧娓呴櫎瀵瑰簲 token
      const isAdminUrl = error.config?.url?.startsWith('/api/admin/')
      if (isAdminUrl) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('userRole')
      } else {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
    }
    // 濡傛灉鏄?blob 涓嬭浇澶辫触锛屽皾璇曡В鏋愰敊璇俊鎭?
    if (error.config?.responseType === 'blob' && error.response?.data) {
      ElMessage.error('涓嬭浇澶辫触锛岃閲嶈瘯')
      return Promise.reject(error)
    }
    ElMessage.error(error.response?.data?.message || '鏈嶅姟鍣ㄥ紓甯?)
    return Promise.reject(error)
  }
)

export default request