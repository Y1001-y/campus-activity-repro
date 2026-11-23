# S02 部署资产说明

> 本机没有 Nginx / 云资源，以下为**真实可用的生产配置资产**（非 mock）；本机可访问性验证见 `docs/iteration-4.md` 的 vite preview 部分。

## 资产清单

| 文件 | 作用 |
| --- | --- |
| `nginx/campus-activity.conf` | admin/app 双静态站 + `/api`、`/uploads` 反代；SPA `try_files` 回退 + 静态缓存 |
| `systemd/campus-activity.service` | 后端 jar 开机自启、崩溃重启、端口与上传目录环境变量 |
| `yunxiao/pipeline.yml` | 云效 Flow：构建 → SCP 部署 → 冒烟校验三阶段 |

## ECS 部署步骤（真实命令）

```bash
# 1. 后端：构建 jar 并交给 systemd
mvn -f server/pom.xml -DskipTests package
sudo mkdir -p /opt/campus-activity/server/target /opt/campus-activity/uploads
sudo cp server/target/campus-activity-0.0.1-SNAPSHOT.jar /opt/campus-activity/server/target/
sudo cp deploy/systemd/campus-activity.service /etc/systemd/system/
sudo systemctl daemon-reload && sudo systemctl enable --now campus-activity

# 2. 前端：构建双端静态
cd admin && pnpm install --frozen-lockfile && pnpm build && cd ..
cd app && pnpm install --frozen-lockfile && pnpm build:h5 && cd ..
sudo mkdir -p /var/www/campus-activity
sudo cp -r admin/dist /var/www/campus-activity/admin/
sudo cp -r app/dist/build/h5 /var/www/campus-activity/app/dist/build/

# 3. Nginx：装载站点并校验语法
sudo cp deploy/nginx/campus-activity.conf /etc/nginx/conf.d/campus-activity.conf
sudo nginx -t && sudo nginx -s reload

# 4. 冒烟：18 项应全 PASS
BASE=http://admin.campus.example.com bash scripts/smoke.sh
```

## 回滚

- 应用回滚：`sudo cp <上一版jar> ... && sudo systemctl restart campus-activity`；静态回滚：切回上一版 dist 目录。
- 配置回滚：`sudo cp <备份conf> /etc/nginx/conf.d/campus-activity.conf && sudo nginx -s reload`。
- 数据库：H2 内存库随进程重启由 `schema.sql`/`data.sql` 重建，无需单独回滚。
