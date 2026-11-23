#!/usr/bin/env bash
# S01 端到端冒烟：全部请求真实打到本机 8016，无任何 mock。
# 用法：先启动后端，再 bash scripts/smoke.sh
# 依赖：curl、python3（仅用于从 JSON 取字段，不参与造假）
set -u
BASE="${BASE:-http://127.0.0.1:8016}"
PASS=0; FAIL=0
log() { echo "$*"; }
check() { # check <名称> <实际值> <期望值>
  if [ "$2" = "$3" ]; then log "  PASS  $1 (=$2)"; PASS=$((PASS+1))
  else log "  FAIL  $1 (期望 $3, 实际 $2)"; FAIL=$((FAIL+1)); fi
}
api() { curl -s -m 8 "$@"; }
field() { python3 -c "import sys,json; d=json.load(sys.stdin); print($1)" 2>/dev/null; }
code() { field "d.get('code')"; }

log "=== S01 端到端冒烟 BASE=$BASE ==="
log "[1] 健康检查 /api/ping"
R=$(api "$BASE/api/ping"); check "ping.code" "$(echo "$R"|code)" "0"

log "[2] 活动分类 /api/categories"
R=$(api "$BASE/api/categories"); check "categories.code" "$(echo "$R"|code)" "0"
check "categories 条数" "$(echo "$R"|field "len(d['data'])")" "6"

log "[3] 公开活动列表 /api/activities"
R=$(api "$BASE/api/activities?page=1&size=2"); check "activities.code" "$(echo "$R"|code)" "0"
check "分页 size" "$(echo "$R"|field "d['data']['size']")" "2"

log "[4] 管理员登录 /api/auth/login"
R=$(api -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' "$BASE/api/auth/login")
check "login.code" "$(echo "$R"|code)" "0"
TOKEN=$(echo "$R"|field "d['data']['token']")
[ -n "$TOKEN" ] && log "  PASS  拿到真实 token (长度 ${#TOKEN})" && PASS=$((PASS+1)) \
  || { log "  FAIL  未拿到 token"; FAIL=$((FAIL+1)); }

log "[5] 携带 token 访问 /api/auth/me"
R=$(api -H "Authorization: Bearer $TOKEN" "$BASE/api/auth/me")
check "me.code" "$(echo "$R"|code)" "0"
check "me.role" "$(echo "$R"|field "d['data']['role']")" "ADMIN"

log "[6] 后台工作台 /api/admin/dashboard（真实 SQL 统计）"
R=$(api -H "Authorization: Bearer $TOKEN" "$BASE/api/admin/dashboard")
check "dashboard.code" "$(echo "$R"|code)" "0"
check "activityCount" "$(echo "$R"|field "d['data']['activityCount']")" "4"
check "userCount" "$(echo "$R"|field "d['data']['userCount']")" "5"

log "[7] 学生登录并报名 /api/activities/101/signup"
SR=$(api -H 'Content-Type: application/json' -d '{"username":"2024001001","password":"stu123"}' "$BASE/api/auth/login")
STOKEN=$(echo "$SR"|field "d['data']['token']")
R=$(api -X POST -H "Authorization: Bearer $STOKEN" "$BASE/api/activities/101/signup")
# 该学生种子数据已报名 101，预期命中 30002 业务码（证明错误分支真实生效）
check "重复报名返回业务码 30002" "$(echo "$R"|code)" "30002"

log "[8] 签到码错误分支 /api/checkin（错误码 40001）"
R=$(api -H 'Content-Type: application/json' -H "Authorization: Bearer $STOKEN" \
   -d '{"activityId":101,"code":"WRONG-CODE"}' "$BASE/api/checkin")
check "错误签到码返回 40001" "$(echo "$R"|code)" "40001"

log "[9] 未登录访问后台 /api/admin/dashboard（拦截码 10001）"
R=$(api "$BASE/api/admin/dashboard")
check "未登录返回 10001" "$(echo "$R"|code)" "10001"

log "[10] 低权限令牌访问后台（拦截码 10002）"
R=$(api -H "Authorization: Bearer $STOKEN" "$BASE/api/admin/dashboard")
check "学生令牌返回 10002" "$(echo "$R"|code)" "10002"

log "[11] 名额已满分支 /api/activities/103/signup（码 30001）"
# 必须用未报名 103 的学生（2024001003）；复用 STOKEN 会先命中 30002（见 bug-09）
FRESHR=$(api -H 'Content-Type: application/json' -d '{"username":"2024001003","password":"stu123"}' "$BASE/api/auth/login")
FTOKEN=$(echo "$FRESHR"|field "d['data']['token']")
R=$(api -X POST -H "Authorization: Bearer $FTOKEN" "$BASE/api/activities/103/signup")
check "容量 2 已满返回 30001" "$(echo "$R"|code)" "30001"

log "[12] 素材列表 /api/uploads"
R=$(api "$BASE/api/uploads"); check "uploads.code" "$(echo "$R"|code)" "0"

log ""
log "=== 结果：PASS=$PASS FAIL=$FAIL ==="
[ "$FAIL" -eq 0 ]
