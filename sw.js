/* হিফজ · দৈনিক সঙ্গী — অফলাইন ক্যাশ
   অ্যাপ আপডেট করলে নিচের সংখ্যাটা বাড়িয়ে দিন: hifz-v2, hifz-v3 … */
const CACHE = "hifz-v2";
const SHELL = [
  "./",
  "./index.html",
  "./config.js",
  "./manifest.json",
  "./icon.svg"
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

  // অ্যাপ শেল: আগে নেটওয়ার্ক (নতুন সংস্করণের জন্য), ব্যর্থ হলে ক্যাশ
  e.respondWith(
    fetch(req)
      .then((res) => {
        const copy = res.clone();
        caches.open(CACHE).then((c) => c.put(req, copy));
        return res;
      })
      .catch(() => caches.match(req).then((hit) => hit || caches.match("./index.html")))
  );
});
