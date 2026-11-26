# 星阶 StarStep · Android 端

> 基于《星阶 PRD》与《滚雪球竞品调研报告》开发的 Android 原生 App
> 技术栈：Kotlin 2.0 + Jetpack Compose (Material 3) + Navigation Compose + DataStore
> 本期范围：**不含数据埋点**（按需求跳过）；单机离线优先。

---

## 线上发布（v0.2.0）

**分享链接**：https://a27c80533834c7c1d.app-tencent.workbuddy.host/?sharecode=LHN357TIM6

- 页面含产品介绍、三步上手说明、FAQ、安卓版下载按钮
- 点「下载安卓版」获取 `xingjie.apk`（即本项目的 v0.2.0 安装包）
- 站点 `version.json` 是版本源；App 启动时静默检查，发现新版本弹提示（失败静默忽略）
- 意见反馈：App「设置 → 意见反馈」拉起 QQ 1183295524
- 重新发布：`node <skill-dir>/scripts/publish.js --dir <site目录> --language static`（同一链接覆盖）

> 版本发布规则：升级 App 时把 `app/build.gradle.kts` 的 `versionCode/versionName`、
> `site/version.json` 的 `versionCode/versionName`、以及版本源 URL 里的地址保持一致。
> 只有站点 `versionCode > App 的 BuildConfig.VERSION_CODE` 时才会提示更新。

---

## 一、直接安装（无需开发环境）

项目根目录已提供编译好的安装包：

```
星阶-app-debug.apk   (17MB, debug 签名)
```

- 用数据线传到安卓手机，或用二维码/网盘传过去，点击安装
- 需 Android 8.0（API 26）及以上
- 首次安装系统可能提示「允许安装未知来源应用」，按提示开启即可
- debug 包仅供体验，正式上架需换 release 签名

---

## 二、从源码构建（Android Studio）

1. 用 Android Studio (Koala 或更新版本) 打开 `StarStepAndroid/` 目录
2. 等待 Gradle 同步（首次会下载依赖，需联网）
3. 连接真机或启动模拟器（API 26+）
4. 点击 ▶ Run，或命令行：

```bash
./gradlew assembleDebug          # 打 debug 包
./gradlew installDebug           # 安装到已连接设备
```

命令行构建需本机配置 `ANDROID_HOME` 指向 Android SDK。

### 关键版本
| 项 | 版本 |
|---|---|
| Kotlin | 2.0.21 |
| AGP | 8.6.1 |
| Gradle Wrapper | 8.9 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |
| Compose BOM | 2024.10.00 |

---

## 三、已实现功能（对照 PRD）

| 模块 | 界面 | 状态 | 关键实现 |
|---|---|---|---|
| 意图与路线 | 首页意图捕获 | ✅ | 双输入框（现状→目标）、吉祥物、粗/细粒度胶囊、最近路线 |
| 意图与路线 | 路线生成 | ✅ | 两档拆解（粗3步/细7步）、逐步可编辑、生成即可播放 |
| 单步执行 | 执行页 | ✅ | 单步聚焦、行动力+2、本步计时（切后台继续）、语音引导、遇到困难跳过、记状态 |
| 单步执行 | 折星完成态 | ✅ | 折纸合拢动画（1.5s，可跳过）、减少动态效果降级 |
| 单步执行 | 调整路线 | ✅ | 现场改步骤、方案胶囊、聚焦/全览切换、保存只影响当前路线 |
| 星阶与召回 | 星阶墙 | ✅ | 阶梯等级、色卡筛选、漂浮星星、单击展开/长按撤销、一键展开最近 |
| 星阶与召回 | 星星回执 | ✅ | 展开动画（1.1s）、手写留言、雪球回信、折回/再做一次/拆星撤销 |
| 路线详情 | 路线详情 | ✅ | 步骤可展开、提醒规则、数据四宫格、全流程预估前置 |
| 状态 | 精力状态 | ✅ | 近7天曲线、高能时段洞察、四档打卡（三处数据汇聚） |
| 全局 | 设置 | ✅ | 语音/自动开始/减少动效三个可关开关、恢复演示数据 |

### 三条底线已落入代码
- **只做正反馈**：无掉血、无连胜清零、无排名；行动力只增不减，拆星才回退
- **不做无限细拆**：仅粗/细两档，上限 12 步
- **拆解即可执行**：生成草案后「就走这条」直接进入执行页

### 竞品优点已借鉴
- 语音播报下一步（TTS，静音降级为文字）
- 到点自动开始 / 自动推进（可关）
- 剩余步数 + 时长可视化
- 全流程预估字段前置
- 星星成长回信（基于用时/频次/当时状态生成，非空洞鼓励）

---

## 四、数据与设计说明

- **本地持久化**：DataStore + JSON 序列化，全离线，无服务端依赖
- **首次启动**：内置种子数据（8 条路线、34 颗星、近 7 天精力），打开即见「走过的路」；设置页可恢复
- **色卡**：晴空蓝 #7EC8E3 / 暖白 #F5F3E7 / 薄荷绿 #A8D5BA / 藕粉 #E8B4B8 / 深海蓝 #2E5C8A
- **星阶等级**：起步0 / 破土8 / 抽芽18 / 小小树30 / 微光40 / 星河54 / 银河72 / 璀璨96
- **本期不做**：数据埋点、多端同步、服务端编排、硬件载体、第三方 AI、复杂社交

## 五、源码结构

```
app/src/main/java/com/starstep/app/
├── MainActivity.kt / StarStepApplication.kt
├── data/        Model.kt（数据模型+星阶规则）· SeedData.kt（种子数据）· StarStepRepository.kt（持久化）
├── viewmodel/   StarStepViewModel.kt（全部业务逻辑）
├── ui/theme/    Color.kt · Type.kt · Theme.kt（深海蓝星空主题）
├── ui/components/  StarShape · Mascot · BottomTab · Common（复用组件库）
├── ui/navigation/  AppNavigation.kt（11 条路由）
├── ui/screens/     11 个界面
└── util/        TtsHelper.kt（语音引导）
```

## 六、验证结果

- ✅ **真编译通过**：`gradle assembleDebug` → BUILD SUCCESSFUL，产出 17MB 可安装 APK
- ✅ 包信息核对：包名 `com.starstep.app`、应用名「星阶」、minSdk 26 / targetSdk 35
- ✅ Kotlin 静态检查：26 个源文件括号平衡、导航引用完整、无占位符残留
- ✅ 编译期修复 6 处真实错误（事件去重、类型推断、缺失 import 等）
