# Cinejoy for Google TV (Android 11)

A WebView wrapper around https://cinejoy.pk/ with Android TV support:

- Shows up on the Google TV home screen (Leanback launcher + banner)
- On-screen pointer controlled by the remote: D-pad moves it, OK/Center clicks,
  push against the top/bottom edge to scroll
- Back button: exits fullscreen video, then goes back in history, then closes the app
- Menu button: jump back to the home page
- Fullscreen video supported

## Build the APK

### Option A: Android Studio
1. Open this folder in Android Studio (Hedgehog or newer).
2. Let it sync, then Build > Build Bundle(s) / APK(s) > Build APK(s).
3. APK: `app/build/outputs/apk/debug/app-debug.apk`

### Option B: GitHub Actions (no local setup)
1. Create a GitHub repo and push this folder to the `main` branch.
2. Open the Actions tab > "Build APK" > Run workflow.
3. Download `cinejoy-tv-apk` from the finished run.

## Install on the TV
1. On Google TV: Settings > System > About > tap "Android TV OS build" 7 times, then
   Settings > Apps > Security & restrictions > Unknown sources: allow "Downloader" (or your file manager).
2. Put the APK on a USB stick, or host it and open it with the Downloader app, or use adb:
   `adb connect <tv-ip>:5555` then `adb install app-debug.apk`
3. The app appears under "Apps" (use "Open app" / "Apps" row; it may need to be added to favorites).
