#!/bin/bash

# Set the Android SDK and NDK paths
ANDROID_SDK_PATH="${HOME}/Android/Sdk"
ANDROID_NDK_PATH="${HOME}/Android/Sdk/ndk/21.4.7075529"

# Remove existing values for ANDROID_HOME and ANDROID_NDK_HOME
sudo sed -i '/^ANDROID_HOME/d' /etc/environment
sudo sed -i '/^ANDROID_NDK_HOME/d' /etc/environment

# Append the environment variables to /etc/environment using sudo tee
sudo sh -c "echo 'ANDROID_HOME=${ANDROID_SDK_PATH}' >> /etc/environment"
sudo sh -c "echo 'ANDROID_NDK_HOME=${ANDROID_NDK_PATH}' >> /etc/environment"

# Reload the environment variables
source /etc/environment

# Print a message to confirm the environment variables were set
echo "ANDROID_HOME set to ${ANDROID_SDK_PATH}"
echo "ANDROID_NDK_HOME set to ${ANDROID_NDK_PATH}"
