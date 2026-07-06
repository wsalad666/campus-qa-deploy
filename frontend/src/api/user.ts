import request from './request'

export const userApi = {
  register: (data: { studentNo: string; username: string; password: string }): Promise<any> =>
    request.post('/api/user/register', data),

  login: (data: { studentNo: string; password: string }): Promise<any> =>
    request.post('/api/user/login', data),

  updateProfile: (data: { nickname?: string; avatar?: string; signature?: string }): Promise<any> =>
    request.put('/api/user/profile', data),

  uploadAvatar: (file: File): Promise<any> => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },

  getProfile: (userId: number): Promise<any> =>
    request.get(`/api/user/profile/${userId}`),

  getUserInfo: (userId: number): Promise<any> =>
    request.get(`/api/user/profile/${userId}`),

  toggleFollow: (data: { followedId: number }): Promise<any> =>
    request.post('/api/user/follow/toggle', data),

  getFollowList: (): Promise<any> =>
    request.get('/api/user/follow/list'),

  getFansList: (): Promise<any> =>
    request.get('/api/user/fans/list'),

  toggleFavorite: (data: { targetId: number; type: number }): Promise<any> =>
    request.post('/api/user/favorite/toggle', data),

  getFavorites: (params: { pageNum: number; pageSize: number }): Promise<any> =>
    request.get('/api/user/favorite/list', { params }),

  searchUsers: (keyword: string): Promise<any> =>
    request.get('/api/user/search', { params: { keyword } }),

  // 常用课程
  getFavoriteCourses: (): Promise<any> =>
    request.get('/api/user/course/favorites'),

  updateFavoriteCourses: (courseIds: number[]): Promise<any> =>
    request.put('/api/user/course/favorites', courseIds),

  // 收藏夹管理
  getCollectFolders: (): Promise<any> =>
    request.get('/api/user/collect/folders'),

  createCollectFolder: (folderName: string): Promise<any> =>
    request.post('/api/user/collect/folder', { folderName }),

  renameCollectFolder: (folderId: number, folderName: string): Promise<any> =>
    request.put(`/api/user/collect/folder/${folderId}`, { folderName }),

  deleteCollectFolder: (folderId: number): Promise<any> =>
    request.delete(`/api/user/collect/folder/${folderId}`),

  addCollect: (data: { folderIds?: number[]; targetType: number; targetId: number }): Promise<any> =>
    request.post('/api/user/collect/add', data),

  getCollectItems: (params: { folderId: number; targetType?: number }): Promise<any> =>
    request.get('/api/user/collect/items', { params }),

  checkCollected: (params: { folderId: number; targetType: number; targetId: number }): Promise<any> =>
    request.get('/api/user/collect/check', { params }),

  moveCollect: (relationId: number, folderId: number): Promise<any> =>
    request.put(`/api/user/collect/move/${relationId}`, { folderId }),

  removeCollect: (relationId: number): Promise<any> =>
    request.delete(`/api/user/collect/${relationId}`),
}