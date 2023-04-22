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

# Prompt user for sudo password
sudo -v

# Update system packages
sudo apt-get update -qq

# Install Java JDK
sudo apt-get install -qq openjdk-11-jdk

# Install required libraries for Android Studio
sudo apt-get install -qq libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386

# Unpack the Android Studio tar.gz file and move it to /opt/local
sudo tar -xf $HOME/Downloads/android-studio-2022.2.1.18-linux.tar.gz -C /opt/local/

# Create Android Studio Desktop entry
sudo bash -c 'cat <<EOF > /usr/share/applications/android-studio.desktop
[Desktop Entry]
Name=Android Studio
Comment=Android Studio IDE
Exec=/opt/local/android-studio/bin/studio.sh
Icon=/opt/local/android-studio/bin/studio.png
Terminal=false
Type=Application
Categories=Development;IDE;
EOF'

# Launch Android Studio and complete setup wizard
/opt/local/android-studio/bin/studio.sh &

# Wait for Android Studio to finish setup wizard
while [ ! -f $HOME/.android/repositories.cfg ]; do sleep 1; done

# Install required Android SDK and NDK packages
sudo apt-get install -qq jq

# Install Android SDK Platform 33 and Sources for Android 33
sudo $ANDROID_HOME/tools/bin/sdkmanager "platforms;android-33" "sources;android-33"

# Install Android SDK Build-Tools 33.0.2
sudo $ANDROID_HOME/tools/bin/sdkmanager "build-tools;33.0.2"

# Install Android NDK 21.4.7075529
sudo $ANDROID_HOME/tools/bin/sdkmanager "ndk;21.4.7075529"