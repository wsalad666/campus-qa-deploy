export interface User {
  id: number
  studentNo: string
  username: string
  nickname: string
  avatar: string
  signature: string
  role: number
  status: number
  isFollowed?: boolean
  createTime: string
}

export interface Question {
  id: number
  userId: number
  userNickname: string
  userAvatar: string
  courseId: number
  courseName: string
  title: string
  content: string
  imageUrls: string[]
  status: number
  viewCount: number
  answerCount: number
  likeCount: number
  favoriteCount: number
  isLiked: boolean
  isFavorited: boolean
  isOffline: number
  createTime: string
}

export interface Answer {
  id: number
  questionId: number
  userId: number
  userNickname: string
  userAvatar: string
  content: string
  imageUrls: string[]
  likeCount: number
  commentCount: number
  isAccepted: number
  createTime: string
  isLiked: boolean
  comments: Comment[]
}

export interface Comment {
  id: number
  answerId: number
  userId: number
  userNickname: string
  userAvatar: string
  parentId: number
  replyToId: number
  replyToNickname: string
  content: string
  createTime: string
  children: Comment[]
}

export interface Resource {
  id: number
  courseId: number
  courseName: string
  userId: number
  userNickname: string
  title: string
  description: string
  category: string
  fileType: string
  fileSize: number
  downloadCount: number
  resourceType: number | null
  isOffline: number
  createTime: string
  fileUrl: string
}

export interface Course {
  id: number
  name: string
  description: string
  icon: string
  parentId: number
  sortOrder: number
}

export interface QuestionDetail {
  id: number
  userId: number
  userNickname: string
  userAvatar: string
  courseId: number
  courseName: string
  title: string
  content: string
  imageUrls: string[]
  status: number
  viewCount: number
  answerCount: number
  likeCount: number
  favoriteCount: number
  createTime: string
  isFavorited: boolean
  isLiked: boolean
  adoptAnswerId: number | null
  answers: Answer[]
}

export interface UserProfile {
  user: User
  questionCount: number
  resourceCount: number
  fansCount: number
  followCount: number
  isFollowed: boolean
}

export interface Statistics {
  totalUsers: number
  todayNewUsers: number
  totalQuestions: number
  todayQuestions: number
  totalResources: number
  totalDownloads: number
}

export interface AdminLoginResponse {
  token: string
  adminId: number
  username: string
  nickname: string
  avatar: string
  userId: number
}

export interface PageResult<T> {
  total: number
  pageNum: number
  pageSize: number
  records: T[]
}

export interface FavoriteItem {
  id: number
  targetId: number
  type: number
  title: string
  description: string
  createTime: string
}

export interface ReportVO {
  id: number
  reporterId: number
  reporterNickname: string
  targetType: number
  targetId: number
  targetTitle: string
  targetContent: string
  reason: string
  status: number
  handlerId: number | null
  handlerNickname: string
  handleNote: string
  handleTime: string
  createTime: string
  // 完整问答上下文
  questionId: number
  questionTitle: string
  questionContent: string
  questionUserNickname: string
  questionCreateTime: string
  answers: AnswerContextVO[]
  reportedAnswerId: number | null
}

export interface AnswerContextVO {
  id: number
  userId: number
  userNickname: string
  userAvatar: string
  content: string
  likeCount: number
  commentCount: number
  isAccepted: number
  isOffline: number
  isReported: boolean
  createTime: string
}

export interface AnswerManageVO {
  id: number
  questionId: number
  questionTitle: string
  userId: number
  userNickname: string
  content: string
  likeCount: number
  commentCount: number
  isAccepted: number
  isOffline: number
  createTime: string
}

export interface UserManageVO {
  id: number
  studentNo: string
  username: string
  nickname: string
  avatar: string
  status: number
  isOffline: number
  banEndTime: string
  banReason: string
  createTime: string
}

export interface UserBanLogVO {
  id: number
  userId: number
  userNickname: string
  adminId: number
  adminNickname: string
  banType: number
  banReason: string
  banStartTime: string
  banEndTime: string
  sourceType: number
  sourceId: number
  isActive: number
  createTime: string
}

export interface NotificationVO {
  id: number
  userId: number
  title: string
  content: string
  type: number
  isRead: number
  linkType: number | null
  linkId: number | null
  senderId: number | null
  senderNickname: string | null
  isDeletable: number
  createTime: string
}

export interface CollectFolder {
  id: number
  folderName: string
  count: number
  createTime: string
}

export interface CollectItem {
  relationId: number
  targetId: number
  targetType: number
  title: string
  description: string
  courseName: string
  userNickname: string
  createTime: string
}

export interface SimilarQuestion {
  id: number
  title: string
  viewCount: number
  answerCount: number
  similarity: number
}