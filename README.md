# Face Effect AR App

## Platform

**Linux**. The app requires the Google Mediapipe framework which doesn't natively support Windows.

> **Note:** I would recommend using Ubuntu 22.04.2 LTS.

## Necessary Installs Before Anything Else

Each of these installs must be done through terminal and some require sudo privileges.

1.  *Download and install the updates for each outdated package and dependency on your system* using  ```sudo apt-get update```

2. Install git using ```sudo apt-get install git```

3. Install OpenCV and FFmpeg using ```sudo apt-get install -y \
   libopencv-core-dev \
   libopencv-highgui-dev \
   libopencv-calib3d-dev \
   libopencv-features2d-dev \
   libopencv-imgproc-dev \
   libopencv-video-dev```

4. Install Android Debug Bridge using ```sudo apt-get install adb```

5. Install Python pip and the required packages using:
   ```sudo apt-get install python3-pip```
   and
   ```pip3 install --user six absl-py attrs flatbuffers numpy opencv-contrib-python protobuf==3.11 sounddevice==0.4.4```


You MUST also turn on developer settings on your Android device to enable USB debugging. Once USB debugging is turned on you should get a popup message asking you to allow it when you connect your device to the PC. Allow it and run the following command in terminal to see whether the device is visible: ```adb devices```.

## Installing Bazel

Again, these commands must be done through terminal and may require sudo privileges.

1.  *Add the Bazel distribution URI as a package source using*
``````
sudo apt install apt-transport-https curl gnupg -y &&
curl -fsSL https://bazel.build/bazel-release.pub.gpg | gpg --dearmor >bazel-archive-keyring.gpg &&
sudo mv bazel-archive-keyring.gpg /usr/share/keyrings &&
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/bazel-archive-keyring.gpg] https://storage.googleapis.com/bazel-apt stable jdk1.8" | sudo tee /etc/apt/sources.list.d/bazel.list
``````

2. *Install and update Bazel 6.1.1 using* ```sudo apt update && sudo apt install bazel-6.1.1```

3. Check Bazel has installed correctly using ```bazel --version```
4. Set up the environment using ```export PATH="$PATH:$HOME/bin"```


## Installing and Setting Android Studio

Again, these commands must be done through terminal and may require sudo privileges.

1.  [Download Android Studio Flamingo | 2022.2.1 for Linux](https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2022.2.1.18/android-studio-2022.2.1.18-linux.tar.gz)

2. Install JAVA JDK using ```sudo apt-get install openjdk-11-jdk```

3. Install the required libraries for Android Studio with ```sudo apt-get install libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386```

4. Unpack the Android Studio tar.gz file which should be in your download folder using ```cd $HOME/Downloads/ && tar -xzf android-studio-2022.2.1.18-linux.tar.gzls```

5. Move the unpacked files to /opt/local using ```sudo mv $HOME/Downloads/android-studio /opt/local/```

6. Add an Android Studio Desktop entry to make launching it easier / searchable using ```sudo nano /usr/share/applications/android-studio.desktop```. This command will open a text editor where you will need to paste this into it:
   > [Desktop Entry]
   Name=Android Studio
   Comment=Android Studio IDE
   Exec=/opt/local/android-studio/bin/studio.sh
   Icon=/opt/local/android-studio/bin/studio.png
   Terminal=false
   Type=Application
   Categories=Development;IDE;

   Save the file (Ctrl+O) and exit (Enter).

7. Press the Windows key and search for Android Studio and follow the setup wizard.

8. Once the setup wizard completes, you should be on the "Welcome to Android Studio" page. **Press More Actions > SDK Manager** .

9. A settings window should be visible now that's on the Android SDK page. Check the boxes **'Hide Obsolete Packages'** and **'Show Package Details'**.

10. On the SDK Platforms tab, check the boxes for '**Android SDK Platform 33'** and **'Sources for Android 33'** under Android 13.0 (Tiramisu).

11. On the SDK Tools tab, check the box for  **'33.0.2'** under Android SDK Build-Tools 34-rc3.

12. On the SDK Tools tab, check the box for  **'21.4.7075529'** under NDK (Side by side).

13. Setup the Android SDK and NDK environment variables using the following command in terminal: ```export ANDROID_HOME=$HOME/Android/Sdk && export ANDROID_HOME_NDK=$HOME/Android/Sdk/ndk/21.4.7075529```.