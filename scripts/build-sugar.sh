#!/bin/bash
exec > /tmp/sugar-build.log 2>&1
export JAVA_HOME=/tmp/jdk-17.0.20+8
export ANDROID_HOME=/home/z/my-project/android-sdk
export ANDROID_SDK_ROOT=/home/z/my-project/android-sdk
export PATH=$JAVA_HOME/bin:$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
cd /home/z/my-project/Sugar
echo "BUILD START $(date)"
/tmp/gradle-dist/gradle-8.5/bin/gradle :app:assembleDebug --no-daemon --console=plain
echo "BUILD END exit=$? $(date)"
