# Play Store-এ দেওয়ার জন্য যা যা লাগবে

> হালনাগাদ: অ্যাপ এখন **Capacitor** দিয়ে তৈরি হয় — PWABuilder আর লাগে না।
> স্টোরে যাওয়ার নাম **Adult Hifz**, ভিতরের ভাষা আগের মতোই বাংলা।

---

## ১. সাইট (ঐচ্ছিক, কিন্তু কাজে লাগে)

Capacitor বিল্ডে সব ফাইল APK-র ভিতরেই থাকে, তাই স্টোরে দিতে সাইট **লাগে না**।
তবু সাইটটা রাখলে গোপনীয়তা নীতির লিংক আর ওয়েব সংস্করণ — দুটোই পাওয়া যায়।

- অ্যাপ: `https://israfil-hossen.github.io/Adult-hifz/hifz-1.0.8/`
- রুট (`/Adult-hifz/`) ওখানেই পাঠিয়ে দেয়
- গোপনীয়তা নীতি: `https://israfil-hossen.github.io/Adult-hifz/PRIVACY.html`

Pages-এর উৎস: **Settings → Pages → Deploy from a branch → master / (root)**।

আপডেটের নিয়ম: ফাইল বদলে push করুন, আর `hifz-1.0.8/sw.js`-এর `CACHE` মানটা
বদলান — তা না হলে পুরোনো সংস্করণ ক্যাশে আটকে থাকবে।

---

## ২. AAB বানানো (Capacitor)

**ক. সাইনিং কি — একবারই**

```
keytool -genkeypair -v -keystore adult-hifz.jks -keyalg RSA \
        -keysize 2048 -validity 10000 -alias adulthifz
```

⚠️ এই ফাইলটা হারালে **আর কোনোদিন** এই অ্যাপের আপডেট দেওয়া যাবে না। গুগল
অন্য কি মানবে না। দুই জায়গায় ব্যাকআপ রাখুন, রিপোতে কখনোই রাখবেন না।

**খ. `android/keystore.properties`** — `keystore.properties.example` কপি করে
পাসওয়ার্ড বসান। ফাইলটা `.gitignore`-এ আছে।

**গ. বিল্ড**

```
npx cap sync android
npm run bundle
```

ফলাফল: `android/app/build/outputs/bundle/release/app-release.aab`

> ডিবাগ APK কখনো স্টোরে দেবেন না — ওটা `CN=Android Debug` দিয়ে সাইন করা
> আর `debuggable`, তাই Play Protect ওটাকে "harmful" বলে বন্ধ করে দেয়।

---

## ৩. Play Console

এককালীন ফি **$২৫**।

### যা যা তৈরি আছে

| কী | কোথায় |
|---|---|
| আইকন ৫১২×৫১২ | `hifz-1.0.8/icon-512.png` |
| ফিচার গ্রাফিক ১০২৪×৫০০ | `store/feature-graphic.png` |
| গোপনীয়তা নীতি | উপরের লিংক |
| বিবরণ | নিচে |

### যা এখনো বানাতে হবে

- **ফোন স্ক্রিনশট — কমপক্ষে ২টি** (১০৮০×১৯২০ বা ফোনের নিজের মাপ)।
  ফোনে অ্যাপ চালিয়ে সরাসরি স্ক্রিনশট নিলেই হয়। ভালো হয় — আজকের পাতা,
  মুসহাফের পাতা, অগ্রগতি, আর অনুশীলনের পর্দা।

### অ্যাপের নাম

> **Adult Hifz** — হিফজ · দৈনিক সঙ্গী

### সংক্ষিপ্ত বিবরণ (৮০ অক্ষরের মধ্যে)

> প্রাপ্তবয়স্কদের জন্য গবেষণা-ভিত্তিক কোরআন হিফজ সঙ্গী · হিসাব অ্যাপ রাখে

### পূর্ণ বিবরণ

> বড় বয়সে হিফজ শুরু করার সবচেয়ে বড় বাধা মুখস্থ করা নয় — হিসাব রাখা। আজ কোন পৃষ্ঠা নতুন? গত সপ্তাহের কোনগুলো রিভিশন দিতে হবে? পুরোনোগুলোর মধ্যে কোন পারার পালা?
>
> এই অ্যাপ প্রতিদিন সকালে ওই হিসাবটা নিজে করে দেয়।
>
> • আজকের নতুন পৃষ্ঠা ও লাইন — আগের দিন যেখানে শেষ, ঠিক সেখান থেকে
> • মুসহাফের আসল পাতা (মাদানী ছাপা, হুবহু ১৫ লাইন), আজকের অংশ হাইলাইট করা
> • আয়াতভিত্তিক তিলাওয়াত, শব্দে শব্দে হাইলাইট, ধীর গতি, টুকরো লুপ
> • নিজের তিলাওয়াত রেকর্ড করে ক্বারীর সাথে মিলিয়ে দেখা
> • স্মৃতি থেকে বের করার পরীক্ষা — ক্রমে সাজানো, ফাঁক পূরণ, এরপর কোনটা
> • রিভিশন ইঞ্জিন — নির্ভুল পৃষ্ঠা দেরিতে ফেরে, দুর্বল পৃষ্ঠা কালই ফেরে
> • মিলে যাওয়া আয়াতের (মুতাশাবিহাত) সতর্কতা
> • পূর্ণ অনুবাদ ও শব্দার্থ — বাংলা, English, اردو, 中文
> • দৈনিক ও যিকিরের স্মরণ
> • ইন্টারনেট ছাড়াও চলে · কোনো অ্যাকাউন্ট নেই · কোনো বিজ্ঞাপন নেই · সব ডেটা আপনার ডিভাইসে
>
> পদ্ধতির প্রতিটি ধাপের পেছনে প্রতিষ্ঠিত স্মৃতি-গবেষণা আছে; অ্যাপের "রুটিন" অংশে উৎসগুলো দেওয়া আছে।
>
> অ্যাপটি হিসাব রাখে, শেখায় না। তাজবীদের জন্য একজন শিক্ষক লাগবেই।

---

## ৩ক. English store listing (default language = en-US)

Copy these straight into Play Console. Character limits are Play's own.

### App name — 30 max

```
Adult Hifz
```

### Short description — 80 max (this one is 79)

```
Quran memorisation for adults - daily lesson, and revision timed to your recall
```

### Full description — 4000 max

```
Starting hifz as an adult is rarely a problem of memory. It is a problem of bookkeeping.

Which page is new today? Which of last week's pages still need revising? Of everything you memorised months ago, whose turn is it?

Adult Hifz does that arithmetic every morning, so your time goes on reciting instead of planning.


WHAT IT DOES

• Today's lesson — the next lines, continuing exactly where you stopped yesterday
• The real mushaf page, with the true line breaks, your portion marked
• Ayah-by-ayah recitation with word-by-word highlighting, slower playback, and phrase looping
• Record your own recitation and compare it against the qari
• Retrieval tests — reorder the pieces, fill the gap, choose what comes next
• A revision engine that pushes clean pages further away and brings shaky ones back tomorrow
• Warnings for look-alike ayahs (mutashabihat), which adults confuse far more often than they forget
• Full translation and word-by-word meaning in বাংলা, English, اردو and 中文
• Daily and dhikr reminders


SIX MUSHAF LAYOUTS

Madani 15-line (1405 and 1421 prints), Indo-Pak 15-line, 16-line Taj, and two 13-line layouts. Choose the one you learned from. The line breaks are not a detail — they are the thing you are memorising.


BUILT AROUND HOW MEMORY ACTUALLY WORKS

Spacing, retrieval practice and delayed feedback are not decoration here. Each is drawn from established memory research, and the app cites its sources inside the Routine section so you can read them yourself.

The daily workload is measured rather than assumed. Instead of ramping up new lines by the calendar, the app learns from your own recorded minutes how long a new line costs you and how long revision costs you, then sets a target that fits the time you really have — and tells you how much you can hold at that pace.


PRIVATE BY DEFAULT

Works offline. No account. No ads. No tracking. Your record stays on your device unless you turn on backup yourself.


The app keeps the record. It does not teach. For tajwid you will still need a teacher.
```

### Category and contact

| Field | Value |
|---|---|
| App or game | App |
| Category | Education |
| Tags | Quran, Islam, Memorisation, Education |
| Contact email | israfil.crp@gmail.com |
| Website | https://israfil-hossen.github.io/Adult-hifz/ |
| Privacy policy | https://israfil-hossen.github.io/Adult-hifz/PRIVACY.html |

> বাংলা তালিকাটা পরে **Store listings → Add language → বাংলা (bn-BD)** থেকে
> যোগ করবেন, নিচের বাংলা বিবরণ দিয়ে।

---

## ৪. Data safety ফর্ম — সাবধানে

আগে এখানে লেখা ছিল "সব না"। ওটা **পুরোপুরি ঠিক নয়**।

ডিফল্টে সত্যিই কোনো ডেটা যায় না। কিন্তু ব্যবহারকারী চাইলে দুটো ব্যাকআপ
চালু করতে পারেন — নিজের কোড দিয়ে `sync.php`, অথবা Google Drive। তখন
অ্যাপের সব হিসাব সার্ভারে যায়। Play জিজ্ঞেস করে অ্যাপ **কী করতে পারে**,
ডিফল্টে কী করে তা নয়।

তাই এভাবে দিন:

| প্রশ্ন | উত্তর |
|---|---|
| Does your app collect or share user data? | **Yes** |
| কোন ধরনের? | App activity → App interactions (হিফজের দৈনিক হিসাব) |
| উদ্দেশ্য | App functionality (ব্যাকআপ ও ডিভাইস বদল) |
| ঐচ্ছিক না বাধ্যতামূলক? | **Optional** — ব্যবহারকারী নিজে চালু করেন |
| ট্রান্সমিশনে এনক্রিপ্ট? | **Yes** (HTTPS) |
| মুছতে পারেন? | **Yes** — সেটিংস থেকে |

মাইক্রোফোন: রেকর্ডিং ডিভাইসেই থাকে, কোথাও যায় না — এটা আলাদা করে বলে দিন।

ভুল Data safety ফর্ম রিজেকশনের সবচেয়ে সাধারণ কারণগুলোর একটা।

---

## ৫. ক্লোজড টেস্টিং — সময় লাগে, আগে শুরু করুন

২০২৩ সালের নভেম্বরের পরে খোলা **ব্যক্তিগত** ডেভেলপার অ্যাকাউন্টের জন্য
গুগলের শর্ত: প্রোডাকশনে যাওয়ার আগে **১২ জন টেস্টার, টানা ১৪ দিন** ক্লোজড
টেস্টিংয়ে থাকতে হবে।

এটাই সবচেয়ে লম্বা ধাপ। কি বানানোর পরপরই AAB আপলোড করে টেস্টিং শুরু করে
দিন — বাকি কাজ (স্ক্রিনশট, বিবরণ) ওই ১৪ দিনের ভিতরেই সারা যায়।

কোম্পানি অ্যাকাউন্ট হলে এই শর্ত নেই।

---

## চীন বা সীমিত ইন্টারনেটের দেশে

মূল ভূখণ্ডে `github.io`, `fonts.googleapis.com` ও `accounts.google.com` বন্ধ।

**অ্যাপের ভেতরে** — সেটিংসে **"সীমিত ইন্টারনেট মোড"** আছে। চীনের টাইমজোন হলে নিজে থেকেই চালু হয়। চালু থাকলে ফন্ট `fonts.loli.net` ও `cdn.jsdelivr.net` থেকে আসে, আর গুগল ড্রাইভ সিঙ্কের স্ক্রিপ্ট লোডই হয় না।

স্টোরের অ্যাপে পাতা-অনুবাদ-ফন্ট সবই ভিতরে থাকে, তাই এই সমস্যা মূলত ওয়েব
সংস্করণের। হোস্টিং লাগলে:

| কোথায় | চীনে | মন্তব্য |
|---|---|---|
| **Cloudflare Pages** | সাধারণত খোলে | GitHub রিপো যুক্ত করলে push-এই আপডেট |
| **Netlify / Vercel** | অনিয়মিত | মাঝে মাঝে ধীর বা বন্ধ |
| **Gitee Pages** | ভালো খোলে | বাস্তব নাম যাচাই লাগে |
| **阿里云 OSS / 腾讯云 COS** | সবচেয়ে ভালো | ডোমেইনে ICP নিবন্ধন লাগে |
