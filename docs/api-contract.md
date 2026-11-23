# 接口契约文档（与真实后端代码一致）

> 来源：逐行读取 `server/src/main/java/com/campus/activity/controller/*.java` 与 `common/ResultCode.java`。
> 所有接口统一响应 `{code:int, message:string, data:T}`，`code=0` 成功。前端 `app/src/api/types.ts`、`admin/src/api/types.ts` 字段与之对应。
> 联调基线：`http://127.0.0.1:8016`，可由 `BASE` 环境变量替换。

## 通用

- 认证：登录后把 `data.token` 放请求头 `Authorization: Bearer <token>`。
- 错误分支：`code!==0` 时前端统一 `BizError(code, message)`；`10001` 清 token 跳登录。

## 接口清单（真实）

| 方法 | 路径 | 入参 | 出参 data | 权限 | 业务码 |
| --- | --- | --- | --- | --- | --- |
| GET | `/api/ping` | - | `{service,status}` | 公开 | - |
| POST | `/api/auth/login` | `{username,password}` | `{token,user}` | 公开 | 10007 密码错 |
| POST | `/api/auth/logout` | header token | void | 登录 | - |
| GET | `/api/auth/me` | header token | `User` | 登录 | 10001 |
| POST | `/api/auth/sms/send` | `{phone}` | `{phone,code,expiresIn,note}` | 公开 | 50001 手机号格式 |
| POST | `/api/auth/bind` | `{studentNo,phone,code}` | `User` | 公开 | 40001 码错 / 10004 学号不存在 / 10005 已绑定 |
| GET | `/api/categories` | - | `Category[]`（6 条） | 公开 | - |
| GET | `/api/activities` | `categoryId,keyword,status,page,size` | `{list,total,page,size}` | 公开 | - |
| GET | `/api/activities/{id}` | path id | `Activity`+`signedUp` | 公开 | 20001 不存在 |
| POST | `/api/activities/{id}/signup` | path id + token | `{signupId,activityId,status}` | 登录 | 20005 未开放 / 30002 重复 / 30001 满员 |
| GET | `/api/me/signups` | token | `Signup[]` | 登录 | 10001 |
| DELETE | `/api/signups/{id}` | path id + token | void | 登录 | 30003 不存在 / 30006 状态非法 |
| POST | `/api/checkin` | `{activityId,code}` + token | `{activityId,title,checkedInAt,message}` | 登录 | 40001 码错 / 40002 未报名 / 40003 重复 / 40004 非签到时段 |
| GET | `/api/notices` | token | `Notice[]` | 登录 | 10001 |
| GET | `/api/admin/dashboard` | token | `{operator,activityCount,publishedCount,pendingAudit,signupCount,checkinCount,userCount}` | ADMIN/ORGANIZER | 10001/10002 |
| GET | `/api/admin/activities` | token | `Activity[]` | ADMIN/ORGANIZER | 10002 |
| POST | `/api/admin/activities` | `{title,categoryId,cover,place,startTime,endTime,signupDeadline,capacity,description}` + token | `{id,checkinCode,auditStatus}` | ADMIN/ORGANIZER | 50001 参数错 |
| POST | `/api/admin/activities/{id}/audit` | `{approve}` + token | `{id,auditStatus,status}` | ADMIN/ORGANIZER | 20001 |
| GET | `/api/admin/users` | token | `User[]`（含 status） | ADMIN/ORGANIZER | 10002 |
| GET | `/api/admin/signups` | `activityId?` + token | `Signup[]`（含 real_name,student_no,title） | ADMIN/ORGANIZER | 10002 |
| POST | `/api/admin/checkin/verify` | `{activityId,userId}` + token | `{activityId,userId,verified}` | ADMIN/ORGANIZER | 40002 未签到 |
| GET | `/api/admin/logs` | token | `OpLog[]`（近 100 条） | ADMIN/ORGANIZER | 10002 |
| POST | `/api/upload` | multipart `file` | `{url,name,size}` | 登录（可选） | 50001 格式/大小 |
| GET | `/api/uploads` | - | `string[]` | 公开 | - |
| GET | `/uploads/{name}` | - | 静态图片二进制 | 公开 | - |

## 字段口径说明（易错点）

- `Activity` 来自 JdbcTemplate 直查，列名是**下划线**：`category_id/start_time/signed_up/audit_status/checkin_code`；
  `AdminController.publish` / `audit` 返回的 `checkinCode/auditStatus` 是**驼峰**——对接时必须按各自路径取字段，前端类型两者都保留。
- `/api/admin/signups` 返回 `user_id/real_name/student_no/title`（SQL JOIN 别名），学生端 `/api/me/signups` 返回 `activity_id/checkin_code`——两套字段不同，`Signup` 类型用可选字段兼容。
- `status`（活动发布态 DRAFT/PUBLISHED）与 `audit_status`（审核态 PENDING/APPROVED/REJECTED）是两个独立维度。

## 业务码表

`0` 成功；`10001` 未登录；`10002` 无权；`10003~10006` 学号绑定类；`10007` 登录失败；`20001/20002/20005` 活动类；`30001` 满员；`30002` 重复报名；`30003/30006` 报名状态；`40001~40004` 签到类；`50000` 系统繁忙；`50001` 参数错。
