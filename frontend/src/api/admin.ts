import request from './request'

export const adminApi = {
  login: (data: { username: string; password: string }): Promise<any> =>
    request.post('/api/admin/login', data),

  getCourseList: (keyword?: string): Promise<any> =>
    request.get('/api/admin/course/list', { params: keyword ? { keyword } : undefined }),

  addCourse: (data: { name: string; description?: string; icon?: string; parentId?: number; sortOrder?: number }): Promise<any> =>
    request.post('/api/admin/course', data),

  updateCourse: (courseId: number, data: { name: string; description?: string; icon?: string; parentId?: number; sortOrder?: number }): Promise<any> =>
    request.put(`/api/admin/course/${courseId}`, data),

  deleteCourse: (courseId: number): Promise<any> =>
    request.delete(`/api/admin/course/${courseId}`),

  getQuestionList: (params: { pageNum: number; pageSize: number; keyword?: string }): Promise<any> =>
    request.get('/api/admin/question/list', { params }),

  getResourceList: (params: { pageNum: number; pageSize: number; keyword?: string }): Promise<any> =>
    request.get('/api/admin/resource/list', { params }),

  offlineQuestion: (questionId: number): Promise<any> =>
    request.put(`/api/admin/question/${questionId}/offline`),

  offlineResource: (resourceId: number): Promise<any> =>
    request.put(`/api/admin/resource/${resourceId}/offline`),

  getStatistics: (): Promise<any> =>
    request.get('/api/admin/statistics'),

  // 回答管理
  getAnswerList: (params: { pageNum: number; pageSize: number; keyword?: string }): Promise<any> =>
    request.get('/api/admin/answer/list', { params }),

  offlineAnswer: (answerId: number): Promise<any> =>
    request.put(`/api/admin/answer/${answerId}/offline`),

  // 用户管理
  getUserList: (params: { pageNum: number; pageSize: number; keyword?: string }): Promise<any> =>
    request.get('/api/admin/user/list', { params }),

  banUser: (data: { userId: number; banType: number; banReason: string; sourceType?: number; sourceId?: number }): Promise<any> =>
    request.post('/api/admin/user/ban', data),

  unbanUser: (userId: number): Promise<any> =>
    request.post(`/api/admin/user/${userId}/unban`),

  // 举报工单
  getReportList: (params: { pageNum: number; pageSize: number; status?: number; keyword?: string }): Promise<any> =>
    request.get('/api/admin/report/list', { params }),

  getReportDetail: (reportId: number): Promise<any> =>
    request.get(`/api/admin/report/${reportId}`),

  handleReport: (reportId: number, data: { status: number; handleNote?: string; banType?: number; banReason?: string }): Promise<any> =>
    request.put(`/api/admin/report/${reportId}/handle`, data),

  // 处罚日志
  getBanLogList: (params: { pageNum: number; pageSize: number; keyword?: string }): Promise<any> =>
    request.get('/api/admin/ban-log/list', { params }),

  // 举报工单待处理数量
  getPendingReportCount: (): Promise<any> =>
    request.get('/api/admin/report/pending-count'),
}