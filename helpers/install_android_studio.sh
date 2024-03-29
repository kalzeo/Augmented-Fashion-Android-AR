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

# Update system packages
echo "Updating system packages..."
sudo apt-get update -qq

# Install Java JDK
echo "Installing Java JDK..."
sudo apt-get install -qq openjdk-11-jdk

# Install required libraries for Android Studio
echo "Installing required libraries..."
sudo apt-get install -qq libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386

# Download Android Studio tar.gz file and move it to /opt/local
echo "Downloading Android Studio..."
wget -q https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2022.2.1.18/android-studio-2022.2.1.18-linux.tar.gz -P $HOME/Downloads/
echo "Unpacking Android Studio..."
sudo tar -xf $HOME/Downloads/android-studio-2022.2.1.18-linux.tar.gz -C /opt/local/

# Create Android Studio Desktop entry
echo "Creating Android Studio Desktop entry..."
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
# echo "Launching Android Studio..." && /opt/local/android-studio/bin/studio.sh &
# echo "Completing setup wizard..."
# while [ ! -f $HOME/.android/repositories.cfg ]; do sleep 1; done

# Download and install Android SDK command line tools
echo "Downloading Android SDK command line tools..."
mkdir -p $ANDROID_SDK_PATH/cmdline-tools
wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -P $HOME/Downloads/
echo "Unzipping Android SDK command line tools..."
unzip -qo $HOME/Downloads/commandlinetools-linux-9477386_latest.zip -d $ANDROID_SDK_PATH/cmdline-tools/
mv $ANDROID_SDK_PATH/cmdline-tools/cmdline-tools $ANDROID_SDK_PATH/cmdline-tools/latest
echo "Adding Android SDK command line tools to PATH..."
echo 'export PATH="$ANDROID_SDK_PATH/cmdline-tools/latest/bin:$PATH"' >> ~/.bashrc

# Install required Android SDK and NDK packages
echo "Installing required Android SDK and NDK packages..."
sudo apt-get install -qq jq

# Accept Android SDK licenses
echo "Accepting Android SDK licenses..."
yes | sudo $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

# Install Android SDK Platform 33 and Sources for Android 33
echo "Installing Android SDK Platform 33 and Sources for Android 33..."
sudo $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-33" "sources;android-33"

# Install Android SDK Build-Tools 33.0.2
sudo -v && echo "Installing Android SDK Build-Tools 33.0.2..." && sudo $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "build-tools;33.0.2"

# Install Android NDK 21.4.7075529
sudo -v && echo "Installing Android NDK 21.4.7075529..." && sudo $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "ndk;21.4.7075529"

# Install Android SDK Platform-Tools 34.0.1
sudo -v && echo "Installing latest Android SDK Platform-Tools..." && sudo $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools"
