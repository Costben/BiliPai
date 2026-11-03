# 只保留 COUI 17，冻结 Material 3 与 Miuix

## Status

accepted

## Context

仓库同时存在 Material 3、Miuix、COUI 17 三套皮肤。COUI 17 已是默认方向，但旧皮肤仍在设置里可选，`App*` 门面因此保留完整的三路分发，页面里也积累了大量 `LocalAppUiStyle` 分支和 `MaterialTheme` 令牌旁路。结果是 COUI 17 的观感被旧皮肤的默认值稀释，迁移成本随页面数量增长。

## Decision

新用户默认 COUI 17；设置只暴露 COUI 17；旧存档里的 Material 3 / Miuix 选择单向迁移为 COUI 17。`AppUiStyle` 的旧枚举与 `material3` 渲染器暂时保留但冻结：不新增适配、不做视觉优化、不再作为验收对象。彻底删除留到页面改造完成后的独立重构。

## Consequences

- Material 3 与 Miuix 用户不再获得观感保证；他们的选择会在升级时被改写。
- 页面改造可以直接使用 COUI 令牌与 COUI 原生组件，不必再为旧皮肤保留分支。
- `NativeThemeMigrationBoundaryTest` 需要先修正语义：COUI 的 `basic` / `preference` import 是迁移目标，不应计入旧皮肤债务。
- 旧枚举与渲染器会在一段时间内成为死代码，增加阅读成本，直到独立重构删除。
