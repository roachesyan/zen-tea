export function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`
}

export function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}:${s}`
}

export const ORDER_STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '待处理', color: 'orange' },
  MAKING: { text: '制作中', color: 'blue' },
  DONE: { text: '已完成', color: 'green' },
  CANCELLED: { text: '已取消', color: 'default' },
}

export function getOrderStatusText(status: string): string {
  return ORDER_STATUS_MAP[status]?.text ?? status
}

export function getOrderStatusColor(status: string): string {
  return ORDER_STATUS_MAP[status]?.color ?? 'default'
}
