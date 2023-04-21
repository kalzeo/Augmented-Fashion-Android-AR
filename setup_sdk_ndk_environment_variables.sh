#!/bin/bash

# Set the Android SDK and NDK paths
ANDROID_SDK_PATH="$HOME/Android/Sdk"
ANDROID_NDK_PATH="$HOME/Android/Sdk/ndk/21.4.7075529"

# Set the environment variables
export ANDROID_HOME="$ANDROID_SDK_PATH"
export ANDROID_HOME_NDK="$ANDROID_NDK_PATH"

# Print a message to confirm the environment variables were set
echo "ANDROID_HOME set to $ANDROID_SDK_PATH"
echo "ANDROID_HOME_NDK set to $ANDROID_NDK_PATH"