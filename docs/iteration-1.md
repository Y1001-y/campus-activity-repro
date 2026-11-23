# 阶段文档 · 迭代 1：工程基座复现（D01-D02 / A01-A02）

## 【1】仓库信息

- 仓库地址：GitHub 远程 `https://github.com/Y1001-y/campus-activity-repro`；本地工作副本 `C:/Users/17442/WorkBuddy/2026-11-23-09-56-10/campus-activity-repro`。远程由 GitHub REST API（凭据管理器 token）创建，`main` 与 `feat/u1-repro` 均已推送。
- 分支：`feat/u1-repro`（从 `main` 的 S00 基线切出）。
- 本轮覆盖提交 7 个（顺序即实现顺序，`git log --oneline` 可核对，SHA 取前 7 位）：

| 序号 | 提交 | SHA | 内容 |
| --- | --- | --- | --- |
| S00 | `chore(repo): 初始化从零构建工作区` | abd11b6 | 空仓库 + .gitignore + README 骨架 |
| B01 | `feat(server): 初始化 Spring Boot 工程` | ce3bc12 | pom + 启动类 + application.yml |
| B02 | `feat(server): 统一响应体与全局异常` | 8ad7b53 | Result/ResultCode/BizException/全局异常处理 |
| A01 | `feat(app): 初始化 uni-app + Vue3 + TS 工程` | d8bdbd8 | pages.json + manifest + 基座 |
| A02 | `feat(app): 类型定义与请求层` | c18e89e | types.ts + request.ts + modules.ts |
| D01 | `feat(admin): 初始化 Vite + Vue3 + TS 工程` | 92528c5 | vite 配置 + 基座 + tsconfig |
| D02 | `feat(admin): 类型定义、请求层与字典` | 314fb7b | axios 实例 + 拦截器 + 字典 |

> D02 后有一次配置修复 `fix(admin): 修正 pnpm 构建脚本放行配置的重复键`（61b7cb6），属 BUG-05，见 `docs/issues.md`。

## 【2】复现说明（真实构建过程）

从空目录 `git init` 开始，按 S00→B01→B02→A01→A02→D01→D02 逐步提交，每步单独一次 Conventional Commit。后端用 Spring Boot 3.3.5 + Java 21 + H2 内存库，`schema.sql`/`data.sql` 在启动时灌入 5 用户/6 分类/4 活动的真实种子数据（非 mock 接口，数据落 H2，可用 SQL 查）。B02 定义统一响应体 `Result<T>(code,message,data)` 与 `ResultCode` 业务码枚举，`GlobalExceptionHandler` 把 `BizException` 收敛为业务码响应，避免各 Controller 各写一套错误格式。A01/D01 分别用 uni-app 和 Vite+Vue3+TS 建基座：uni-app 端 `pages.json` 声明页面路由，Vite 端 `vue-router` 配 history 路由；两端 `vite.config.ts` 都把 `/api`、`/uploads` 代理到 `http://127.0.0.1:8016`，页面内不硬编码后端域名。A02/D02 封装请求层：app 端用 `uni.request` 包 Promise，admin 端用 axios 实例 + 请求/响应拦截器，统一在 `code===0` 时 resolve、`code!==0` 抛 `BizError`，并处理 10001 未登录跳登录。TS 类型（`Activity/Category/User/Signup/Dashboard` 等）严格对齐后端真实响应字段——注意 JdbcTemplate 返回下划线列名、Controller 手拼对象返回驼峰，类型里两者都保留。全部安装/类型检查真实执行，日志见 `evidence/install-*.log`、`app-typecheck*.log`、`admin-typecheck*.log`（字数：本说明正文约 480 字）。

## 【3】检查点命令与真实结果（≥3 条）

| # | 命令 | 真实结果 |
| --- | --- | --- |
| C1 | `mvn -f server/pom.xml spring-boot:run` | Tomcat started on port 8016，`Started ... in 2.093s`（`evidence/server-run.log`） |
| C1 | `curl http://127.0.0.1:8016/api/ping` | `{"code":0,"data":{"service":"campus-activity","status":"up"}}`（`evidence/api-checks.txt`） |
| 启动 | `cd admin && pnpm install && pnpm type-check` | 放行 `esbuild/vue-demi` 后退出码 0（`evidence/admin-typecheck-fixed3.log`） |
| 启动 | `cd app && pnpm install && pnpm type-check` | 放行 `core-js/core-js-pure/esbuild` 后退出码 0（`evidence/app-typecheck-fixed3.log`） |

## 【4】问题记录（≥2 条，现象→排查→原因→解决→预防）

本轮共 5 个真实问题（BUG-01~05），完整复现指令与原始输出见 `docs/issues.md` 与 `evidence/bug-01*.txt` ~ `bug-05*.txt`。摘要：

1. **BUG-01** pnpm 12 拦 `core-js/esbuild` 构建脚本 → `ERR_PNPM_IGNORED_BUILDS` → 在 `app/pnpm-workspace.yaml` 的 `allowBuilds` 显式放行 → 重装退出码 0。
2. **BUG-02** YAML 换行写成字面 `/n` → 解析报 `mapping values are not allowed` → 改真实换行。
3. **BUG-03** uni 条件编译下两个 `const BASE_URL` 被 vue-tsc 判重复声明 TS2451 → 改单 `let` 声明 + 条件赋值。
4. **BUG-04** admin 漏放行 `vue-demi` postinstall → 同类错误 → 以报错列表为准逐项放行。
5. **BUG-05** pnpm 自动追加占位行造成 YAML 重复键 → 整体重写干净配置。

## 【5】AI 协作记录（≥2 次，含采纳/否决理由）

1. **采纳**：AI 建议"请求层统一出口 `http()`/`request()`，页面内不写 axios/uni.request"——采纳，因为迭代 3 权限收口和 10001 跳登录都依赖这一层，实测 10001 分支生效。
2. **否决**：AI 初版把 `Activity` 字段全写成驼峰 `categoryName/startTime`——否决，因为实测 `/api/activities` 走 JdbcTemplate 返回 `category_id/start_time` 下划线，已按真实响应改为下划线为主、驼峰可选兼容。
3. **采纳**：AI 提出"把 type-check 纳入每次提交前检查"——采纳，迭代 2 的 BUG-06/07 正是靠 type-check 提前发现。

## 【6】截图位置标注（你手动截图）

> 复现方式见 `docs/tutorial.md`；以下标注每阶段应截的画面与位置。

- **S1-1 仓库与提交**：`git log --oneline` 终端窗口，框选本轮 7+1 条提交，含 SHA 与 Conventional Commit message。
- **S1-2 三端目录树**：VS Code 资源管理器展开 `server/`、`app/src/`、`admin/src/`，截三端并存目录。
- **S1-3 后端启动**：终端 `mvn spring-boot:run` 输出 `Tomcat started on port 8016`。
- **S1-4 ping 响应**：浏览器或终端打开 `http://127.0.0.1:8016/api/ping`，截 JSON 响应（含地址栏）。
- **S1-5 git diff 差异**：`git diff <起始SHA> <结束SHA>` 终端输出，说明与参考实现的差异。
