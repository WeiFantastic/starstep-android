#!/usr/bin/env bash
# ============================================================
# 星阶 StarStep · 一键发布脚本
# 用法：
#   ./发布.sh                          # 默认：versionCode+1，版本名不变，更新说明="体验优化"
#   ./发布.sh "修复提示条配色"          # 自定义更新说明
#   ./发布.sh "修复提示条配色" "0.2.1"  # 自定义更新说明 + 版本名
#
# 自动完成：升版本号 → 编译 APK → 复制到 site → 更新 version.json → 打包 ZIP
# 不能自动：上传花生壳（需你本人账号），脚本最后会提示你上传哪个文件
# ============================================================
set -euo pipefail
cd "$(dirname "$0")"

CHANGELOG="${1:-体验优化与问题修复}"
NEW_VERSION_NAME="${2:-}"

# ---- 0. 环境准备（SDK 路径 + 干净 Gradle 缓存）----
# 若 local.properties 缺失或路径不对，自动写入 SDK 路径
SDK_DIR=""
for d in /tmp/android-sdk "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" /opt/android-sdk /root/Android/Sdk; do
  [ -n "$d" ] && [ -d "$d/platforms" ] && SDK_DIR="$d" && break
done
if [ -n "$SDK_DIR" ]; then
  echo "sdk.dir=$SDK_DIR" > local.properties
  echo "ℹ️  SDK 路径：$SDK_DIR"
else
  echo "❌ 未找到 Android SDK，请设置 ANDROID_HOME 或创建 local.properties" >&2
  exit 1
fi
# 规避全局 init 脚本污染，使用干净 Gradle 缓存目录
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-/tmp/gradle-home}"

# ---- 1. 读取并递增版本号 ----
BUILD_FILE="app/build.gradle.kts"
OLD_CODE=$(grep -oE 'versionCode = [0-9]+' "$BUILD_FILE" | grep -oE '[0-9]+')
OLD_NAME=$(grep -oE 'versionName = "[^"]+"' "$BUILD_FILE" | sed 's/versionName = "//;s/"//')
NEW_CODE=$((OLD_CODE + 1))
if [ -n "$NEW_VERSION_NAME" ]; then
  NEW_NAME="$NEW_VERSION_NAME"
else
  NEW_NAME="$OLD_NAME"
fi
sed -i "s/versionCode = $OLD_CODE/versionCode = $NEW_CODE/" "$BUILD_FILE"
sed -i "s/versionName = \"$OLD_NAME\"/versionName = \"$NEW_NAME\"/" "$BUILD_FILE"
echo "✅ 版本号：$OLD_NAME($OLD_CODE) → $NEW_NAME($NEW_CODE)"

# ---- 2. 编译 APK ----
echo "🔨 正在编译..."
./gradlew assembleDebug -q
APK="app/build/outputs/apk/debug/app-debug.apk"
[ -f "$APK" ] || { echo "❌ 编译产物不存在：$APK" >&2; exit 1; }
echo "✅ 编译完成"

# ---- 3. 复制到站点目录 ----
cp "$APK" site/xingjie.apk
echo "✅ APK 已同步到 site/xingjie.apk"

# ---- 4. 更新 version.json ----
# 保留原有 downloadUrl/downloadPage（公网域名），只更新版本与说明
python3.11 - "$NEW_CODE" "$NEW_NAME" "$CHANGELOG" <<'PY'
import json, sys, datetime
code, name, changelog = sys.argv[1], sys.argv[2], sys.argv[3]
p = 'site/version.json'
d = json.load(open(p, encoding='utf-8'))
d['versionCode'] = int(code)
d['versionName'] = name
d['changelog'] = changelog
d['publishDate'] = datetime.date.today().isoformat()
json.dump(d, open(p, 'w', encoding='utf-8'), ensure_ascii=False, indent=2)
print('✅ version.json 已更新')
PY

# ---- 5. 打包 ZIP ----
ZIP="星阶-app-发布包.zip"
rm -f "$ZIP"
python3.11 - <<'PY'
import zipfile, os
with zipfile.ZipFile('星阶-app-发布包.zip', 'w', zipfile.ZIP_DEFLATED) as z:
    for f in ['index.html', 'version.json', 'xingjie.apk']:
        z.write(os.path.join('site', f), arcname=f)
PY
echo "✅ 发布包已生成：$ZIP ($(du -h "$ZIP" | cut -f1))"

# ---- 6. 提示下一步 ----
echo ""
echo "============================================================"
echo "🎉 本地准备完成！版本 $NEW_NAME($NEW_CODE)"
echo "下一步（需你本人操作，脚本替代不了）："
echo "  1) 浏览器打开 https://drop.ficp.fun"
echo "  2) 上传这个文件：$PWD/$ZIP"
echo "  3) 发布后微信扫码认领（否则链接会跳激活页）"
echo "  4) 核对 version.json 里 downloadUrl 是否为公网地址，若域名变了需改后重传"
echo "  5) 把公网链接发给朋友"
echo "============================================================"
