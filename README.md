# campus-activity-repro

校园活动管理平台三端复现仓库。按课程「项目迭代 1-4」的覆盖提交顺序，从空仓库逐步实现。

> 本文件为 **S03** 交付：补全「环境要求 / 启动命令 / 构建命令 / 部署步骤 / 回滚方式」五要素 +
> 后台说明 + 技术取舍 + 答辩材料。所有命令均在本机真实执行，输出见 `evidence/`。

## 一、环境要求（真实版本）

| 依赖 | 本机实测版本 | 说明 |
| --- | --- | --- |
| Git | 2.54.0.windows.1 | `git --version` |
| JDK | Java 21.0.12.1 | `java -version`，Spring Boot 3.3.5 要求 17+ |
| Maven | 3.x | `mvn -v`，依赖已在本地 ~/.m2 缓存 |
| Node | 22.22.2 | `node -v` |
| pnpm | 12.x | `pnpm -v`，需 `pnpm-workspace.yaml` 的 `allowBuilds` |
| curl / python3 | curl 8.22 / Python 3.13 | 冒烟脚本依赖 |

## 二、启动命令（三端可独立启动）

```bash
# 1) 后端：H2 内存库随启动由 schema.sql + data.sql 灌入真实种子数据，端口 8016
mvn -f server/pom.xml spring-boot:run
#   或 java -jar server/target/campus-activity-0.0.1-SNAPSHOT.jar
curl http://127.0.0.1:8016/api/ping   # 期望 {"code":0,...,"status":"up"}

# 2) 管理后台：http://127.0.0.1:5173（代理 /api、/uploads → 8016）
cd admin && pnpm install && pnpm dev

# 3) 小程序 H5：http://127.0.0.1:5174（代理同上）
cd app && pnpm install && pnpm dev:h5
```

默认种子账号（真实写在 `server/src/main/resources/data.sql`）：

| 账号 | 密码 | 角色 | 用途 |
| --- | --- | --- | --- |
| admin | admin123 | ADMIN | 后台全部菜单 + 审核/核销 |
| organizer | org123 | ORGANIZER | 发布活动、素材上传、报名核销 |
| 2024001001 | stu123 | STUDENT | 学生报名/签到（已报名 101/102/103） |
| 2024001003 | stu123 | STUDENT | 干净学生，用于复现「名额已满 30001」 |

## 三、构建命令（真实通过，产物可查）

```bash
cd admin && pnpm type-check && pnpm build      # 133 modules，1.14s，dist/
cd app   && pnpm type-check && pnpm build:h5   # DONE Build complete，dist/build/h5/
bash scripts/smoke.sh                          # PASS=18 FAIL=0，退出码 0
python scripts/icon-gen.py scripts/output/app-icon.png  # 真实生成 664 字节 PNG
```

构建日志：`evidence/build-admin.txt`、`evidence/build-app-h5.txt`；冒烟输出：`evidence/smoke-run.txt`。

## 四、部署步骤

本机无 Nginx / 云资源，采用**两档**方案，均为真实配置：

1. **本机可访问验证**：`cd admin && pnpm preview`（127.0.0.1:4173），浏览器刷新 `/users`、`/logs` 等子路由不 404（Vite preview 自带 SPA fallback，等价验证 Nginx `try_files`）。
2. **生产 ECS 部署**：见 `deploy/README.md` 的真实命令（systemd 起 jar → Nginx 装载 `deploy/nginx/campus-activity.conf` → 云效 `deploy/yunxiao/pipeline.yml` 构建/部署/冒烟）。Nginx 配置含 `try_files $uri $uri/ /index.html` 子路由回退，静态资源 `expires 30d` + `immutable` 缓存。

## 五、回滚方式

- 后端：`systemctl restart campus-activity` 切回上一版 jar（源码在 `server/target/`，systemd 单元见 deploy）。
- 前端：Nginx `root` 指回上一版 dist 目录，`nginx -s reload`。
- 数据：H2 内存库无状态，重启即从 `schema.sql`/`data.sql` 重建，不需单独回滚。

## 六、后台与接口说明

- 统一响应：`{code, message, data}`，`code=0` 成功；业务码表见 `server/.../common/ResultCode.java`（10001 未登录 / 10002 无权 / 30001 满员 / 40001 签到码错 等）。
- 完整接口清单与入参出参：`docs/api-contract.md`（与前端 `api/types.ts` 字段一一对应）。
- 三层权限：路由 `meta.roles` 守卫 → 菜单/按钮过滤（`v-permission`）→ 后端 `requireManager` 返回 10002。

## 七、技术取舍（≥2 处，讲清为什么）

1. **H2 内存库而非 MySQL/Redis**：后端课程负责持久化/缓存/Token 下发能力，本前端轮次只需稳定可复现的联调后端。H2 随进程启动由 SQL 重建，保证「换台机器按 README 一条命令就能跑」，避免 mock 与数据不一致；Redis 缓存、短信网关在 `AuthController` 用本机自签验证码 + `ConcurrentHashMap` 等效替代并标注，不伪装成真实短信。
2. **uni-app H5 用 Vite 代理同源 `/api` 而非硬编码域名**：`app/src/api/request.ts` 在 H5 下 `BASE_URL='/api'`（走 5174 proxy），非 H5 端才用 `http://127.0.0.1:8016/api`；这样浏览器端无跨域、部署时由 Nginx `/api` 反代统一收口，页面内不出现后端域名。
3. **CJS/ESM 互操作修复**（`app/vite.config.ts`）：`@dcloudio/vite-plugin-uni` 是 CJS 包，ESM 加载配置时 default 是对象，做 `typeof` 归一化后构建成功（见 bug-10）。

## 八、答辩材料（3 分钟演示脚本 + 架构图 + 问题记录）

- 演示脚本与架构图：`docs/defense.md`。
- 四轮阶段文档：`docs/iteration-1.md` ~ `docs/iteration-4.md`（含仓库信息/复现说明/检查点/问题记录/AI 协作/截图位置）。
- 全部问题记录（10 个真实 bug + 复现指令）：`docs/issues.md` 与 `evidence/bug-*.txt`。
- 截图位置标注清单：`docs/screenshots.md`（每阶段应截什么、在哪截）。

## 分支与提交

- 远程：`https://github.com/Y1001-y/campus-activity-repro`（`origin` 已配置，两分支均已推送并设 upstream）。
- `main`：S00 基线；`feat/u1-repro`：四轮全部覆盖提交。
- Conventional Commits；每个覆盖提交一次独立提交，阶段文档单独以 `docs(uN)` 提交。
