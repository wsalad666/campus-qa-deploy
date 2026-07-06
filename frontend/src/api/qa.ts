import request from './request'
import axios from 'axios'

export const qaApi = {
  uploadImage: (file: File): Promise<any> => {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    return axios.post('http://localhost:8080/api/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    }).then(res => res.data.data)
  },

  publishQuestion: (data: { courseId: number; title: string; content: string; imageUrls?: string[] }): Promise<any> =>
    request.post('/api/qa/question', data),

  getQuestionList: (params: {
    pageNum: number
    pageSize: number
    courseId?: number
    keyword?: string
    sort?: string
    userId?: number
  }): Promise<any> => request.get('/api/qa/question/list', { params }),

  getQuestionDetail: (questionId: number): Promise<any> =>
    request.get(`/api/qa/question/${questionId}`),

  answerQuestion: (data: { questionId: number; content: string; imageUrls?: string[] }): Promise<any> =>
    request.post('/api/qa/answer', data),

  addComment: (data: {
    answerId: number
    content: string
    parentId?: number
    replyToId?: number
  }): Promise<any> => request.post('/api/qa/comment', data),

  likeAnswer: (answerId: number): Promise<any> =>
    request.post(`/api/qa/like/${answerId}`),

  likeQuestion: (questionId: number): Promise<any> =>
    request.post(`/api/qa/question/${questionId}/like`),

  submitReport: (data: { targetType: number; targetId: number; reason: string }): Promise<any> =>
    request.post('/api/qa/report', data),

  // 通知
  getNotifications: (params: { pageNum: number; pageSize: number; type?: number }): Promise<any> =>
    request.get('/api/qa/notification/list', { params }),
  getUnreadCount: (): Promise<any> =>
    request.get('/api/qa/notification/unread-count'),
  markRead: (notificationId: number): Promise<any> =>
    request.put(`/api/qa/notification/${notificationId}/read`),
  batchMarkRead: (): Promise<any> =>
    request.put('/api/qa/notification/read-all'),
  deleteNotification: (notificationId: number): Promise<any> =>
    request.delete(`/api/qa/notification/${notificationId}`),

  // 学生自主删除
  deleteComment: (commentId: number): Promise<any> =>
    request.delete(`/api/qa/comment/${commentId}`),

  deleteQuestion: (questionId: number): Promise<any> =>
    request.delete(`/api/qa/question/${questionId}`),

  closeQuestion: (questionId: number): Promise<any> =>
    request.put(`/api/qa/question/${questionId}/close`),

  hideQuestion: (questionId: number): Promise<any> =>
    request.put(`/api/qa/question/${questionId}/hide`),

  acceptAnswer: (answerId: number): Promise<any> =>
    request.post(`/api/qa/accept/${answerId}`),

  // 相似问题推荐
  getSimilarQuestions: (params: { courseId: number; excludeId?: number; title?: string; content?: string; limit?: number }): Promise<any> =>
    request.get('/api/qa/question/similar', { params }),
}