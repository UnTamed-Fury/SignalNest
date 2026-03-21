#!/data/data/com.termux/files/usr/bin/bash
# SignalNest — Termux ARM64 build script
set -e

export GRADLE_OPTS="-Xmx900m -XX:MaxMetaspaceSize=256m -Dorg.gradle.daemon=false"

R='\033[0;31m'; G='\033[0;32m'; Y='\033[1;33m'; B='\033[0;34m'; N='\033[0m'
ok()  { echo -e "${G}✓ $*${N}"; }
err() { echo -e "${R}✗ $*${N}"; exit 1; }
inf() { echo -e "${Y}→ $*${N}"; }
hdr() { echo -e "${B}══ $* ══${N}"; }

check() {
    hdr "Environment check"
    command -v java &>/dev/null || err "Java not found — run: pkg install openjdk-17"
    ok "Java: $(java -version 2>&1 | head -1)"
    [ -n "$ANDROID_HOME" ] || err "ANDROID_HOME not set — source ~/.profile"
    [ -d "$ANDROID_HOME" ] || err "ANDROID_HOME=$ANDROID_HOME does not exist"
    ok "SDK: $ANDROID_HOME"
    command -v aapt2 &>/dev/null || { inf "Installing aapt2..."; pkg install aapt2 -y; }
    ok "aapt2: $(command -v aapt2)"
    echo "sdk.dir=$ANDROID_HOME" > signalnest-app/local.properties
    chmod +x signalnest-app/gradlew
    echo ""
}

debug() {
    hdr "assembleDebug"
    cd signalnest-app
    ./gradlew assembleDebug --no-daemon --stacktrace
    APK="app/build/outputs/apk/debug/app-debug.apk"
    [ -f "$APK" ] || err "APK not found"
    ok "APK: signalnest-app/$APK"
    [ -d "/storage/emulated/0/Download" ] && cp "$APK" "/storage/emulated/0/Download/signalnest-debug.apk" && ok "Copied to Downloads/"
    cd ..
}

release() {
    hdr "assembleRelease"
    if [ -z "$KEYSTORE_FILE" ]; then
        inf "KEYSTORE_FILE not set — unsigned release"
        inf "To sign:  export KEYSTORE_FILE=~/signalnest.jks"
        inf "          export KEYSTORE_PASSWORD=pass"
        inf "          export KEY_ALIAS=signalnest"
        inf "          export KEY_PASSWORD=pass"
        inf "Create:   keytool -genkey -v -keystore ~/signalnest.jks -keyalg RSA -keysize 2048 -validity 10000 -alias signalnest"
    fi
    cd signalnest-app
    ./gradlew assembleRelease --no-daemon --stacktrace
    APK="app/build/outputs/apk/release/app-release.apk"
    UNSIGNED="app/build/outputs/apk/release/app-release-unsigned.apk"
    [ -f "$APK" ] && ok "Signed APK: signalnest-app/$APK" || ok "Unsigned APK: signalnest-app/$UNSIGNED"
    cd ..
}

clean() { hdr "clean"; (cd signalnest-app && ./gradlew clean --no-daemon 2>/dev/null || true); ok "Done"; }

case "${1:-debug}" in
    debug)   check; debug   ;;
    release) check; release ;;
    clean)   clean          ;;
    *) echo "Usage: $0 [debug|release|clean]"; exit 1 ;;
esac
