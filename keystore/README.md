# Release signing (Play Store upload)

`upload.jks` is the **upload key** for Google Play. It lives here, git-ignored, along with
`keystore.properties` (store/key passwords, mode 600).

`app/build.gradle.kts` reads those properties and signs `release`; if the file is missing it
falls back to environment variables (`PLAY_KEYSTORE_PATH`, `PLAY_KEYSTORE_PASSWORD`,
`PLAY_KEY_ALIAS`, `PLAY_KEY_PASSWORD`). With neither present the project still configures,
builds debug APKs and runs tests (that is what CI does) - a release build then falls back to the
debug key rather than failing configuration, so never ship that variant as a real release.

## Build

```bash
export JAVA_HOME=/opt/data/home/tools/jdk17 ANDROID_HOME=/opt/data/home/tools/android-sdk
./gradlew assembleRelease   # release APK, minified + signed (testable on a device)
./gradlew bundleRelease     # AAB - this is what you upload to Play Console
```

## Keep safe

- Back up `upload.jks` and the passwords somewhere offline. **If you lose the upload key you can
  never update the app again** - Google cannot reset it (you would have to publish a new app with
  a new package name).
- The certificate subject is `Zozac Developments Pty Ltd`, matching the Play Console organisation.
- Play App Signing: enrol and Google re-signs downloads with its own key. Your upload key only
  signs what you hand to Play.
