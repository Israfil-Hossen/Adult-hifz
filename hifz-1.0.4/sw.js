/* হিফজ · দৈনিক সঙ্গী — অফলাইন ক্যাশ
   অ্যাপ আপডেট করলে নিচের সংখ্যাটা বাড়িয়ে দিন: hifz-v45, hifz-v45 … */
const CACHE = "hifz-1.0.4";
const SHELL = [
  "./",
  "./index.html",
  "./config.js",
  "./manifest.json",
  "./icon-192.png",
  "./icon-512.png",
  "./icon-maskable-512.png",
  "./fonts/indopak-nastaleeq.woff2"
];

self.addEventListener("install", (e) => {
  e.waitUntil(
    caches.open(CACHE)
      .then((c) => c.addAll(SHELL))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (e) => {
  e.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (e) => {
  const req = e.request;
  if (req.method !== "GET") return;

  const url = new URL(req.url);
  const sameOrigin = url.origin === self.location.origin;
  const isFont = url.hostname.startsWith("fonts.");

  // Google লগইন ও ড্রাইভ — কখনো ক্যাশ করা যাবে না, সরাসরি নেটওয়ার্কে যাক
  if (!sameOrigin && !isFont) return;

  // ফন্ট: আগে ক্যাশ, না পেলে নেটওয়ার্ক, পেলে জমিয়ে রাখো
  if (isFont) {
    e.respondWith(
      caches.match(req).then((hit) =>
        hit || fetch(req).then((res) => {
          const copy = res.clone();
          caches.open(CACHE).then((c) => c.put(req, copy));
          return res;
        }).catch(() => hit)
      )
    );
    return;
  }

  // মুসহাফের পাতা (data/): ক্যাশ আগে, না পেলে নেটওয়ার্ক। index.html কখনো
  // ফেরত দেওয়া যাবে না — JSON-এর জায়গায় HTML এলে পাতা ভেঙে যায়।
  if (url.pathname.indexOf("/data/") >= 0) {
    e.respondWith(
      caches.match(req).then((hit) =>
        hit || fetch(req).then((res) => {
          const copy = res.clone();
          caches.open(CACHE).then((c) => c.put(req, copy));
          return res;
        })
      )
    );
    return;
  }

  // অ্যাপ শেল: ক্যাশ থাকলে সাথে সাথে দেখাও, পিছনে নতুন সংস্করণ নামাও।
  // নেট ধীর/বন্ধ (চীন) হলেও অ্যাপ কখনো সাদা পর্দায় আটকে থাকবে না।
  e.respondWith(
    caches.match(req).then((hit) => {
      const net = fetch(req).then((res) => {
        const copy = res.clone();
        caches.open(CACHE).then((c) => c.put(req, copy));
        return res;
      }).catch(() => hit || caches.match("./index.html"));
      if (hit) { net.catch(() => {}); return hit; }
      // ক্যাশে নেই: ৬ সেকেন্ড পর হাল ছেড়ে যা আছে তাই দেখাও
      return Promise.race([
        net,
        new Promise((r) => setTimeout(() => r(caches.match("./index.html")), 6000))
      ]).then((r) => r || net);
    })
  );
});
