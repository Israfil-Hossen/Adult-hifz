# হিফজ · দৈনিক সঙ্গী

প্রাপ্তবয়স্কদের জন্য একটা কোরআন হিফজ ট্র্যাকার। স্মৃতি-গবেষণার ভিত্তিতে সাজানো।

প্রতিদিন খুললেই অ্যাপ নিজে হিসাব করে বলে দেয় — আজ কোন পৃষ্ঠা নতুন, সাবকিতে ঠিক কোন সাতটা পৃষ্ঠা, আর মানজিলে কোন পারা। আপনাকে কিছু মনে রাখতে হয় না।

**ফাইল সাতটা।** সবগুলো একই ফোল্ডারে রাখতে হবে:

```
index.html          অ্যাপ (সবকিছু এর ভিতরে)
config.js           আপনার Google Client ID এখানে বসাবেন
manifest.json       ফোনে অ্যাপ হিসেবে বসার জন্য
sw.js               অফলাইনে চালানোর জন্য
icon.svg
icon-maskable.svg
README.md
```

---

# ধাপ ১ · GitHub-এ প্রকাশ

১. GitHub-এ লগ ইন করে ডানে উপরে **+** → **New repository**

২. নাম দিন `hifz`। **Public** বেছে নিন। **Create repository** চাপুন।

৩. **uploading an existing file** লিংকে ক্লিক করুন। উপরের সাতটা ফাইল টেনে ছেড়ে দিন। নিচে **Commit changes** চাপুন।

৪. উপরে **Settings** → বাঁ পাশে **Pages** →
   - **Source**: `Deploy from a branch`
   - **Branch**: `main` এবং `/ (root)`
   - **Save**

৫. দুই মিনিট অপেক্ষা করে পেজটা রিফ্রেশ করুন। উপরে আপনার লিংক দেখাবে:

```
https://আপনার-ইউজারনেম.github.io/hifz/
```

**এই লিংকটাই আপনার অ্যাপ।** এটাই মানুষকে দেবেন।

> ⚠️ ফাইলগুলো অবশ্যই root-এ থাকতে হবে, কোনো সাব-ফোল্ডারে নয়। সাব-ফোল্ডারে দিলে অফলাইন আর ইনস্টল — দুটোই কাজ করবে না।

---

# ধাপ ২ · Google Drive সিঙ্ক চালু করা

এটাই আপনার আসল সমস্যার সমাধান: ব্রাউজার পরিষ্কার করলেও ডেটা হারাবে না, আর ল্যাপটপ-ফোনে একই ডেটা থাকবে।

**সময় লাগবে দশ মিনিট। খরচ নেই। একবারই করতে হবে।**

### ২ক · Google Cloud-এ প্রকল্প বানান

১. [console.cloud.google.com](https://console.cloud.google.com/) এ যান, Google দিয়ে লগ ইন করুন

২. উপরে প্রকল্পের নামের জায়গায় ক্লিক → **New Project** → নাম দিন `Hifz` → **Create**

৩. উপরের সার্চ বাক্সে লিখুন **Google Drive API** → খুলে **Enable** চাপুন

### ২খ · সম্মতির পর্দা সাজান

৪. বাঁ মেনু থেকে **APIs & Services** → **OAuth consent screen**
   (নতুন কনসোলে এর নাম **Google Auth Platform → Branding**)

৫. **External** বেছে নিন → **Create**

৬. তিনটা ঘর ভরুন — বাকিগুলো খালি রাখলেও চলবে:
   - **App name**: `হিফজ দৈনিক সঙ্গী`
   - **User support email**: আপনার ইমেইল
   - **Developer contact**: আপনার ইমেইল

৭. Save করে এগোতে থাকুন, শেষে **Back to dashboard**

৮. ড্যাশবোর্ডে **PUBLISH APP** বোতাম থাকলে সেটা চাপুন → **Confirm**

> **এটা কেন জরুরি:** না চাপলে সর্বোচ্চ ১০০ জন ব্যবহার করতে পারবে। চাপলে সীমা উঠে যায়। এই অ্যাপ শুধু `drive.file` অনুমতি চায় — যেটা Google-এর "সংবেদনশীল নয়" শ্রেণিতে পড়ে, তাই কোনো যাচাই বা নিরীক্ষা লাগে না।

### ২গ · Client ID বানান

৯. **APIs & Services** → **Credentials** → উপরে **+ CREATE CREDENTIALS** → **OAuth client ID**

১০. **Application type**: `Web application`

১১. **Authorized JavaScript origins** এ **+ ADD URI** চেপে বসান:

```
https://আপনার-ইউজারনেম.github.io
```

> ⚠️ **সবচেয়ে বেশি ভুল এখানেই হয়।** শুধু ডোমেইনটুকু — `/hifz/` অংশটা **দেবেন না**। শেষে স্ল্যাশও না।
>
> Redirect URI-এর ঘরটা **খালি রাখুন**, ওটা লাগবে না।

১২. **Create** চাপুন। একটা লম্বা লেখা দেখাবে, শেষে `.apps.googleusercontent.com`। **কপি করুন।**

### ২ঘ · অ্যাপে বসান

১৩. GitHub-এ আপনার repo-তে যান → `config.js` ফাইলে ক্লিক → পেন্সিল আইকন (✏️) চাপুন

১৪. এই লাইনটা খুঁজুন:

```js
googleClientId: "",
```

কপি করা লেখাটা কোটেশনের ভিতরে বসান:

```js
googleClientId: "8281xxxxxxxx-xxxxxxxxxx.apps.googleusercontent.com",
```

১৫. নিচে **Commit changes** চাপুন।

১৬. এক মিনিট পর অ্যাপ খুলুন। উপরে **সিঙ্ক** বোতাম দেখা যাবে। সেটিংস ট্যাবে গিয়ে **Google দিয়ে যুক্ত হোন** চাপুন।

**হয়ে গেল।** এখন থেকে প্রতিটা পরিবর্তন কয়েক সেকেন্ডের মধ্যে আপনার ড্রাইভের `hifz-backup.json` ফাইলে চলে যাবে।

---

## সিঙ্ক নিয়ে কয়েকটা কথা

**অ্যাপ আপনার ড্রাইভের কী দেখতে পায়?** শুধু নিজের বানানো ওই একটা ফাইল। `drive.file` অনুমতির মানেই তাই — আপনার ছবি, ডকুমেন্ট, বাকি কিছুই অ্যাপের চোখে পড়ে না। এটা Google-এর দিক থেকেই আটকানো, আমার কথার উপর নির্ভর করতে হবে না।

**অন্যরা ব্যবহার করলে তাদের ডেটা কোথায় যায়?** যার যার নিজের ড্রাইভে। আপনি কিছুই দেখতে পাবেন না। শুধু সম্মতির পর্দায় আপনার প্রকল্পের নামটা দেখাবে।

**দুই ডিভাইসে একসাথে কাজ করলে?** যেটায় শেষে পরিবর্তন হয়েছে, সেটাই থাকবে। তাই এক ডিভাইসে কাজ শেষ করে অন্যটা খুলুন। খুললেই ড্রাইভ থেকে নতুনটা নিজে নেমে আসবে।

**"Google hasn't verified this app" লেখা এলে?** **Advanced** → **Go to … (unsafe)** চাপুন। অ্যাপটা আপনারই বানানো, আর কোনো সংবেদনশীল অনুমতি চায় না।

### ইমেইলে অটোমেটিক পাঠানো কেন নেই

Gmail থেকে অটোমেটিক ইমেইল পাঠাতে গেলে Google-এর `gmail.send` অনুমতি লাগে, যেটা "restricted" শ্রেণির — প্রতি বছর কয়েক হাজার ডলারের নিরাপত্তা নিরীক্ষা করাতে হয়। শুধু ব্যাকআপের জন্য ওই খরচ অর্থহীন, কারণ ড্রাইভ সিঙ্কে ঠিক একই নিরাপত্তা পাওয়া যাচ্ছে — ডেটা আপনার Google অ্যাকাউন্টেই থাকছে।

চাইলে সেটিংস থেকে যেকোনো সময় JSON ফাইল নামিয়ে নিজেকে ইমেইল করতে পারেন। সেটা এক ক্লিকের কাজ।

---

# ধাপ ৩ · ইনস্টল

### ফোনে (অ্যান্ড্রয়েড)

১. Chrome-এ আপনার লিংকটা খুলুন
২. উপরে ডানে তিন-ডট মেনু (⋮)
৩. **Add to Home screen** বা **Install app**
৪. **Install** চাপুন

### ফোনে (আইফোন)

১. **Safari**-তে লিংকটা খুলুন *(Chrome-এ কাজ করবে না — আইফোনে অবশ্যই Safari)*
২. নিচে Share বোতাম (⬆️)
৩. নিচে স্ক্রল করে **Add to Home Screen**
৪. **Add**

### ল্যাপটপে (Chrome / Edge)

১. লিংকটা খুলুন
২. অ্যাড্রেস বারের ডান কোণে ইনস্টল আইকন (⊕ বা মনিটরের মতো ছবি)
৩. **Install**

ইনস্টলের পর আলাদা উইন্ডোতে খুলবে, অ্যাড্রেস বার থাকবে না, আর **ইন্টারনেট ছাড়াই চলবে** — ফজরের সময় ডেটা না থাকলেও সমস্যা নেই।

> ইনস্টল করার পর প্রতিটা ডিভাইসে একবার সেটিংস → **Google দিয়ে যুক্ত হোন** চাপতে হবে। এরপর সব ডিভাইসে একই ডেটা।

---

# ধাপ ৪ · মানুষের মাঝে ছড়ানো

লিংকটা শেয়ার করাই যথেষ্ট। তবে কয়েকটা জিনিস করলে অনেক বেশি মানুষ আসলেই ব্যবহার শুরু করবে:

### ক · শুধু লিংক নয়, ইনস্টলের কথাটাও বলুন

বেশিরভাগ মানুষ লিংক খুলে দেখে, তারপর ভুলে যায়। "হোম স্ক্রিনে যোগ করুন" কথাটা না বললে অ্যাপটা অ্যাপ হয়ে ওঠে না।

নিচের লেখাটা কপি করে WhatsApp বা ফেসবুকে দিতে পারেন:

```
আসসালামু আলাইকুম।

বড় বয়সে যারা হিফজ শুরু করতে চান, তাদের জন্য একটা ছোট
অ্যাপ বানিয়েছি। বিনামূল্যে, কোনো বিজ্ঞাপন নেই, লগ ইন
ছাড়াও চলে।

প্রতিদিন খুললেই বলে দেয় — আজ কোন পৃষ্ঠা নতুন, কোন সাতটা
পৃষ্ঠা রিভিশন, আর কোন পারা মুরাজাআ। হিসাব রাখতে হয় না।

👉 [আপনার লিংক]

ফোনে খুলে মেনু থেকে "Add to Home Screen" দিলে অ্যাপের
মতো বসে যাবে, ইন্টারনেট ছাড়াও চলবে।
```

### খ · repo-টা সাজিয়ে রাখুন

GitHub-এ repo-র উপরে ⚙️ চেপে:
- **Description**: `প্রাপ্তবয়স্কদের জন্য গবেষণা-ভিত্তিক কোরআন হিফজ ট্র্যাকার`
- **Website**: আপনার Pages লিংক
- **Topics**: `quran`, `hifz`, `memorization`, `bangla`, `pwa`

এতে GitHub-এ কেউ খুঁজলে পাবে, আর repo-র লিংক দিলেই অ্যাপের লিংক দেখা যাবে।

### গ · একটা QR কোড বানান

মসজিদে বা মাদরাসায় কাগজে ছাপিয়ে দিতে পারেন। যেকোনো ফ্রি QR জেনারেটরে আপনার লিংকটা দিলেই হবে। মুখে লিংক বলার চেয়ে এটা অনেক কাজে দেয়।

### ঘ · স্ক্রিনশট যোগ করুন

ফোন থেকে "আজ" আর "অগ্রগতি" ট্যাবের দুটো স্ক্রিনশট নিয়ে repo-তে আপলোড করুন, তারপর README-র উপরে বসিয়ে দিন:

```markdown
![আজকের রুটিন](screenshot-today.png)
![অগ্রগতি](screenshot-progress.png)
```

মানুষ ছবি দেখে যত দ্রুত বোঝে, লেখা পড়ে তত না।

### ঙ · যাদের কাছে দেবেন

সবচেয়ে ভালো ফল পাবেন — মসজিদের ইমাম ও খতিব, মাদরাসার শিক্ষক, প্রাপ্তবয়স্কদের হিফজ হালাকা, আর কর্মজীবী মানুষের ইসলামিক গ্রুপে। **সাপ্তাহিক রিপোর্ট** ফিচারটার কথা শিক্ষকদের আলাদা করে বলবেন — ছাত্রের সপ্তাহের হিসাব এক পাতায় প্রিন্ট হয়ে আসে, নিচে মন্তব্য আর স্বাক্ষরের ঘরসহ।

---

# অ্যাপে কী কী আছে

| ট্যাব | কী |
|---|---|
| **আজ** | দিনের ছয়টি ব্লক, গেট-পরীক্ষা, টাইমার, ভুলের হিসাব, নোট |
| **রুটিন** | পূর্ণ পদ্ধতি — র‍্যাম্প, আটকে যাওয়ার প্রোটোকল, মুতাশাবিহাত, ভ্রমণ-অসুস্থতার নিয়ম, গবেষণা সূত্র |
| **অগ্রগতি** | ৬০৪-পৃষ্ঠার মুসহাফ মানচিত্র, ৮৪ দিনের হিটম্যাপ, পারা-ভিত্তিক অগ্রগতি, সমাপ্তির প্রক্ষেপণ, সাপ্তাহিক রিপোর্ট |
| **সেটিংস** | শুরুর তারিখ, দৈনিক লক্ষ্য, বিশ্রামের দিন, দুর্বল পৃষ্ঠা, ড্রাইভ সিঙ্ক, ফাইল ব্যাকআপ |

### অ্যাপ যা নিজে হিসাব করে

- **র‍্যাম্প** — শুরুর তারিখ থেকে সপ্তাহ গুনে দৈনিক লক্ষ্য (৫ → ৮ → ১১ → ১৫ লাইন)
- **সাবকির সারি** — গত ৭টি সবক-দিনের পৃষ্ঠা, বিশ্রামের দিন বাদ দিয়ে
- **মানজিলের ঘূর্ণন** — সপ্তাহে এক পারা, শুধু যেসব পারায় মুখস্থ আছে
- **গেট** — গতকালের পৃষ্ঠা ফেল করলে আজকের নতুন সবক নিজে থেকেই বন্ধ হয়ে যায়
- **সমাপ্তির প্রক্ষেপণ** — বর্তমান গতিতে কবে ৩০ পারা শেষ হবে

---

# নিজের মতো বদলাতে চাইলে

সবকিছু `index.html`-এর ভিতরেই — আলাদা কোনো build বা npm লাগে না।

| কী বদলাবেন | কোথায় |
|---|---|
| রঙ, ফন্ট | ফাইলের শুরুর `:root` ব্লক |
| র‍্যাম্পের ধাপ | `targetLines()` |
| সাবকিতে কয় দিন | `sabaqiQueue()` |
| মানজিলের ঘূর্ণন | `manzilJuz()` |
| ব্লকগুলোর ড্রিল-লেখা | `renderToday()` |
| রিপোর্টের চেহারা | `openReport()` |

**আপডেট করার পর অবশ্যই:** `sw.js` ফাইলের `CACHE = "hifz-v2"` লাইনের সংখ্যাটা বাড়িয়ে `hifz-v3` করুন। না করলে মানুষের কাছে পুরোনো সংস্করণ ক্যাশে আটকে থাকবে।

---

# সমস্যা হলে

| যা হচ্ছে | কারণ ও সমাধান |
|---|---|
| সিঙ্ক বোতামই দেখাচ্ছে না | `config.js`-এ Client ID বসেনি বা কোটেশন ভুল |
| `redirect_uri_mismatch` / `origin` ভুল | Authorized JavaScript origins এ `/hifz/` অংশটা ঢুকে গেছে। শুধু `https://ইউজারনেম.github.io` রাখুন |
| যুক্ত হয় কিন্তু সিঙ্ক হয় না | Google Drive API **Enable** করা হয়নি |
| ১০০ জনের পর কেউ ঢুকতে পারছে না | সম্মতির পর্দা এখনো Testing-এ। **PUBLISH APP** চাপুন |
| পুরোনো সংস্করণ দেখাচ্ছে | `sw.js`-এর CACHE সংখ্যা বাড়াননি |
| আইফোনে ইনস্টল হচ্ছে না | Chrome-এ চেষ্টা করছেন। আইফোনে অবশ্যই Safari |

---

# একটা কথা

অ্যাপটা হিসাব রাখে, শেখায় না। **তাজবীদের জন্য একজন শিক্ষক লাগবেই** — সপ্তাহে অন্তত একদিন শুনিয়ে নিন। ভুল উচ্চারণে মুখস্থ হয়ে গেলে সেটা ঠিক করা নতুন করে মুখস্থ করার চেয়েও কঠিন।

---

<details>
<summary><b>English summary</b></summary>

A research-based Qur'an memorization tracker for adult learners, in Bengali.

**Deploy:** upload all files to the root of a public repo → Settings → Pages → Deploy from branch → `main` / `/ (root)`.

**Stack:** one HTML file, no build step, no dependencies, no server. Installable as a PWA, works fully offline via service worker.

**Storage:** `localStorage` by default, with optional Google Drive sync via Google Identity Services and the Drive REST API — entirely client-side. Uses only the `drive.file` scope, so the app can access nothing in the user's Drive except the single backup file it creates. Because `drive.file` is a non-sensitive scope, no Google verification or security assessment is required; publishing the consent screen lifts the 100-user testing cap. Set your OAuth client ID in `config.js`; the authorized JavaScript origin must be the bare origin (`https://user.github.io`), not the repo path.

Gmail auto-send is deliberately not implemented: `gmail.send` is a restricted scope requiring an annual third-party security assessment, which is not worth it when Drive sync provides the same guarantee. JSON export/import remains available as a manual fallback.

**What it computes automatically:** the daily line target (a six-week ramp), the seven-day *sabaqi* review queue, the weekly *manzil* juz rotation, a gate rule that blocks new memorization when yesterday's page was not recalled cleanly, and a printable weekly report for a teacher.

**Method sources:** Craik & Lockhart (1972) on depth of processing; Karpicke & Roediger (2008) on retrieval practice; Ebbinghaus (1885) and Cepeda et al. (2006) on spaced repetition; Miller (1956) on chunking; Maguire et al. (2000, 2003) and Dresler et al. (2017) on spatial memory; Paivio (1986) and Shams & Seitz (2008) on multisensory encoding.

Free to use, adapt, and share.

</details>
