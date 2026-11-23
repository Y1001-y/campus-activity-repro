-- 真实种子数据：5 用户 / 6 分类 / 4 活动 / 报名 / 消息 / 操作日志
-- 幂等：先删后插，重复执行行数不叠加
DELETE FROM op_log; DELETE FROM sys_notice; DELETE FROM activity_checkin;
DELETE FROM activity_signup; DELETE FROM activity; DELETE FROM activity_category; DELETE FROM sys_user;

INSERT INTO sys_user (id,username,password,real_name,role,student_no,phone,status) VALUES
 (1,'admin','admin123','系统管理员','ADMIN',NULL,'13800000001',1),
 (2,'organizer','org123','社团负责人','ORGANIZER',NULL,'13800000002',1),
 (3,'2024001001','stu123','张同学','STUDENT','2024001001','13800000003',1),
 (4,'2024001002','stu123','李同学','STUDENT','2024001002','13800000004',1),
 (5,'2024001003','stu123','王同学','STUDENT','2024001003','13800000005',1);

INSERT INTO activity_category (id,name,sort) VALUES
 (1,'学术讲座',1),(2,'文体活动',2),(3,'志愿服务',3),(4,'创新创业',4),(5,'社团招新',5),(6,'其他',6);

INSERT INTO activity (id,title,category_id,cover,place,start_time,end_time,signup_deadline,capacity,signed_up,status,audit_status,organizer_id,description,checkin_code) VALUES
 (101,'前端工程化实践工作坊',4,NULL,'实训楼 A302','2026-11-28 14:00','2026-11-28 17:00','2026-11-27 18:00',30,2,'PUBLISHED','APPROVED',2,'从零搭建 Vite + Vue3 工程，含请求层与类型契约。','CK-2026-101'),
 (102,'校园秋季马拉松',2,NULL,'东区操场','2026-12-05 08:00','2026-12-05 12:00','2026-12-01 20:00',200,1,'PUBLISHED','APPROVED',2,'5 公里欢乐跑，完赛可领取纪念徽章。','CK-2026-102'),
 (103,'图书馆志愿整理日',3,NULL,'图书馆二楼','2026-11-30 09:00','2026-11-30 11:30','2026-11-29 18:00',2,2,'PUBLISHED','APPROVED',2,'名额极少，用于复现「名额已满」业务码 30001。','CK-2026-103'),
 (104,'AI 应用创新分享会',1,NULL,'报告厅 101','2026-12-12 19:00','2026-12-12 21:00','2026-12-10 18:00',80,0,'DRAFT','PENDING',2,'待审核活动，用于后台审核流程演示。','CK-2026-104');

INSERT INTO activity_signup (id,activity_id,user_id,status,created_at) VALUES
 (1001,101,3,'APPROVED','2026-11-20 10:12:00'),
 (1002,101,4,'APPROVED','2026-11-20 10:35:00'),
 (1003,102,3,'APPROVED','2026-11-21 09:02:00'),
 (1004,103,3,'APPROVED','2026-11-22 15:40:00'),
 (1005,103,4,'APPROVED','2026-11-22 15:41:00');

INSERT INTO sys_notice (id,user_id,title,content,created_at) VALUES
 (2001,3,'报名审核通过','你报名的「前端工程化实践工作坊」已通过审核。','2026-11-20 10:20:00'),
 (2002,3,'活动即将开始','「图书馆志愿整理日」将于 11-30 09:00 开始。','2026-11-22 16:00:00'),
 (2003,4,'报名审核通过','你报名的「前端工程化实践工作坊」已通过审核。','2026-11-20 10:40:00');

INSERT INTO op_log (id,user_id,username,action,detail,created_at) VALUES
 (3001,1,'admin','LOGIN','管理员登录','2026-11-20 09:00:00'),
 (3002,2,'organizer','PUBLISH_ACTIVITY','发布活动 101','2026-11-20 09:30:00'),
 (3003,3,'2024001001','SIGNUP','报名活动 101','2026-11-20 10:12:00'),
 (3004,4,'2024001002','SIGNUP','报名活动 101','2026-11-20 10:35:00'),
 (3005,1,'admin','AUDIT_ACTIVITY','审核通过活动 101','2026-11-20 11:00:00');
