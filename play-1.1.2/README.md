# Play Store আপডেট — ভার্সন 1.1.2

এই ফোল্ডারে যা আছে সব তৈরি। শুধু নিচের ধাপগুলো মেনে Play Console-এ তুলুন।

| ফাইল | কী কাজে |
|---|---|
| `app-release.aab` | **Play Store-এ এটাই তুলবেন** (versionCode 8, versionName 1.1.2) |
| `release-notes-en-bn.txt` | **Release notes বক্সে হুবহু পুরোটা কপি-পেস্ট** (ইংরেজি + বাংলা) |
| `release-notes-en-only.txt` | বাংলা লিস্টিং যোগ করা না থাকলে এটা দিন |
| `Quran-Hifz-1.1.2-debug.apk` (প্রজেক্টের মূল ফোল্ডারে) | নিজের ফোনে বসিয়ে দেখার জন্য — Play Store-এ দেবেন না |
| `listing-en-US/`, `listing-bn-BD/` | নাম, বর্ণনা, "নতুন কী আছে" — কপি করে বসানোর লেখা |
| `graphics/icon-512.png` | Play Store-এর আইকন (নতুন হৃদয়-মুসহাফ লোগো) |
| `graphics/feature-graphic-1024x500.png` | Play Store-এর উপরের বড় ছবি |

সাইন করা হয়েছে আগের আপলোডের একই চাবি দিয়ে (SHA-256
`F1:8D:AB:AA:3C:D6:38:7C:C8:B2:14:FA:30:1E:A0:8D:54:FF:34:76:F9:3C:5B:FD:06:42:8C:BE:D8:CC:47:FA`),
তাই Play এটাকে নতুন অ্যাপ নয়, **আপডেট** হিসেবে নেবে।

এই ভার্সনে অ্যাপের আকার বেড়ে গেছে (~১৩ MB → ~৬২ MB): মাদানী মুসহাফের (১৪০৫ ছাপা) আসল হরফ এখন অ্যাপের ভিতরেই থাকে, তাই প্রথমবার পাতা খুলতেও ইন্টারনেট লাগে না। Play Console এই লাফটা দেখে অতিরিক্ত সময় নিতে পারে রিভিউতে — অস্বাভাবিক কিছু না।


---

## ধাপ ১ — নতুন রিলিজ তৈরি ও AAB তোলা

1. https://play.google.com/console খুলুন → আপনার অ্যাপটা বাছুন।
2. বাঁ দিকে **Test and release → Production** (বা আগে যে ট্র্যাকে তুলেছিলেন, যেমন Closed testing)।
3. **Create new release** চাপুন।
4. **App bundles** অংশে `app-release.aab` টেনে ছাড়ুন (বা Upload চেপে বাছুন)।
   আপলোড শেষে নিচে দেখাবে **8 (1.1.2)** — এটা দেখলেই ঠিক আছে।
5. **Release name**: নিজে থেকেই `8 (1.1.2)` বসবে, বদলানোর দরকার নেই।
6. **Release notes** বক্সের ভেতরে আগে যা আছে সব মুছে দিন। তারপর
   `release-notes-en-bn.txt` ফাইলটা খুলে **পুরোটা হুবহু** কপি করে বসান —
   `<en-US>` থেকে শেষের `</bn-BD>` পর্যন্ত, কিছু বাদ না দিয়ে, কিছু না যোগ করে।
7. **Next** → নিচে কোনো লাল ভুল না থাকলে **Save**।

### "Syntax error" দেখালে

Release notes বক্সের **প্রতিটা লেখা** একটা ভাষার ট্যাগের ভেতরে থাকতে হয়
(`<en-US>` … `</en-US>`)। ট্যাগের বাইরে একটা অক্ষরও থাকলে, বা `<bn-BD>` দিলেন
অথচ আপনার লিস্টিংয়ে বাংলা ভাষা যোগ করা নেই — তখন Play "syntax error" বলে।

- বক্স পুরো খালি করে `release-notes-en-bn.txt` আবার বসান।
- তাও একই ভুল দেখালে বাংলা লিস্টিং নেই — তখন `release-notes-en-only.txt` বসান।
- "Version code … has already been used" দেখালে: এই ফোল্ডারের নতুন AAB (versionCode 8) তুলুন, পুরনোটা না।
  (আগের 1.1.0 বা 1.1.1 তুলে থাকলেও বা না তুলে থাকলেও — 8 দুই ক্ষেত্রেই চলবে।)

## ধাপ ২ — নতুন নাম আর লোগো

**Grow users → Store presence → Main store listing**:

| ঘর | কী দেবেন |
|---|---|
| App name | `listing-en-US/app-name.txt` → **Quran: Adult Memorization** |
| App icon | `graphics/icon-512.png` |
| Feature graphic | `graphics/feature-graphic-1024x500.png` |

বাংলা লিস্টিং থাকলে (Store listings → বাংলা) সেখানে App name দিন
`listing-bn-BD/app-name.txt` থেকে: **কুরআন: প্রাপ্তবয়স্কদের হিফজ**।
**Save** চাপুন।

## ধাপ ৩ — নতুন ঘোষণা (একবারই লাগবে)

এই ভার্সনে ডাউনলোড অ্যাপের বাইরেও চলে, তাই Play জিজ্ঞেস করবে।
**Policy and programs → App content → Foreground service permissions**:

- **Data sync** টিক দিন, আর নিচের লেখাটা বসান:

> The user taps "Download" to save Quran recitation audio and page fonts for
> offline memorisation (a surah, a juz, or the whole Quran — often hundreds of
> files). The download must continue when the user leaves the app, and it shows
> a progress notification with a Stop button. It starts only when the user
> asks, and ends when the files are saved or the user stops it.

- ভিডিও চাইলে: ফোনে স্ক্রিন রেকর্ড চালু করে একটা পারা ডাউনলোড শুরু করুন →
  Home চাপুন → নোটিফিকেশন নামিয়ে অগ্রগতি আর থামান বাটন দেখান। ভিডিওটা
  YouTube-এ (Unlisted) তুলে লিংক দিন।
- আগের **Media playback** ঘোষণা যেমন আছে তেমনই থাকবে।

এই ভার্সনে সুন্নাহ রিমাইন্ডার (নির্দিষ্ট সময়ে নোটিফিকেশন) আছে — এর জন্য
নতুন কোনো ঘোষণা লাগে না, আগের "exact alarm" অনুমতিই যথেষ্ট।

"নিজের ক্বারী" (Music/Quran Hifz-এ নিজের বানানো ফোল্ডার) পড়ার জন্য আগের
অডিও-ফাইল পড়ার অনুমতিই (READ_MEDIA_AUDIO) ব্যবহার হয় — নতুন ঘোষণা লাগে না।

এই ভার্সনে "ব্যাকআপ" ফাইলটা ফোনের Download/Quran Hifz ফোল্ডারে সেভ হয় আর শেয়ার
করার সুযোগ দেয় — এর জন্য নতুন কোনো অনুমতি বা ঘোষণা লাগে না।

## ধাপ ৪ — রিভিউতে পাঠানো

**Publishing overview** (বাঁ দিকে) → সব পরিবর্তন তালিকায় দেখাবে →
**Send changes for review**।

রিভিউতে সাধারণত কয়েক ঘণ্টা থেকে ২–৩ দিন লাগে। পাস হলে ব্যবহারকারীরা
নিজে থেকেই আপডেট পাবেন, তাদের ডাউনলোড আর হিসাব সব থেকে যাবে।

---

## নিজের ফোনে আগে দেখতে চাইলে

`Quran-Hifz-1.1.2-debug.apk` (প্রজেক্টের মূল ফোল্ডারে) ফোনে পাঠিয়ে খুলুন। ফোনে Play Store-এর অ্যাপ বসানো
থাকলে যদি "App not installed" বলে, তাহলে Play Store-এর সাথে সাইনের
পার্থক্য আছে — সেক্ষেত্রে পুরোনোটা uninstall করে বসাতে হবে, অথবা সরাসরি
Play Store-এর আপডেটের জন্য অপেক্ষা করুন।

## পরের বার

প্রতি আপলোডে versionCode এক করে বাড়ে (এবার 8, পরের বার 9)। একই নম্বর
Play দ্বিতীয়বার নেয় না। `adult-hifz.jks` ফাইল আর তার পাসওয়ার্ড অবশ্যই
এই কম্পিউটারের বাইরেও কোথাও রেখে দিন — এটা হারালে এই অ্যাপ আর কখনো আপডেট
করা যাবে না।

নতুন AAB বানাতে: `cd android` → `gradlew bundleRelease` →
`android/app/build/outputs/bundle/release/app-release.aab` এই ফোল্ডারে কপি।
