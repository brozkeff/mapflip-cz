# Android development on macOS

Install the CLI tools with Homebrew:

```sh
brew install openjdk@17 kotlin android-commandlinetools
export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
export ANDROID_HOME="$(brew --prefix)/share/android-commandlinetools"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
sdkmanager --licenses
sdkmanager 'platform-tools' 'platforms;android-36' 'build-tools;34.0.0' 'emulator' 'system-images;android-36;google_apis;arm64-v8a'
avdmanager create avd --name MapFlip_API_36 --package 'system-images;android-36;google_apis;arm64-v8a' --device pixel_7
```

Persist the exports in `~/.zprofile` for new terminals. The ARM64 image is for Apple Silicon; use an x86_64 image on an Intel Mac. Gradle uses the repository's pinned Kotlin plugin; the Homebrew Kotlin CLI is optional for standalone experiments.

Start the device and install the online debug build:

```sh
android --no-metrics emulator start MapFlip_API_36
./gradlew :app:assembleOnlineDebug
adb install -r app/build/outputs/apk/online/debug/app-online-debug.apk
```

Android Studio is optional. The command-line SDK provides the build tools, adb, and emulator; see the [SDK manager documentation](https://developer.android.com/tools/sdkmanager) and [emulator documentation](https://developer.android.com/studio/run/emulator-commandline).
