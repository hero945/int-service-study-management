/**
 * 治疗领域（TA）的唯一真相源：筛选选项、展示颜色、CSS 类名。
 * 未知编码回退到默认棕色（.area-dot）。
 */

/** TA 中文名筛选选项（Study 列表 / 管线总览共用） */
export const TA_OPTIONS = ['肿瘤', '自身免疫', '代谢与心血管', '呼吸系统', '感染性疾病', '神经科学'] as const

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
