#!/bin/bash
exec > /tmp/sugar-build.log 2>&1
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export ANDROID_HOME=/home/z/my-project/android-sdk
export ANDROID_SDK_ROOT=/home/z/my-project/android-sdk
export PATH=$JAVA_HOME/bin:$PATH
export GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx1536m -Dfile.encoding=UTF-8"
cd /home/z/my-project/Sugar
echo "BUILD START $(date)"
/home/z/.gradle/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/bin/gradle :app:assembleDebug --no-daemon --console=plain --stacktrace 2>&1
echo "BUILD END exit=$? $(date)"
