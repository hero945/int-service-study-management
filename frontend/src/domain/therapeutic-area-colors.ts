/**
 * 治疗领域（TA）在管线总览等处的展示颜色映射。
 * 以色块/圆点形式区分不同 TA，未知编码回退到默认棕色。
 */

export const THERAPEUTIC_AREA_COLORS: Record<string, string> = {
  ONCOLOGY: '#d64545',
  AUTOIMMUNE: '#2c74e0',
  METABOLIC_CARDIOVASCULAR: '#27a159',
  RESPIRATORY: '#8a4bd8',
  INFECTIOUS_DISEASE: '#e08a2b',
  NEUROSCIENCE: '#0f8a9e',
}

/**
 * 将 areaCode 转为可用的 CSS 修饰类名。
 * 例如 ONCOLOGY → area-dot--oncology。
 */
export function areaDotClass(areaCode: string | undefined): string {
  if (!areaCode) return ''
  return `area-dot--${areaCode.toLowerCase().replace(/_/g, '-')}`
}

/**
 * 判断某个 areaCode 是否在已配置颜色映射中。
 */
export function hasAreaColor(areaCode: string | undefined): boolean {
  return !!areaCode && areaCode in THERAPEUTIC_AREA_COLORS
}
