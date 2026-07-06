import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/student/home',
    },
    {
      path: '/student/login',
      name: 'StudentLogin',
      component: () => import('@/views/student/Login.vue'),
    },
    {
      path: '/student/home',
      name: 'StudentHome',
      component: () => import('@/views/student/Home.vue'),
    },
    {
      path: '/student/qa',
      name: 'QaList',
      component: () => import('@/views/student/QaList.vue'),
    },
    {
      path: '/student/qa/:id',
      name: 'QaDetail',
      component: () => import('@/views/student/QaDetail.vue'),
    },
    {
      path: '/student/resource',
      name: 'ResourceCenter',
      component: () => import('@/views/student/ResourceCenter.vue'),
    },
    {
      path: '/student/resource/:id',
      name: 'ResourceDetail',
      component: () => import('@/views/student/ResourceDetail.vue'),
    },
    {
      path: '/student/profile',
      name: 'StudentProfile',
      component: () => import('@/views/student/Profile.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/student/collect',
      name: 'MyCollect',
      component: () => import('@/views/student/MyCollect.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/student/user/:userId',
      name: 'UserProfile',
      component: () => import('@/views/student/UserProfile.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/student/user/:id',
      name: 'UserHome',
      component: () => import('@/views/student/UserHome.vue'),
    },
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/Login.vue'),
    },
    {
      path: '/admin/dashboard',
      name: 'AdminDashboard',
      component: () => import('@/views/admin/Dashboard.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/courses',
      name: 'CourseManage',
      component: () => import('@/views/admin/CourseManage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/questions',
      name: 'QuestionManage',
      component: () => import('@/views/admin/QuestionManage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/resources',
      name: 'ResourceManage',
      component: () => import('@/views/admin/ResourceManage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/answers',
      name: 'AnswerManage',
      component: () => import('@/views/admin/AnswerManage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/users',
      name: 'UserList',
      component: () => import('@/views/admin/UserList.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/reports',
      name: 'ReportManage',
      component: () => import('@/views/admin/ReportManage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/ban-logs',
      name: 'BanLog',
      component: () => import('@/views/admin/BanLog.vue'),
      meta: { requiresAdmin: true },
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const adminStore = useAdminStore()

  if (to.meta.requiresAuth) {
    userStore.restoreFromStorage()
    if (!userStore.isLoggedIn || !userStore.token) {
      return next('/student/login')
    }
  }

  if (to.meta.requiresAdmin) {
    adminStore.restoreFromStorage()
    if (!adminStore.isLoggedIn || !adminStore.token) {
      return next('/admin/login')
    }
  }

  // 防止学生访问管理员路由
  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    const role = localStorage.getItem('userRole')
    if (role !== 'admin') {
      return next('/admin/login')
    }
  }

  // 管理员可以访问学生端页面（用于审核举报、查看个人主页等）
  if (to.path.startsWith('/student')) {
    const role = localStorage.getItem('userRole')
    if (role === 'admin' && to.path !== '/student/login') {
      // 允许管理员访问所有学生端页面（JwtAuthInterceptor会处理token验证）
      return next()
    }
  }

  next()
})

export default router