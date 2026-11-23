# 阶段文档 · 迭代 2：组件化与状态管理

## 【1】仓库信息

- 仓库：`campus-activity-repro`，分支 `feat/u1-repro`。
- 本轮覆盖提交 10 个（前端 A03-A07、D03-D07，SHA 前 7 位）：

| 提交 | SHA | 内容 |
| --- | --- | --- |
| A03 首页（分类/热门/即将开始） | 5048243 | 首页 + 三个入口页 |
| A04 活动列表与详情页 | f3f382a | 列表/详情 + ActivityCard 复用 |
| A05 报名页 | 40f7d8a | 报名 + 业务码提示 |
| A06 学号绑定与验证码登录页 | 944d5f2 | bind + login |
| A07 我的、我的报名与消息页 | 0c96e0b | 三个个人中心页 |
| D03 登录页与路由骨架 | e238976 | LoginView + router meta 守卫 |
| D04 布局与工作台 | 5796b4a | AdminLayout + Dashboard |
| D05 活动审核与详情抽屉 | 3dc0350 | AuditView + ActivityDrawer |
| D06 活动管理页 | f128ee8 | ActivitiesView + DataTable |
| D07 用户管理页 | 102f5c3 | UsersView |

> 本轮后有一次类型检查修复 `fix: 修正迭代2 类型检查发现的组件路径与路由表语法`（1555890），属 BUG-06/07。

## 【2】复现说明（真实构建过程）

本轮按 A03→A07、D03→D07 顺序，每个页面/能力单独一次规范提交。app 端 5 个页面覆盖「首页分类/热门/即将开始 → 活动列表/详情 → 报名 → 学号绑定/验证码登录 → 我的/我的报名/消息」，全部通过 `api.modules` 调真实后端（迭代 1 已在 8016 运行），不写死数据。admin 端 5 个页面覆盖「登录 → 布局工作台 → 活动审核详情抽屉 → 活动管理 → 用户管理」，用 Pinia `useUserStore` 管理登录态，刷新后从 localStorage 恢复，退出登录清空。组件化方面抽出 **3 个被 ≥2 处复用的业务组件**：`ActivityCard.vue`（首页热门、列表、详情推荐共用）、`DataTable.vue`（活动管理、用户管理、操作日志共用）、`StatusTag.vue`（活动列表、审核、管理共用），props/emits 接口清晰。路由分层为「公开页（login/403/404）+ 布局路由 + 业务子路由」，`beforeEach` 守卫：未登录访问受保护路由带 redirect 跳登录，`meta.roles` 不匹配跳 403。每加一页即跑 `pnpm type-check`，BUG-06（组件相对路径 ../../ 写错）和 BUG-07（children 漏逗号）都在本轮被 type-check 拦下并修复（正文约 460 字）。

## 【3】组件清单（≥3 个被 ≥2 处使用）

| 组件 | 职责 | props / emits / 插槽 | 使用位置 |
| --- | --- | --- | --- |
| `ActivityCard.vue`（app） | 活动卡片展示 | props: `activity`；emit: `tap` | 首页热门、活动列表、即将开始（≥3 处） |
| `DataTable.vue`（admin） | 通用表格 | props: `columns/rows`；slot: 空态 | 活动管理、用户管理、操作日志（≥3 处） |
| `StatusTag.vue`（admin） | 状态徽标 | props: `status` | 活动列表、审核、活动管理（≥3 处） |
| `PageHeader.vue`（admin） | 页头 + 操作区 | slot: `actions` | 全部 admin 业务页 |

## 【4】路由表设计与守卫

- app：`pages.json` 声明 14 个页面路由（首页 → 分类/热门/即将/列表/详情/报名/签到 → 登录/绑定 → 我的/报名/消息 → 组织者工作台）。
- admin：`router/index.ts` 分 `public`（login/403/404）、`/` 布局 + children（dashboard/audit/activities/users/logs/materials）、catch-all 404。
- 守卫：`beforeEach` 里 `meta.public` 放行 → 无 token 跳 `/login?redirect=` → `meta.roles` 不匹配跳 `/403?from=`。低权限手输高权限 URL 被拦截（迭代 3 实测截图）。

## 【5】页面可访问性（后台 5 页 + 小程序 5 页）

- 后台（浏览器 `http://127.0.0.1:5173`，admin/admin123 登录）：`/` 工作台、`/audit` 审核、`/activities` 活动管理、`/users` 用户管理、`/logs` 操作日志，均真实可访问、数据来自 8016。
- 小程序 H5（浏览器 `http://127.0.0.1:5174/#/pages/...`）：首页、活动列表、报名、登录、我的，均真实可访问（截图含地址栏，见 screenshots.md）。

## 【6】问题记录（≥3 条）

BUG-06（ActivityCard 相对路径 ../../ 错，TS2307）、BUG-07（router children 漏逗号，TS1005），加上构建期的配置问题，详见 `docs/issues.md` 与 `evidence/bug-06-07-typecheck.txt`。

## 【7】AI 协作记录（≥2 次）

1. **采纳**：AI 建议抽 `DataTable` 统一三张表的列渲染——采纳，减少重复且 props 清晰，被 3 页复用。
2. **否决**：AI 初版把路由守卫写在每个页面 `onMounted`——否决，改为 `beforeEach` 集中守卫，避免遗漏且能统一 redirect/from 参数。
3. **采纳**：AI 提出"每加一页立即 type-check"——采纳，直接发现了 BUG-06/07。

## 【8】截图位置标注

- **S2-1 组件复用证据**：VS Code 全局搜索 `ActivityCard` 的引用，截 3 处 import。
- **S2-2 路由表**：`admin/src/router/index.ts` 编辑器截图，框选 children 数组与 beforeEach 守卫。
- **S2-3 后台 5 页**：逐页浏览器截图，URL 栏含 `127.0.0.1:5173`（工作台/审核/活动/用户/日志各 1 张）。
- **S2-4 小程序 5 页**：逐页 H5 浏览器截图，URL 栏含 `5174/#/pages/...`（首页/列表/报名/登录/我的）。
- **S2-5 403 拦截**：用学生token手输 `/users`，截跳 `/403?from=/users` 地址栏。
