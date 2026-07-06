import request from './request'

export const resourceApi = {
  upload: (formData: FormData): Promise<any> =>
    request.post('/api/resource/upload', formData),

  getList: (params: { pageNum: number; pageSize: number; courseId?: number; keyword?: string; resourceType?: number; userId?: number }): Promise<any> =>
    request.get('/api/resource/list', { params }),

  getMyResources: (params: { pageNum: number; pageSize: number }): Promise<any> =>
    request.get('/api/resource/my', { params }),

  download: (resourceId: number): Promise<Blob> =>
    request.get(`/api/resource/download/${resourceId}`, { responseType: 'blob' }),

  getDownloadUrl: (resourceId: number): string =>
    `http://localhost:8080/api/resource/download/${resourceId}`,

  deleteResource: (resourceId: number): Promise<any> =>
    request.delete(`/api/resource/${resourceId}`),
}