# নতুন ভাষা যোগ করা · Adding a language

অ্যাপের সব লেখা `lang/` ফোল্ডারে। কোডে হাত না দিয়েই যেকোনো ভাষা যোগ করা যায়।

বর্তমানে আছে: **বাংলা** (`bn`) · **English** (`en`) · **اردو** (`ur`) · **中文** (`zh`)

---

## তিন ধাপে নতুন ভাষা

### ১ · ফাইল কপি করুন

`lang/en.js` কপি করে ভাষার কোড নামে রাখুন — যেমন তুর্কির জন্য `lang/tr.js`, আরবির জন্য `lang/ar.js`, ইন্দোনেশিয়ার জন্য `lang/id.js`।

ভাষার কোড দুই অক্ষরের ([ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes) তালিকা)।

### ২ · উপরের নামটা বদলান

ফাইলের প্রথম লাইনে:

```js
window.HIFZ_LANG_en = {     →     window.HIFZ_LANG_tr = {
```

তারপর মাথার সেটিংসগুলো:

```js
code: "tr",
name: "Türkçe",              // পিকারে যে নামটা দেখাবে — নিজের ভাষাতেই লিখুন
dir: "ltr",                  // আরবি, উর্দু, ফারসি, হিব্রু হলে "rtl"
digits: "0123456789",        // ভাষার নিজস্ব অঙ্ক থাকলে বসান
locale: "tr-TR",
fonts: {
  google: "Spectral:wght@400;500&family=Inter:wght@400;600",
  display: '"Spectral", Georgia, serif',
  body: '"Inter", system-ui, sans-serif',
  lineHeight: "1.7"          // নাস্তালিক লিপির জন্য ২.০+, চীনার জন্য ১.৮৫
}
```

তারপর ডান পাশের লেখাগুলো অনুবাদ করুন।

### ৩ · তালিকায় যোগ করুন

`index.html` ফাইলে একটামাত্র লাইন:

```js
var LANGS = ["bn", "en", "ur", "zh"];      →     var LANGS = ["bn", "en", "ur", "zh", "tr"];
```

শেষে `sw.js`-এ `"./lang/tr.js"` যোগ করুন আর `CACHE` সংখ্যাটা বাড়িয়ে দিন।

**ব্যস।** ভাষা পিকারে নতুন নামটা চলে আসবে।

---

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

## অনুবাদ যাচাই করার স্ক্রিপ্ট

Node ইনস্টল থাকলে ফোল্ডারে এই ফাইলটা বানিয়ে চালান — কোন কী বাদ পড়েছে, কোথায় প্লেসহোল্ডার মেলেনি, সব ধরিয়ে দেবে:

```js
// check.js  —  node check.js tr
const fs = require("fs");
const code = process.argv[2];
global.window = {};
eval(fs.readFileSync("lang/en.js", "utf8"));
eval(fs.readFileSync("lang/" + code + ".js", "utf8"));

const walk = (o, pre, out) => {
  for (const k in o) {
    const v = o[k], p = pre ? pre + "." + k : k;
    if (Array.isArray(v)) out[p] = "array:" + v.length;
    else if (v && typeof v === "object") walk(v, p, out);
    else out[p] = typeof v;
  }
  return out;
};
const get = (o, p) => p.split(".").reduce((a, b) => a && a[b], o);
const en = walk(window.HIFZ_LANG_en, "", {});
const tr = walk(window["HIFZ_LANG_" + code], "", {});

const missing = Object.keys(en).filter(k => !(k in tr));
const wrongLen = Object.keys(en).filter(k => k in tr && tr[k] !== en[k]);
const badPh = Object.keys(en).filter(k => {
  if (en[k] !== "string" || !(k in tr)) return false;
  const r = s => (String(s || "").match(/\{\d\}/g) || []).sort().join();
  return r(get(window.HIFZ_LANG_en, k)) !== r(get(window["HIFZ_LANG_" + code], k));
});

console.log("বাদ পড়েছে :", missing.length ? missing.join(", ") : "কিছু না");
console.log("দৈর্ঘ্য ভুল :", wrongLen.length ? wrongLen.join(", ") : "কিছু না");
console.log("প্লেসহোল্ডার:", badPh.length ? badPh.join(", ") : "ঠিক আছে");
```

---

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

All user-facing text lives in `lang/`. Adding a language never requires touching application code.

**Three steps:**

1. Copy `lang/en.js` to `lang/xx.js` (ISO 639-1 code).
2. Rename `window.HIFZ_LANG_en` to `window.HIFZ_LANG_xx`, set `code`, `name`, `dir`, `digits`, `locale`, and `fonts`, then translate the values.
3. Add `"xx"` to the `LANGS` array in `index.html`, add `"./lang/xx.js"` to `sw.js`, and bump `CACHE`.

**Never translate:** the keys on the left, `{0}`-style placeholders, HTML tags, or researcher names. Placeholder *order* may change to suit your grammar; the numbers must all still appear.

**Array lengths must match English** — `drill` has 5 items, `stats` 6, `legend` 4, and so on.

**Partial translations are fine.** Any key you omit falls back to English at runtime.

**RTL languages:** set `dir: "rtl"` and the layout mirrors itself — the CSS uses logical properties throughout. The mushaf page diagram stays right-to-left in every language, because it represents Arabic script rather than interface text. For Nastaliq scripts use `lineHeight` of at least `2.0`.

Pull requests welcome.

</details>
