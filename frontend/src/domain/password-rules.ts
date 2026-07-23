/** Shared password rules for the change-password flow. */
export const PASSWORD_RULE_HINT = '至少 8 位，且须包含大写字母、小写字母和数字'

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,128}$/

export function validateNewPassword(password: string): string | null {
  if (!password) return '请输入新密码'
  if (!PASSWORD_PATTERN.test(password)) return PASSWORD_RULE_HINT
  return null
}
