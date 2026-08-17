# Plan: Explore milestone detail pages

1. Identify Vue views: `MilestoneView.vue` and `ProjectMilestoneView.vue`.
2. Inspect API types for `MilestoneNode`, `StageGroup`, `MilestonePage`, `ProjectMilestonePage`.
3. Trace grouping/filtering helpers:
   - `useMilestoneStageFocus` + `MilestoneStageNav` for stage navigation.
   - `ProjectMilestoneView` filters groups via `regulatoryStageCodes`.
   - `milestone-status.ts` flattens groups and derives completion flags.
   - `milestone-filters.ts` maps stage codes to sub-status labels.
   - `pipeline-aggregation.ts` classifies regulatory vs clinical phases.
4. Extract route definitions from `router.ts`.
5. Review related tests in `*.test.ts` domain files.
6. Summarize findings with paths, names, and snippets.
