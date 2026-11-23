# 总教程：一键复现 + 手动截图指南

> 代码、构建、冒烟我已全部真实跑完（29 次规范提交，PASS=18 FAIL=0）。本教程教你如何在自己电脑上复现每一步，并在标注位置手动截图交作业。

## 第一部分：环境准备（一次性）

1. 确认版本：`git --version`（2.54.0）、`java -version`（21）、`mvn -v`、`node -v`（22）、`pnpm -v`（12）、`python --version`（3.13）。
2. 进入仓库：`cd C:/Users/17442/WorkBuddy/2026-11-23-09-56-10/campus-activity-repro`。
3. 确认分支：`git branch` 应看到 `* feat/u1-repro`。

## 第二部分：复现指令（按顺序执行）

### 步骤 1：启动后端（终端 A）

```bash
mvn -f server/pom.xml spring-boot:run
# 看到 "Tomcat started on port 8016" 即成功
```
另开终端验证：`curl http://127.0.0.1:8016/api/ping` → 应返回 `{"code":0,...,"status":"up"}`。

### 步骤 2：启动管理后台（终端 B）

```bash
cd admin
pnpm install          # 若报 ERR_PNPM_IGNORED_BUILDS，见 docs/issues.md BUG-01/04/05
pnpm dev              # 打开 http://127.0.0.1:5173
```
登录：`admin` / `admin123`（或 `organizer` / `org123`）。

### 步骤 3：启动小程序 H5（终端 C）

```bash
cd app
pnpm install
pnpm dev:h5           # 打开 http://127.0.0.1:5174
```
学生登录：`2024001001` / `stu123`。

### 步骤 4：双端构建验证

```bash
cd admin && pnpm type-check && pnpm build
cd ../app && pnpm type-check && pnpm build:h5
```

### 步骤 5：冒烟验证

```bash
bash scripts/smoke.sh     # 期望底部：结果：PASS=18 FAIL=0
```

### 步骤 6：复现中间 bug（可选，证明问题真实）

```bash
# BUG-01 依赖拦截：从无 allowBuilds 的配置重装
cd app && cp pnpm-workspace.yaml /tmp/w.bak && printf 'packages:\n  - .\n' > pnpm-workspace.yaml && pnpm install --no-frozen-lockfile && cp /tmp/w.bak pnpm-workspace.yaml
# BUG-09 冒烟选错学生：把 smoke.sh 第 11 项 FTOKEN 改回 STOKEN 后 bash scripts/smoke.sh（会 FAIL=1）
# BUG-10 ESM 互操作：把 vite.config.ts 改回 import uni from...; plugins:[uni()] 后 pnpm build:h5（报 uni is not a function）
```
> 每个 bug 的完整输出在 `evidence/bug-*.txt`，改回正确状态后重新跑一遍即可恢复。

## 第三部分：截图位置标注汇总（你手动截）

> 通用要求：浏览器截图**保留地址栏**（证明真实访问），主链路截图**带时间戳**且顺序递增。

| 编号 | 截什么 | 在哪/怎么操作 | 归档建议命名 |
| --- | --- | --- | --- |
| S1-1 | git 提交历史 | 终端 `git log --oneline`，框选提交 | `s1-1-commits.png` |
| S1-2 | 三端目录树 | VS Code 展开 server/app/admin/src | `s1-2-tree.png` |
| S1-3 | 后端启动 | 终端 `mvn spring-boot:run` 输出 | `s1-3-server.png` |
| S1-4 | ping 响应 | 浏览器开 `127.0.0.1:8016/api/ping` | `s1-4-ping.png` |
| S2-2 | 路由表 | 编辑器打开 `admin/src/router/index.ts` | `s2-2-router.png` |
| S2-3 | 后台 5 页 | 浏览器逐页 5173（工作台/审核/活动/用户/日志） | `s2-3-admin-*.png` |
| S2-4 | 小程序 5 页 | 浏览器逐页 5174（首页/列表/报名/登录/我的） | `s2-4-h5-*.png` |
| S2-5 | 403 拦截 | 学生token手输 `/users` | `s2-5-403.png` |
| S3-1 | Network 请求 | F12 选一条 `/api/...`，截 Headers+Response | `s3-1-network.png` |
| S3-2 | 主链路 5 步 | 登录→浏览→报名→我的报名→签到（时间递增） | `s3-2-flow-*.png` |
| S3-3 | 错误处理 3 类 | 触发 30001/40001/20005 的页面提示+响应 | `s3-3-error-*.png` |
| S3-4 | 权限证据 | 403 页面 + curl 10002 终端 | `s3-4-perm-*.png` |
| S4-1 | 构建日志 | 两次 build 终端，框选体积行 | `s4-1-build-*.png` |
| S4-2 | dist 目录 | 资源管理器展开两个 dist | `s4-2-dist.png` |
| S4-3 | 冒烟输出 | `bash scripts/smoke.sh` 全屏 | `s4-3-smoke.png` |
| S4-4 | 部署片段 | Nginx 的 try_files/expires 段 | `s4-4-nginx.png` |
| S4-5 | preview 子路由 | `pnpm preview` 后开 `:4173/users` 刷新 | `s4-5-preview.png` |
| S4-6 | README | README 五要素章节 | `s4-6-readme.png` |

## 第四部分：提交作业

- 阶段文档：`docs/iteration-1.md` ~ `iteration-4.md`（已写好，按轮提交）。
- 问题记录：`docs/issues.md` + `evidence/bug-*.txt`。
- 总教程：本文件 `docs/tutorial.md`。
- 把以上截图按编号放进 `screenshots/` 目录（你需要时自行创建），随作业一并提交。

## 附：本仓库提交约定

Conventional Commits（feat/fix/docs/chore...），每个覆盖提交一次独立提交，文档单独 `docs(uN)` 提交。当前 29 次提交，全部规范（`git log --oneline` 可核对）。远程推送：本机 `gh` 不可用，如需 GitHub 远程，见文末说明在自己电脑执行两条命令。
