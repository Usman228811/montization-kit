#!/bin/sh
# Some JitPack build hosts can't open gradle/wrapper/gradle-wrapper.jar from the checkout.
# If ./gradlew fails, download the official wrapper jar to /tmp and build with that instead.
TASKS="${*:-:MonetizeKit:publishToMavenLocal}"
ARGS="--no-daemon --console=plain"

chmod +x gradlew
./gradlew $TASKS $ARGS && exit 0

echo "=== ./gradlew failed, retrying with a fresh wrapper jar ==="
JAR_URL=https://raw.githubusercontent.com/gradle/gradle/v9.8.0/gradle/wrapper/gradle-wrapper.jar
JAR_SHA=238e777fcddd7e34f9708186085def2abd6e08e658505b38718d79d74c21abd5
W=/tmp/jitpack-wrapper/gradle/wrapper
mkdir -p $W
cp gradle/wrapper/gradle-wrapper.properties $W/

n=0
until curl -sSfL -o $W/gradle-wrapper.jar $JAR_URL && echo "$JAR_SHA  $W/gradle-wrapper.jar" | sha256sum -c -; do
    n=$((n+1))
    if [ $n -ge 5 ]; then echo "could not download wrapper jar"; exit 1; fi
    sleep 10
done

n=0
until java -Dorg.gradle.appname=gradlew -classpath $W/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain $TASKS $ARGS; do
    n=$((n+1))
    if [ $n -ge 3 ]; then exit 1; fi
    echo "=== build failed, retry $n in 20s ==="
    sleep 20
done
