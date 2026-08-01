#!/bin/bash
export JAVA_HOME=/tmp/jdk-21.0.12
export ANDROID_HOME=/home/z/my-project/android-sdk
export ANDROID_SDK_ROOT=/home/z/my-project/android-sdk
export PATH=$JAVA_HOME/bin:$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
cd /home/z/my-project/PlanReminder
/tmp/gradle-dist/gradle-8.5/bin/gradle :app:assembleDebug --no-daemon --console=plain
echo "EXIT=$?"
