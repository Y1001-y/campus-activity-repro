# 阶段文档 · 迭代 3：接口联调与权限（A08-A09 / D08）

## 【1】仓库信息

- 仓库：`campus-activity-repro`，分支 `feat/u1-repro`。
- 本轮覆盖提交 3 个（前端 A08-A09、D08）：

| 提交 | SHA | 内容 |
| --- | --- | --- |
| A08 扫码签到页 | d4b3b10 | `/pages/activity/checkin`，POST /api/checkin |
| A09 组织者工作台 | c35d854 | `/pages/organizer/index`，GET /api/admin/dashboard |
| D08 操作日志页 | cb737e4 | `/logs`，GET /api/admin/logs |

## 【2】联调记录表（≥8 条，真实接口）

> 后端真实运行在 8016，全部请求走统一请求层，`docs/api-contract.md` 可核对路径/方法/入参/出参。

| # | 接口路径 | 方法 | 入参 | 出参 data | 遇到的问题 | 解决 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | `/api/auth/login` | POST | admin/admin123 | `{token,user}` | 密码错返回 10007 | 前端区分提示"账号或密码错误" |
| 2 | `/api/auth/me` | GET | Bearer token | `User` | 无 token 返回 10001 | 拦截器清 token 跳登录 |
| 3 | `/api/activities` | GET | page=1&size=2 | `{list,total,page,size}` | 字段是下划线非驼峰 | 类型按真实响应修正 |
| 4 | `/api/activities/101/signup` | POST | id+token | `{signupId,...}` | 重复报名 30002 | 页面 toast 业务码文案 |
| 5 | `/api/checkin` | POST | `{activityId,code}`+token | 签到结果 | 错误码 40001 | 提示"刷新后再试" |
| 6 | `/api/me/signups` | GET | token | `Signup[]` | 字段与 admin 端不同 | Signup 用可选字段兼容 |
| 7 | `/api/admin/dashboard` | GET | token | 6 项真实统计 | 学生令牌 10002 | 前端隐藏入口 + 后端拦截 |
| 8 | `/api/admin/logs` | GET | token | `OpLog[]` | 需管理员权限 | 角色守卫 + requireManager |
| 9 | `/api/upload` | POST | multipart file | `{url,name,size}` | 格式/大小校验 | 前端先校验后端再校验 |
| 10 | `/api/admin/signups` | GET | activityId?+token | `Signup[]`+姓名学号 | 核销需 userId | 列表带 user_id 供核销 |

## 【3】主链路（5 步连续截图，时间戳递增）

登录 `/login` → 浏览活动 `/pages/activity/list` → 报名 `/pages/activity/signup` → 我的报名 `/pages/mine/signups` → 扫码签到 `/pages/activity/checkin`。每步截图带地址栏与时间戳，证明真实拼接非拼图。签到成功后后端写 `activity_checkin` 并记 `op_log`，可用 `/api/admin/dashboard` 的 `checkinCount` 变化佐证。

## 【4】错误处理证据（3 类业务码）

| 错误 | 触发方式 | 响应 | 前端处理代码片段位置 |
| --- | --- | --- | --- |
| 名额已满 30001 | 未报名学生报名活动 103（容量 2 已 2 人） | `{"code":30001,"message":"名额已满"}` | `app/src/api/request.ts` 的 `code!==0 → BizError` |
| 签到码过期 40001 | 签到页输入 WRONG-CODE | `{"code":40001,...}` | `app/src/pages/activity/checkin.vue` catch 分支 |
| 活动已结束/未开放 20005 | 报名 DRAFT 活动 104 | `{"code":20005,...}` | 报名页 catch 分支 |

对应真实 curl 输出见 `evidence/smoke-run.txt`（[7][8][11] 项）。

## 【5】权限证据（前端隐藏 + 后端校验）

- 前端：`admin/src/router/index.ts` 的 `meta.roles` 守卫 + `directives/permission.ts` 的 `v-permission` 移除无权限按钮 + 菜单过滤（`AdminLayout.vue`）。
- 后端：`AdminController` 每个方法先 `ApiSupport.requireManager(req)`，低权限令牌返回 10002（`AdminController.java` 第 21/35/42/63/78/85/95/106 行）。
- 越权测试：学生令牌（STUDENT）访问 `/api/admin/dashboard` → `code=10002`（`evidence/smoke-run.txt` [10]）。

## 【6】问题记录（≥3 条）

本轮联调中发现并解决的真实问题：BUG-08（chooseImage tempFiles 联合类型 TS7053）、BUG-09（冒烟用例选错学生导致业务码分支不符）、BUG-10（uni 插件 ESM 互操作导致 build:h5 失败）。完整现象→排查→原因→解决→预防与可复现指令见 `docs/issues.md`。

## 【7】AI 协作记录（≥2 次，含一次"把 Network 请求响应交给 AI 排障"）

1. **采纳**：把签到失败的 Network 响应（40001 JSON）贴给 AI 排障——AI 指出是签到码与 `activity.checkin_code` 不一致，建议提示用户刷新；采纳，因为后端 `StudentController.checkin` 确实先比对 `checkin_code`，建议有效。
2. **采纳**：AI 建议 admin 端 `v-permission` 指令在按钮级也做角色过滤——采纳，形成"菜单隐藏 + 按钮隐藏 + 后端 10002"三层，实测学生令牌即使绕过前端也被后端拦。
3. **否决**：AI 初版建议前端自行判断"名额是否已满"再决定是否禁用按钮——否决，因为容量是后端实时统计（`signed_up`），前端缓存判断会过期；改为后端 30001 为准、前端只展示响应。

## 【8】截图位置标注

- **S3-1 联调记录**：`docs/api-contract.md` 表格 + 对应浏览器 Network 面板（F12）选中一条 `/api/...` 请求，截 Headers+Response。
- **S3-2 主链路 5 张**：登录→浏览→报名→我的报名→签到，每张含地址栏和时间戳（按操作顺序截）。
- **S3-3 错误处理 3 张**：触发 30001/40001/20005 后页面 toast + Network 响应 JSON。
- **S3-4 权限 2 张**：学生手输 `/users` 跳 403（前端）；curl 学生令牌打后台接口返回 10002（终端）。
- **S3-5 AI 排障**：把 40001 Network 响应贴给 AI 的对话截图（你复现时贴一次即可）。
