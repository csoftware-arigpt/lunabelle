#!/usr/bin/env bash
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-/cache/gradle}"
PLATFORM="${ANDROID_PLATFORM:-android-36}"
BUILD_TOOLS="${ANDROID_BUILD_TOOLS:-36.0.0}"

# Writable HOME + android prefs under the persisted gradle cache volume (works under --user)
export HOME="$GRADLE_USER_HOME"
export ANDROID_USER_HOME="$GRADLE_USER_HOME/.android"
mkdir -p "$ANDROID_HOME" "$GRADLE_USER_HOME" "$ANDROID_USER_HOME"

if [ ! -d "$ANDROID_HOME/platforms/$PLATFORM" ] || [ ! -d "$ANDROID_HOME/build-tools/$BUILD_TOOLS" ]; then
  echo ">> Installing Android SDK ($PLATFORM, build-tools $BUILD_TOOLS) into $ANDROID_HOME (one-time, cached on external volume)..."
  yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses >/dev/null 2>&1 || true
  sdkmanager --sdk_root="$ANDROID_HOME" "platform-tools" "platforms;$PLATFORM" "build-tools;$BUILD_TOOLS"
fi

exec "$@"
