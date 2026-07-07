/**
 * 解析后端返回的时间字符串（格式：yyyy-MM-dd HH:mm:ss，GMT+8）
 * 避免 new Date(string) 在不同浏览器中的解析差异
 */
export function parseTime(time: string): Date {
  if (!time) return new Date()
  const [datePart, timePart] = time.split(' ')
  const [y, m, d] = datePart.split('-').map(Number)
  const [hh, mm, ss] = (timePart || '00:00:00').split(':').map(Number)
  return new Date(y, m - 1, d, hh, mm, ss)
}

/**
 * 格式化时间为相对时间（x分钟前、x小时前、x天前）
 */
export function formatRelativeTime(time: string): string {
  if (!time) return ''
  const d = parseTime(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return d.toLocaleDateString('zh-CN')
}

/**
 * 格式化时间为本地化字符串
 */
export function formatDateTime(time: string): string {
  if (!time) return ''
  return parseTime(time).toLocaleString('zh-CN')
}
