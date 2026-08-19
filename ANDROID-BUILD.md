# Building the Android APK/AAB on your laptop (no PWABuilder)

## Is my laptop enough?

| Route | Disk | RAM while building | Notes |
| --- | --- | --- | --- |
| **Light** (Bubblewrap, no Android Studio) | ~2.5 GB | ~1.5–2 GB | 4 GB RAM laptop is fine |
| Full Android Studio | ~10–12 GB | 6–8 GB | only needed for the visual IDE / emulator |

**Never run the emulator** — install the APK on your real phone with a USB cable instead. That is what makes the difference.

### Light setup (skip Android Studio entirely)

1. Node.js LTS — ~100 MB
2. JDK 17 (Temurin) — ~300 MB
3. Android **command-line tools only** (https://developer.android.com/studio#command-tools, the "Command line tools only" zip at the bottom) — ~150 MB, then:

```bash
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```
≈ 1.5 GB more. Bubblewrap can also install these for you when it asks.

Then follow **Route A** below. Bubblewrap builds from the terminal — no IDE, no emulator, a couple of minutes per build.

---

## What to download

1. **Node.js LTS** — https://nodejs.org (gives you `npm`/`npx`)
2. **JDK 17** — https://adoptium.net (Temurin 17)
3. **Android Studio** — https://developer.android.com/studio
   - On first run, in the SDK Manager install: **Android SDK Platform 35**, **Build-Tools 35**, **Platform-Tools**, **Command-line Tools**
4. Set env vars (once):
   - `JAVA_HOME` → your JDK 17 folder
   - `ANDROID_HOME` → `C:\Users\<you>\AppData\Local\Android\Sdk` (Win) / `~/Library/Android/sdk` (mac)

That's all the tooling. Then pick one of two routes.

---

## Route A — Bubblewrap (recommended: same TWA output as PWABuilder, from CLI)

The app must be live over HTTPS first (Netlify), because a TWA loads the hosted URL.

```bash
npm i -g @bubblewrap/cli
bubblewrap init --manifest https://YOUR-SITE.netlify.app/manifest.json
bubblewrap build          # produces app-release-signed.apk + app-release-bundle.aab
```

`init` asks for package id (e.g. `app.hifz.mushaf`), app name, colors, and creates/uses a signing keystore — **back up that keystore file and its passwords**, you need them for every future update.

Digital Asset Links (removes the browser URL bar):
```bash
bubblewrap fingerprint generateAssetLinks
```
Put the generated `assetlinks.json` at `/.well-known/assetlinks.json` on Netlify, then rebuild.

Install to a phone: `adb install app-release-signed.apk`

---

## Route B — Capacitor (bundles the app files inside the APK, works fully offline from install)

Run in a folder containing `index.html`, `sw.js`, `manifest.json`, `config.js`, icons:

```bash
npm init -y
npm i @capacitor/core @capacitor/cli @capacitor/android
npx cap init "Hifz" app.hifz.mushaf --web-dir=.
npx cap add android
npx cap sync
npx cap open android      # opens Android Studio
```

In Android Studio: **Build → Generate Signed Bundle / APK** → create a keystore → release build.

Route B needs no server, but audio/text fetched from the network still needs a connection until cached.

---

## Which to choose

- Publishing to Play Store + site already on Netlify → **Route A**.
- Want a standalone app with no hosting at all → **Route B**.
