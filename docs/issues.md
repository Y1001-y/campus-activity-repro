# 全部问题记录（真实，含可复现指令）

> 10 个真实问题，每个都有原始输出证据 `evidence/bug-*.txt`。格式：现象 → 排查 → 原因 → 解决 → 预防。
> 所有复现指令均可在本仓库根目录/bash 下执行；后端需先在 8016 运行（见 README）。

---

## BUG-01 pnpm 12 拦截 core-js/esbuild 构建脚本

- **现象**：`cd app && pnpm install --no-frozen-lockfile` 退出码 1，`ERR_PNPM_IGNORED_BUILDS: Ignored build scripts: core-js-pure@3.51.0, core-js@3.51.0, esbuild@0.20.2`。
- **排查**：`cat pnpm-workspace.yaml` 看到 pnpm 自动生成的占位（`<pkg>: set this to true or false`）。
- **原因**：pnpm 12 默认禁止依赖执行 install/postinstall（供应链安全），uni-app 依赖链里的 core-js/esbuild 需要构建脚本。
- **解决**：`app/pnpm-workspace.yaml` 的 `allowBuilds` 显式写 `core-js: true`、`core-js-pure: true`、`esbuild: true`，重跑安装退出码 0。
- **预防**：初始化工程即写 `allowBuilds`，不靠 `pnpm approve-builds` 交互。
- **复现**：`cd app && git show d8bdbd8:app/pnpm-workspace.yaml > /tmp/w.yaml`（无 allowBuilds 版本）覆盖后再 install。
- **证据**：`evidence/bug-01-ignored-builds.txt`。

## BUG-02 YAML 字面 /n 导致解析失败

- **现象**：`pnpm install` 报 `line 2 column 15: mapping values are not allowed in this context`，第一行显示 `allowBuilds:/n  core-js: true`。
- **排查**：`cat pnpm-workspace.yaml` 确认 `/n` 是字面两字符而非换行。
- **原因**：用脚本一次性写 YAML 时把换行写成字面 `/n`，YAML 解析器把第二行当成同一行的值。
- **解决**：改用真实换行（`chr(10).join` / printf 多参数）重写文件，cat 复核后重装。
- **预防**：写 YAML 用 `cat <<'EOF'` 多行或编辑器，write 后必 cat 校验。
- **复现**：`python -c "open('pnpm-workspace.yaml','w').write('allowBuilds:/n  esbuild: true')" && pnpm install`。
- **证据**：`evidence/bug-02-yaml-newline.txt`。

## BUG-03 uni 条件编译下 BASE_URL 重复声明 TS2451

- **现象**：`cd app && pnpm type-check` 报 `TS2451: Cannot redeclare block-scoped variable 'BASE_URL'`（第 8、11 行）。
- **排查**：读 `src/api/request.ts`，H5 与非 H5 分支各写一个 `const BASE_URL`。
- **原因**：`// #ifdef H5` 是构建期条件编译标记，对 TypeScript 只是注释；vue-tsc 看到两个 `const` 判重复声明。
- **解决**：改为单个 `let BASE_URL = 'http://127.0.0.1:8016/api'` + 条件编译内 `BASE_URL='/api'` 覆盖，type-check 退出码 0。
- **预防**：type-check 纳入每次提交前检查，不能只靠 dev/build（构建期插件吃掉注释掩盖错误）。
- **复现**：把 request.ts 临时改回两个 `const BASE_URL` 后 `pnpm type-check`。
- **证据**：`evidence/bug-03-conditional-compile.txt`。

## BUG-04 admin 漏放行 vue-demi

- **现象**：`cd admin && pnpm install` 退出码 1，`Ignored build scripts: vue-demi@0.14.10`。
- **排查**：对照 BUG-01，发现 admin 的 allowBuilds 只放行了 esbuild，漏了 Pinia 传递依赖 vue-demi 的 postinstall。
- **原因**：与 BUG-01 同源，凭经验罗列不全。
- **解决**：在 `admin/pnpm-workspace.yaml` 的 allowBuilds 补 `vue-demi: true` 后重装。
- **预防**：以 `pnpm install` 报错里的 Ignored build scripts 列表为准逐项放行，改完重跑确认退出码 0。
- **复现**：从 allowBuilds 删除 vue-demi 行后 `pnpm install --no-frozen-lockfile`。
- **证据**：`evidence/bug-04-vue-demi.txt`。

## BUG-05 pnpm 自动追加占位行造成 YAML 重复键

- **现象**：补写 vue-demi 后 `pnpm install` 报 `duplicate mapping key: vue-demi`。
- **排查**：`cat admin/pnpm-workspace.yaml` 看到手工写的 `vue-demi: true` 与 pnpm 追加的 `vue-demi: set this to true or false` 并存。
- **原因**：pnpm 12 在 ERR_PNPM_IGNORED_BUILDS 时自动往 YAML 追加占位行，手工补同名键形成重复键。
- **解决**：整体重写为干净的 allowBuilds 映射（每包只出现一次），重装退出码 0。
- **预防**：修配置前先 cat，确认 pnpm 没自动追加；修完 cat 复核。
- **复现**：在干净 allowBuilds 里手工再写一遍同名键后 `pnpm install`。
- **证据**：`evidence/bug-05-duplicate-yaml-key.txt`。

## BUG-06 ActivityCard 相对路径写错

- **现象**：`cd app && pnpm type-check` 报 `TS2307: Cannot find module '../../utils/format'` 与 `'../../api/types'`。
- **排查**：`ActivityCard.vue` 位于 `src/components/`，导入 `src/api/types` 只需 `../`。
- **原因**：从 pages 目录复制组件时未调整相对路径层级。
- **解决**：改为 `../utils/format`、`../api/types`，type-check 退出码 0。
- **预防**：新建组件后用 type-check 立即验证 import。
- **复现**：把两处 import 改回 `../../` 后 `pnpm type-check`。
- **证据**：`evidence/bug-06-07-typecheck.txt`。

## BUG-07 admin 路由 children 漏逗号

- **现象**：`cd admin && pnpm type-check` 报 `TS1005: ',' expected`（router/index.ts 第 6/7/8 行）。
- **排查**：children 数组由多行字符串拼接，元素之间漏逗号；只有 1 个元素时不暴露，加到 4 个才报。
- **原因**：手写路由数组时漏逗号。
- **解决**：每个 RouteRecordRaw 元素末尾补逗号，重写路由数组后 type-check 退出码 0。
- **预防**：每新增页面立即 type-check，不攒到一轮结束。
- **复现**：删除 children 某元素末尾逗号后 `pnpm type-check`。
- **证据**：`evidence/bug-06-07-typecheck.txt`。

## BUG-08 chooseImage tempFiles 联合类型索引 TS7053

- **现象**：`cd app && pnpm type-check` 报 `TS7053: expression of type '0' can't be used to index type 'ChooseImage...|File|File[]'`（publish.vue 第 71 行）。
- **排查**：`res.tempFiles[0].size` 直接索引联合类型，TS 不允许对可能是单对象的类型用 `[0]`。
- **原因**：uni-app chooseImage 回调的 tempFiles 是「单对象 | 数组」联合类型。
- **解决**：`const files = Array.isArray(res.tempFiles) ? res.tempFiles : [res.tempFiles]` 归一化后取 `files[0]`，type-check 退出码 0。
- **预防**：uni API 回调的联合类型返回值先归一化再访问下标。
- **复现**：`git show a500029:app/src/pages/activity/publish.vue` 里把归一化改回 `res.tempFiles[0]` 后 type-check。
- **证据**：`evidence/bug-08-chooseimage-tempfiles.txt`。

## BUG-09 冒烟用例选错学生导致业务码分支不符

- **现象**：首次 `bash scripts/smoke.sh` → FAIL 第 11 项「期望 30001 实际 30002」，PASS=17 FAIL=1，退出码 1。
- **排查**：第 11 项复用学生 2024001001 报名活动 103；查 `data.sql` 该学生已报名 103（signup 1004）。
- **原因**：后端 `StudentController.signup` 先判重复报名（30002）再判容量（30001），测试账号前置条件不干净。
- **解决**：改用未报名的 2024001003/stu123 登录取 token 再报名 103 → 30001，重跑 PASS=18 FAIL=0。
- **预防**：写业务码分支用例先核对种子数据里该账号状态，确保前置条件干净。
- **复现**：把 smoke.sh 第 11 项的 FTOKEN 换回 STOKEN 后运行。
- **证据**：`evidence/bug-09-smoke-student-choice.txt`。

## BUG-10 uni 插件 CJS/ESM 互操作导致 build:h5 失败

- **现象**：`cd app && pnpm build:h5` 报 `failed to load config from vite.config.ts: uni is not a function`，退出码 1。
- **排查**：`node -e "require(...)"` 下 default 是 function；`node --input-type=module -e "import uni from ..."` 下 default 是 object，真正函数在其 `.default`。
- **原因**：`@dcloudio/vite-plugin-uni` 是 CJS 包，Vite 以 ESM 方式加载 TS 配置时发生互操作，默认导入不是函数。
- **解决**：vite.config.ts 里 `const uni = typeof uniPlugin === 'function' ? uniPlugin : uniPlugin.default`，重跑 build:h5 输出 `DONE Build complete.`，退出码 0。
- **预防**：CJS/ESM 双形态依赖在 vite.config 里做 typeof 归一化；构建脚本纳入提交前验证。
- **复现**：把 vite.config.ts 改回 `import uni from ...; plugins:[uni()]` 后 `pnpm build:h5`。
- **证据**：`evidence/bug-10-uni-esm-interop.txt`。
