/**
 * 校验登录成功后的重定向目标，防止开放重定向。
 * 仅接受站内相对路径（以单个 `/` 开头），其余一律返回 undefined。
 */
export function safeRedirectPath(value: unknown): string | undefined {
  const target = Array.isArray(value) ? value[0] : value
  if (typeof target !== 'string') return undefined
  if (!target.startsWith('/') || target.startsWith('//')) return undefined
  return target
}
