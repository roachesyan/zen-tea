export function formatPrice(price: number): string {
  return `¥${price.toFixed(2)}`
}

export function formatDateTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${m}-${d} ${h}:${min}`
}

export const ORDER_STATUS_MAP: Record<string, string> = {
  PENDING: '待处理',
  MAKING: '制作中',
  DONE: '已完成',
  CANCELLED: '已取消',
}

export function getOrderStatusText(status: string): string {
  return ORDER_STATUS_MAP[status] ?? status
}

const ICE_LABELS: Record<string, string> = {
  normal: '正常冰',
  less: '少冰',
  no_ice: '去冰',
  hot: '热饮',
}

const SWEETNESS_LABELS: Record<string, string> = {
  full: '全糖',
  less: '少糖',
  half: '半糖',
  little: '微糖',
  none: '无糖',
}

const TOPPING_LABELS: Record<string, string> = {
  pearl: '珍珠',
  coconut_jelly: '椰果',
  taro_ball: '芋圆',
  red_bean: '红豆',
}

export function formatSpecs(specs: { ice: string; sweetness: string; toppings: string[] }): string {
  const parts: string[] = []
  parts.push(ICE_LABELS[specs.ice] ?? specs.ice)
  parts.push(SWEETNESS_LABELS[specs.sweetness] ?? specs.sweetness)
  if (specs.toppings.length > 0) {
    parts.push(specs.toppings.map((t) => TOPPING_LABELS[t] ?? t).join('+'))
  }
  return parts.join('/')
}
