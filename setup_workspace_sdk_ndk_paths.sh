#!/bin/bash

# Set the Android SDK and NDK paths from environment variables
ANDROID_SDK_PATH=${ANDROID_HOME}
ANDROID_NDK_PATH=${ANDROID_NDK_HOME}

# Check if the Android SDK path is set
if [ -z "${ANDROID_SDK_PATH}" ]; then
  echo "Error: ANDROID_HOME environment variable is not set."
  exit 1
fi

# Check if the Android NDK path is set
if [ -z "${ANDROID_NDK_PATH}" ]; then
  echo "Error: ANDROID_HOME_NDK environment variable is not set."
  exit 1
fi

# Delete old android_sdk_repository() and android_ndk_repository() if exists
sed -i '/android_sdk_repository(/,/)/d' WORKSPACE
sed -i '/android_ndk_repository(/,/)/d' WORKSPACE

# Add an empty line before android_sdk_repository
echo "\n" >> WORKSPACE

# Add the Android SDK repository to the Bazel WORKSPACE file
echo "android_sdk_repository(
    name = \"androidsdk\",
    api_level = 33,
    build_tools_version = \"33.0.2\",
    path = \"${ANDROID_SDK_PATH}\",
)" >> WORKSPACE

# Add an empty line after android_sdk_repository
echo "\n" >> WORKSPACE

# Add the Android NDK repository to the Bazel WORKSPACE file
echo "android_ndk_repository(
    name = \"androidndk\",
    path = \"${ANDROID_NDK_PATH}\",
    api_level = 21,
)" >> WORKSPACE

echo "Android SDK and NDK repositories added to WORKSPACE file."
