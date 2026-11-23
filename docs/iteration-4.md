# 阶段文档 · 迭代 4：多端适配与构建发布

## 【1】仓库信息

- 仓库：`campus-activity-repro`，分支 `feat/u1-repro`。
- 本轮覆盖提交 6 个（A10-A11、D09、S01-S03），加 1 个构建修复：

| 提交 | SHA | 内容 |
| --- | --- | --- |
| A10 发布活动页（含封面上传） | a500029 | `/pages/activity/publish`，POST /api/admin/activities + /api/upload |
| A11 报名名单与签到核销 | 5e7531a | `/pages/activity/signup-list`，admin/signups + checkin/verify |
| D09 素材上传页 | 3b9b932 | `/materials`，格式大小双端校验 |
| S01 冒烟与图标脚本 | ad9b968 | scripts/smoke.sh + icon-gen.py + 真实输出 |
| S02 部署资产 | 966c254 | Nginx/systemd/云效 YAML |
| S03 README 与答辩材料 | 0029df3 | README 五要素 + 技术取舍 |
| fix(app) ESM 互操作 | da8999f | 使 build:h5 成功（BUG-10） |

## 【2】构建证据（两次构建真实成功）

- `cd admin && pnpm type-check && pnpm build` → `✓ 133 modules transformed`，`✓ built in 1.14s`，产物 `admin/dist/`（index.html + 分包 JS/CSS，最大 chunk `index-DFP6ioSt.js` 104.91 kB / gzip 40.98 kB）。日志：`evidence/build-admin.txt`。
- `cd app && pnpm type-check && pnpm build:h5` → `DONE Build complete.`，产物 `app/dist/build/h5/`。日志：`evidence/build-app-h5.txt`（修复 BUG-10 后）。
- 图标：`python scripts/icon-gen.py` → 真实 PNG 664 字节 `scripts/output/app-icon.png`。

## 【3】冒烟证据

`bash scripts/smoke.sh` → **PASS=18 FAIL=0，退出码 0**（`evidence/smoke-run.txt`）。覆盖 12 组接口、含 5 类业务码分支（30002/40001/10001/10002/30001）。中途 BUG-09（PASS=17 FAIL=1）已记录并修正，见 issues.md。

## 【4】部署证据

- 本机无 Nginx：以 `cd admin && pnpm preview`（127.0.0.1:4173）验证静态站可访问、子路由刷新不 404（Vite preview 自带 SPA fallback）。
- 生产配置：`deploy/nginx/campus-activity.conf` 含 `try_files $uri $uri/ /index.html`（子路由回退）+ `expires 30d`+`immutable`（静态缓存）+ `/api`、`/uploads` 反代 8016；`deploy/systemd/campus-activity.service` 起 jar；`deploy/yunxiao/pipeline.yml` 构建→部署→冒烟三阶段。
- 部署地址（真实 ECS 需你提供）：占位域名 `admin.campus.example.com` / `h5.campus.example.com`，配置已就位但本机无云资源未实际发布（不谎称上线，见 README 技术取舍）。

## 【5】README 五要素

环境要求 / 启动命令 / 构建命令 / 部署步骤 / 回滚方式，全部在 `README.md` 第一~五节，命令均本机实测。

## 【6】答辩材料

见 `docs/defense.md`：3 分钟演示脚本 + 架构图（Mermaid）+ 三处技术取舍说明。

## 【7】问题记录与 AI 协作

本轮问题记录：BUG-08/09/10（见 `docs/issues.md`）；AI 协作 ≥2 次见 `docs/iteration-3.md` 与各阶段文档。

## 【8】截图位置标注

- **S4-1 构建日志**：两次构建的终端输出，框选 `✓ built in` 与 `DONE Build complete.` + 体积行（含 chunk 体积）。
- **S4-2 dist 目录**：资源管理器展开 `admin/dist/` 与 `app/dist/build/h5/`，截文件列表。
- **S4-3 冒烟输出**：`bash scripts/smoke.sh` 全屏输出，框选底部 `结果：PASS=18 FAIL=0`。
- **S4-4 部署片段**：`deploy/nginx/campus-activity.conf` 里 `try_files` 与 `expires 30d` 两段。
- **S4-5 preview 子路由**：`pnpm preview` 后浏览器打开 `http://127.0.0.1:4173/users` 刷新，截地址栏 + 页面（不 404）。
- **S4-6 README**：`README.md` 五要素章节编辑器截图。
