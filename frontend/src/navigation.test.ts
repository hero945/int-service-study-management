import { describe, expect, it } from 'vitest'
import {
  firstAllowedHome,
  navigationGroups,
  visibleNavigationGroups,
} from './navigation'

describe('navigation', () => {
  it('keeps every primary destination in one grouped navigation contract', () => {
    expect(navigationGroups.map((group) => group.label)).toEqual([
      '研发工作',
      '月报运营',
      '平台管理',
    ])
    expect(navigationGroups.flatMap((group) => group.items.map((item) => item.name))).toEqual([
      'pipeline',
      'studies',
      'risks',
      'team',
      'reports',
      'config',
      'accounts',
      'roles',
    ])
  })

  it('filters destinations by permission and removes empty groups', () => {
    const groups = visibleNavigationGroups(['study.read', 'report.page.view'])

    expect(groups).toEqual([
      expect.objectContaining({
        label: '研发工作',
        items: [expect.objectContaining({ name: 'studies' })],
      }),
      expect.objectContaining({
        label: '月报运营',
        items: [expect.objectContaining({ name: 'reports' })],
      }),
    ])
  })

  it('chooses the first visible destination as the post-login home', () => {
    expect(firstAllowedHome(['risk.page.view', 'report.page.view'])).toBe('risks')
    expect(firstAllowedHome(['report.page.view'])).toBe('reports')
    expect(firstAllowedHome(['monthly.read'])).toBe('login')
    expect(firstAllowedHome([])).toBe('login')
  })
})
