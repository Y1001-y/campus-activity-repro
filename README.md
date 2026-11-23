# campus-activity-repro

校园活动管理平台三端复现仓库。按课程「项目迭代 1-4」的覆盖提交顺序，从空仓库逐步实现。

> 本文件在 S03 提交中补全「环境要求 / 启动命令 / 构建命令 / 部署步骤 / 回滚方式」五要素。

## 三端

| 目录 | 技术栈 | 端口 / 命令 |
| --- | --- | --- |
| `server/` | Spring Boot 3.3 + Java 21 + H2 | `8016`，`mvn -f server/pom.xml spring-boot:run` |
| `admin/` | Vite + Vue3 + TypeScript + Pinia | `5173`，`cd admin && pnpm dev` |
| `app/` | uni-app + Vue3 + TypeScript | H5，`cd app && pnpm dev:h5` |

## 分支

- `main`：S00 初始化基线
- `feat/u1-repro`：四轮迭代的全部能力提交

## 提交约定

Conventional Commits，形如 `feat(admin): 类型定义、请求层与字典`；范围限定 `server/app/admin/scripts/deploy/docs`。
每个「覆盖提交」对应一次独立提交，阶段文档单独以 `docs(uN)` 提交。
