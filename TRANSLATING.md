# নতুন ভাষা যোগ করা · Adding a language

অ্যাপের সব লেখা `index.html`-এর উপরের দিকে, আলাদা ব্লকে। বাকি কোডে হাত না দিয়েই যেকোনো ভাষা যোগ করা যায়।

বর্তমানে আছে: **বাংলা** (`bn`) · **English** (`en`) · **اردو** (`ur`) · **中文** (`zh`)

---

## তিন ধাপে নতুন ভাষা

চার ভাষাই এখন `index.html`-এর **ভেতরে**। ফাইলের উপরের দিকে খুঁজুন — পরপর চারটা ব্লক:

```
window.HIFZ_LANG_en = { ... }
window.HIFZ_LANG_bn = { ... }
window.HIFZ_LANG_ur = { ... }
window.HIFZ_LANG_zh = { ... }
```

### ১ · একটা ব্লক কপি করুন

`window.HIFZ_LANG_en = {` থেকে তার শেষ `};` পর্যন্ত পুরোটা কপি করে ঠিক নিচে বসান, নতুন `<script>` ট্যাগের ভেতরে।

### ২ · নাম ও সেটিংস বদলান

```js
window.HIFZ_LANG_tr = {       // ← ভাষার কোড, যেমন তুর্কির জন্য tr
  code: "tr",
  name: "Türkçe",             // পিকারে যা দেখাবে — নিজের ভাষাতেই
  dir: "ltr",                 // আরবি, উর্দু, ফারসি, হিব্রু হলে "rtl"
  digits: "0123456789",       // ভাষার নিজস্ব অঙ্ক থাকলে বসান
  locale: "tr-TR",
  fonts: {
    google: "Spectral:wght@400;500&family=Inter:wght@400;600",
    display: '"Spectral", Georgia, serif',
    body: '"Inter", system-ui, sans-serif',
    lineHeight: "1.7"         // নাস্তালিকের জন্য ২.০+, চীনার জন্য ১.৮৫
  },
```

তারপর ডান পাশের লেখাগুলো অনুবাদ করুন।

### ৩ · তালিকায় যোগ করুন

একই ফাইলে নিচের দিকে একটামাত্র লাইন:

```js
var LANGS = ["bn", "en", "ur", "zh"];   →   var LANGS = ["bn", "en", "ur", "zh", "tr"];
```

শেষে `sw.js`-এ `CACHE` সংখ্যাটা এক বাড়িয়ে দিন, যাতে সবার ফোনে নতুন সংস্করণ পৌঁছায়।

**ব্যস।** পিকারে নতুন ভাষাটা চলে আসবে।

## অনুবাদের নিয়ম

**অনুবাদ করবেন না:**

| জিনিস | উদাহরণ | কেন |
|---|---|---|
| বাঁ পাশের কী | `gate:`, `sabaq:` | কোড এগুলো দিয়েই খোঁজে |
| প্লেসহোল্ডার | `{0}` `{1}` `{2}` | অ্যাপ এখানে সংখ্যা বসায় |
| HTML ট্যাগ | `<b>` `</b>` `<br>` | নাহলে লেখা ভেঙে যাবে |
| গবেষকদের নাম | `Craik & Lockhart 1972` | আন্তর্জাতিক রেফারেন্স |

**প্লেসহোল্ডারের ক্রম বদলানো যাবে**, শুধু সংখ্যাগুলো ঠিক রাখলেই হবে:

```js
// ইংরেজি
week: "<b>This week's juz: {0}</b> — pages {1}–{2}."
// অনুবাদে ক্রম উল্টে গেলেও সমস্যা নেই
week: "<b>{1}–{2} সংখ্যক পৃষ্ঠা — এই সপ্তাহের পারা {0}।</b>"
```

**অ্যারের দৈর্ঘ্য বদলাবেন না।** `drill` এ ঠিক ৫টা, `stats` এ ৬টা, `legend` এ ৪টা — যতগুলো ইংরেজিতে আছে ততগুলোই রাখতে হবে।

**অর্ধেক করলেও চলবে।** যে কী বাদ পড়বে, সেটা নিজে থেকেই ইংরেজিতে দেখাবে। তাই একবারে সব শেষ করার দরকার নেই।

---

## অনুবাদ ঠিক আছে কি না দেখা

ব্রাউজারে অ্যাপটা খুলে **F12** চেপে Console-এ এটা চালান — কোন কী বাদ পড়েছে, কোথায় প্লেসহোল্ডার মেলেনি, ধরিয়ে দেবে:

```js
(code => {
  const walk = (o, p, out) => { for (const k in o) { const v = o[k], q = p ? p+"."+k : k;
    if (Array.isArray(v)) out[q] = "array:"+v.length;
    else if (v && typeof v === "object") walk(v, q, out); else out[q] = typeof v; } return out; };
  const get = (o, p) => p.split(".").reduce((a,b) => a && a[b], o);
  const en = walk(window.HIFZ_LANG_en, "", {}), t = walk(window["HIFZ_LANG_"+code], "", {});
  const r = s => (String(s||"").match(/\{\d\}/g)||[]).sort().join();
  console.log("বাদ পড়েছে :", Object.keys(en).filter(k => !(k in t)).join(", ") || "কিছু না");
  console.log("দৈর্ঘ্য ভুল :", Object.keys(en).filter(k => k in t && t[k] !== en[k]).join(", ") || "কিছু না");
  console.log("প্লেসহোল্ডার:", Object.keys(en).filter(k => en[k] === "string" && k in t &&
    r(get(window.HIFZ_LANG_en, k)) !== r(get(window["HIFZ_LANG_"+code], k))).join(", ") || "ঠিক আছে");
})("tr");   // ← আপনার ভাষার কোড
```

## ডান-থেকে-বামে ভাষা

`dir: "rtl"` দিলে পুরো লেআউট নিজে থেকেই উল্টে যায় — CSS-এ যৌক্তিক নিয়ম (`border-inline-start`, `text-align:start`) ব্যবহার করা হয়েছে, তাই আলাদা কিছু করতে হয় না।

শুধু **মুসহাফের পৃষ্ঠা-নকশাটা** সবসময় ডান দিক থেকেই শুরু হবে, ভাষা যাই হোক — কারণ ওটা আরবি লিপির প্রতিরূপ, ইন্টারফেস নয়।

নাস্তালিক লিপির (উর্দু, ফারসি) জন্য `lineHeight` অন্তত `2.0` রাখবেন, নাহলে অক্ষরের লেজ কেটে যায়।

---

## সাহায্য করতে চান?

অনুবাদ করে পাঠাতে চাইলে GitHub-এ Pull Request দিন, অথবা ফাইলটা ইস্যুতে জুড়ে দিন।

**যেসব ভাষার চাহিদা সবচেয়ে বেশি:** العربية · Türkçe · Bahasa Indonesia · Français · Español · Hausa · Soomaali · فارسی · Melayu · Kiswahili

---

<details>
<summary><b>English</b></summary>

All four languages live inline near the top of `index.html`. Adding one never requires touching application logic.

**Three steps:**

1. Near the top of `index.html`, copy the whole `window.HIFZ_LANG_en = { ... };` block into a new `<script>` tag below it.
2. Rename it to `window.HIFZ_LANG_xx`, set `code`, `name`, `dir`, `digits`, `locale` and `fonts`, then translate the values.
3. Add `"xx"` to the `LANGS` array further down the same file, and bump `CACHE` in `sw.js`.

**Never translate:** the keys on the left, `{0}`-style placeholders, HTML tags, or researcher names. Placeholder *order* may change to suit your grammar; the numbers must all still appear.

**Array lengths must match English** — `drill` has 5 items, `stats` 6, `legend` 4, and so on.

**Partial translations are fine.** Any key you omit falls back to English at runtime.

**RTL languages:** set `dir: "rtl"` and the layout mirrors itself — the CSS uses logical properties throughout. The mushaf page diagram stays right-to-left in every language, because it represents Arabic script rather than interface text. For Nastaliq scripts use `lineHeight` of at least `2.0`.

Pull requests welcome.

</details>
