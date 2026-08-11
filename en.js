/* =========================================================
   English — the reference language file.

   To add a new language: copy this file, rename it to your
   language code (e.g. lang/tr.js for Turkish), translate the
   values on the right, and register it in index.html's LANGS
   list. Any key you leave out falls back to English, so a
   half-finished translation still works.

   Do NOT translate: the keys on the left, {0} placeholders,
   or anything inside code fences.
   ========================================================= */

window.HIFZ_LANG_en = {
  code: "en",
  name: "English",
  dir: "ltr",
  digits: "0123456789",
  locale: "en-US",
  fonts: {
    google: "Spectral:ital,wght@0,400;0,500;1,400&family=Inter:wght@300;400;500;600;700",
    display: '"Spectral", Georgia, serif',
    body: '"Inter", system-ui, -apple-system, sans-serif',
    lineHeight: "1.7"
  },

  months: ["January","February","March","April","May","June",
           "July","August","September","October","November","December"],
  monthsShort: ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],
  weekdays: ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],

  app: {
    name: "Daily Companion",
    title: "Hifz · Daily Companion",
    desc: "A research-based Qur'an memorization tracker for adults",
    streak: "day streak",
    footer1: "Hifz · Daily Companion — <b>for adult learners</b>",
    footer2: "Built on memory research · your data stays on your device"
  },

  nav: { today:"Today", routine:"Method", progress:"Progress", settings:"Settings" },

  ob: {
    kicker: "Let's begin",
    h1a: "One page a day.",
    h1b: "The app remembers the rest.",
    lede: "Three answers, and from tomorrow the app tells you every morning which page is new, which seven to review, and which juz is due.",
    lang: "Language",
    date: "Start date",
    dateHint: "Use today's date. The six-week ramp is counted from here.",
    page: "First page you'll take",
    pageHint: "Madani mushaf page number (1–604). Starting from the short surahs? Use 582 (Surah An-Naba).",
    rest: "Weekly rest day",
    restHint: "No new lesson that day — just the whole week reviewed in one sitting.",
    go: "Start"
  },

  phase: {
    week: "Week {0}",
    rest: "Rest day",
    target: "Target {0} lines",
    noNew: "No new lesson today",
    page: "Page {0}",
    restNote: "The week's six pages in one sitting, unbroken.",
    manual: "Your own chosen target.",
    notes: [
      "Just building the habit. Don't think about speed yet.",
      "This is where the review load first bites.",
      "Two blocks and a half — practice joining them.",
      "Full page. The real arithmetic starts here."
    ]
  },

  gate: {
    q: "Yesterday's lesson — page {0} — could you recite it without the mushaf, without a single error?",
    yes: "I could",
    no: "I couldn't",
    fail: "<b>No new lesson today.</b> Yesterday's page again — same drill, from the top.",
    failStreak: " That's {0} days running. Drop to half a page for two weeks — you can change the target in Settings.",
    pass: "Gate passed. Today's page is open."
  },

  blk: {
    sabaq: "New lesson",
    sabaqRepeat: "Repeat — yesterday's page",
    sabaqMeta: "Page {0} · {1} lines · 50 minutes",
    listen: "Passive listening",
    listenMeta: "All day · costs no extra time",
    sabaqi: "Sabaqi — the last seven days",
    sabaqiMeta: "After Dhuhr · 25 minutes",
    salah: "Recite in prayer",
    salahMeta: "Five prayers · costs no extra time",
    manzil: "Manzil — everything older",
    manzilMeta: "Maghrib–Isha · 35 minutes · juz {0}",
    manzilWait: "Maghrib–Isha · waiting",
    night: "Final pass",
    nightMeta: "Right before sleep · 10 minutes",
    doneLabel: "Mark complete"
  },

  sabaq: {
    pageLabel: "Today's page number",
    pageHint: "The app assumes the next page. If your order differs — starting from Surah An-Naba, say — change it here.",
    blocked: "<b>Nothing new today.</b> Same page, same drill, right from the start.",
    intro: "Five steps per block. Tap a block above once you've finished it.",
    after: "<b>After {0} blocks:</b> the whole passage 3 times with the mushaf closed, unbroken.",
    drill: [
      "<b>Listen 5 times</b> — eyes on those five lines, lips still",
      "<b>Read aloud 7 times</b> — looking at the mushaf, unhurried",
      "<b>Meaning once</b> — word by word, what each word means",
      "<b>Cover and recite 3 times</b> — a count only starts when it's flawless",
      "<b>Join twice</b> — pulling in from the last line of the previous block"
    ]
  },

  listen: {
    body: "Let today's page play in the same reciter's voice — in the car, the kitchen, on a walk. You don't need to concentrate; the ear does the work.",
    qari: " Your reciter: <b>{0}</b>."
  },

  sabaqi: {
    empty: "No review queue yet — it fills from tomorrow, once you've done your first lesson.",
    weakLabel: "<b>Weak pages</b> — give these one extra pass:",
    order: "<b>The order is the whole point:</b> close the mushaf and recite first, then open it and check. Do it the other way round and you lose everything this block is for.",
    ago: "{0} days ago",
    page: "p {0}"
  },

  salah: {
    body: "Recite from today's lesson and this week's pages. Standing, no mushaf, pulling it out of memory under pressure — this is retrieval practice in its purest form.",
    counted: "prayers recited"
  },

  manzil: {
    week: "<b>This week's juz: {0}</b> — pages {1}–{2}. Only the parts you've memorized.",
    rot: "The rotation is automatic. The more juz you accumulate, the further apart each one returns — which is exactly the interval at which Ebbinghaus's curve breaks.",
    empty: "Manzil hasn't started yet. It switches on by itself once you have about 8 pages."
  },

  night: {
    body: "Today's page once more, sitting on the bed. What goes in right before sleep consolidates best overnight. No screens after this."
  },

  timer: { start: "{0}-minute timer", stop: "Stop" },

  log: {
    err: "How many errors today",
    errHint: "Whether this number is falling is the real measure of progress — not the page count.",
    min: "Active time (minutes)",
    note: "Weak spots / notes",
    notePh: "Which ayah keeps catching you? Where did two similar verses blur together?",
    weak: "Flag as weak (page number)",
    weakPh: "e.g. 45",
    weakAdd: "Add",
    weakClear: "Clear list",
    weakHint: "Flagged pages return to your review queue every day until you remove them."
  },

  sum: {
    done: "Completed today",
    total: "Total memorized",
    stat: "{0} pages · {1} minutes today"
  },

  rt: {
    ramp: {
      k: "Starting out", h: "The six-week ramp",
      p: "Memorizing new material is easy; holding on to it is not. These six weeks build the habit — not the speed — before the review load gets heavy.",
      lines: "lines",
      labels: ["Weeks 1–2","Weeks 3–4","Weeks 5–6","Week 7 →"]
    },
    day: {
      k: "Every day", h: "Six touches through the day",
      p: "Not three hours in one sitting — short sessions spread across the day. Cepeda's work on distributed practice shows the same total time retains far more when it's broken up.",
      th: ["When","What","Time"],
      rows: [
        ["Fajr + 15m","New lesson — three blocks, then the whole page 3 times","50m"],
        ["All day","Passive listening — the same reciter","0"],
        ["After Dhuhr","Sabaqi — the last 7 days, mushaf closed first","25m"],
        ["Five prayers","Recite from the lesson in salah","0"],
        ["Maghrib–Isha","Manzil — one juz a week, rotating","35m"],
        ["Before sleep","Today's page one last time","10m"]
      ]
    },
    stuck: {
      k: "When you're stuck", h: "The stubborn-line protocol",
      p: "Reading a line ten more times doesn't work when it won't stick — you have to change method. Work down the list and stop at whichever one opens it.",
      steps: [
        "<b>Split it</b> — break the line into two or three pieces, set each separately, then join",
        "<b>Go to the meaning</b> — getting stuck is almost always a sign you don't know what it says; read the translation of that stretch",
        "<b>Write it by hand</b> — writing it once brings in a different sensory channel",
        "<b>Pull from the line before</b> — not alone, but together with the previous line, 5 times",
        "<b>Go to sleep</b> — if you've been stuck 20 minutes, stop. It often opens on its own the next morning"
      ]
    },
    mut: {
      k: "The biggest trap", h: "Mutashabihat — near-identical verses",
      p: "The number one cause of hifz breaking down isn't new pages — it's two verses with near-identical wording blurring together. Memory research calls this interference, and the only cure is to learn the difference <b>deliberately</b>.",
      steps: [
        "<b>Write them side by side</b> — both verses on one sheet, one above the other",
        "<b>Mark the exact word</b> — where they diverge",
        "<b>Anchor the address</b> — \"this one is low on the right page, that one high on the left\"; spatial memory is what saves you here",
        "<b>Log it in your notes</b> — in today's note field; review the whole list once a week"
      ],
      nh: "A small rule",
      np: "A pair that blurred once will blur again. Write it down the very first time — six months from now this list will be the most valuable page you own."
    },
    real: {
      k: "Real life", h: "Travel, illness, a brutal week",
      p: "The routine will break. The only question is what you do on the day it breaks — and if the answer is \"nothing,\" hifz stops there.",
      th: ["Situation","What to do"],
      rows: [
        ["No time","Skip the new lesson. Sabaqi only — 25 minutes. The streak survives."],
        ["Travelling","Skip the new lesson. Listen, and recite in salah. The day still counts."],
        ["Ill","Complete rest. Push through and you'll bake in wrong pronunciation, which is hard to undo."],
        ["A week missed","Don't resume where you stopped — go back <b>two weeks</b> and spend two days on review only."],
        ["A month missed","No new lesson for two weeks. Cycle once through everything you have, then restart."]
      ]
    },
    month: {
      k: "Monthly", h: "One day a month — the test",
      p: "On the last rest day of the month: recite everything you memorized that month in one unbroken run — to someone, or into a recorder. Flag every page you stumbled on as <b>weak</b>. The app will feed them back into your daily review.",
      gh: "The gate",
      g1: "If you can't recite yesterday's page without the mushaf and without error — nothing new today.",
      g2: "Two days stuck in a row: drop to half a page and try again in two weeks. Skip this rule and four months from now you'll have 100 pages you \"more or less remember\" — which counts as zero."
    },
    basis: {
      k: "Foundations", h: "Where each step comes from",
      th: ["Research","What it shows","Where it lands"],
      rows: [
        ["Craik &amp; Lockhart 1972<br>Craik &amp; Tulving 1975","Material understood for meaning is encoded far deeper than sound alone","Drill step 3 — word-by-word translation"],
        ["Karpicke &amp; Roediger 2008<br>Roediger &amp; Butler 2011","Pulling from memory beats re-reading by a wide margin","Covered recitation · closed-mushaf-first sabaqi · reciting in salah"],
        ["Ebbinghaus 1885<br>Cepeda 2006","Without review, much is lost within 24 hours; at widening intervals it becomes near-permanent","7-day sabaqi · manzil rotation · six touches a day"],
        ["Miller 1956<br>Gobet 2001","Working memory holds only a few units at once","Splitting the page into 5-line blocks"],
        ["Maguire 2000, 2003<br>Dresler 2017","The brain is extraordinary at remembering place and position","The same mushaf for life · anchoring mutashabihat by location"],
        ["Paivio 1986<br>Shams &amp; Seitz 2008","Memory holds better when it enters through several senses","Listen + see + speak + write when stuck"],
        ["Mayer &amp; Moreno 2003<br>Baddeley 2000","Interruption eats working-memory capacity","Phone in another room · 50-minute ceiling"],
        ["Draganski 2004<br>Park &amp; Bischof 2013","Training produces structural change in adult brains too","32 isn't late — method is your advantage"]
      ]
    },
    fixed: {
      k: "Non-negotiable", h: "Three things with no give in them",
      items: [
        ["Tajweed first","Memorize with wrong pronunciation and fixing it is harder than memorizing from scratch. Recite to a teacher at least once a week."],
        ["Seven hours of sleep","Consolidation happens largely during sleep. Cutting sleep to gain memorization time is a net loss — you shed more than you add."],
        ["One mushaf, one reciter","Change the mushaf and the whole spatial layer is wiped; change the reciter and the melodic cue goes. Choose once, then don't touch it."]
      ]
    }
  },

  pg: {
    k: "Progress", h: "Where you stand",
    report: "Weekly report — for your teacher",
    stats: ["pages memorized","full juz","complete","day streak","total hours","avg errors / day"],
    map: "Mushaf map · 604 pages",
    legend: ["Memorized","Today's","Weak","Remaining"],
    heat: "Last 84 days",
    heatNote: "Darker means more completed",
    juz: "By juz",
    rate: "At your current pace",
    rateDaily: "Daily average",
    rateLeft: "Remaining",
    rateFinish: "Projected finish",
    rateNone: "Let a few more days accumulate",
    ratePages: "{0} pages",
    weakH: "Weak pages",
    weakP: "These come back into your review queue every day. Once you've been flawless for seven days running, clear them in Settings.",
    notes: "Recent notes"
  },

  set: {
    k: "Settings", h: "Your setup",
    p: "Everything is stored on this device — nothing goes to a server. Clearing your browser wipes it, so either turn on Drive sync below or download a backup once a month.",
    lang: "Language",
    langHint: "The whole app switches — including the method tab.",
    date: "Start date",
    page: "First page",
    rest: "Rest day",
    lines: "Daily target (lines)",
    linesAuto: "Automatic — follow the ramp",
    linesN: "{0} lines",
    linesHint: "Stuck at the gate two days running? Come down here. That isn't going backwards.",
    qari: "Your reciter",
    qariPh: "one, for life",
    mushaf: "Your mushaf",
    weakLabel: "Weak pages",
    weakHint: "Tap to remove once you've been flawless for seven days.",
    driveLabel: "Google Drive sync",
    driveHint: "While linked, every change reaches your Drive within seconds. Sign in on a new phone with the same Google account and everything returns. The app can see only the one file it created — nothing else in your Drive.",
    driveNoConfig: "Add your Client ID in config.js to switch this on.",
    backupLabel: "File backup",
    exp: "Download data (JSON)",
    imp: "Restore from backup",
    backupHint: "Download once a month and keep it in email or Drive. Restoring that file on a new phone brings everything back.",
    danger: "Danger zone",
    reset: "Erase all data",
    confirmReset: "All data will be erased and cannot be recovered. Have you downloaded a backup?",
    restored: "Backup restored.",
    badFile: "Couldn't read that file. Pick a valid backup."
  },

  sync: {
    off: "Sync off", connecting: "Connecting", ok: "Synced", failed: "Sync failed",
    pushing: "Sending", pulling: "Fetching", reconciling: "Reconciling", cancelled: "Cancelled",
    noConfig: "No Google Client ID set — add one in <b>config.js</b> to switch sync on.",
    notLinked: "Not linked yet. Once linked, your data saves to your Drive automatically.",
    linked: "Linked: <b>{0}</b>",
    last: " · last sync {0}",
    never: " · not synced yet",
    connect: "Connect with Google",
    push: "Send now",
    pull: "Fetch from Drive",
    unlink: "Unlink",
    confirmOff: "Once unlinked, new changes stop reaching your Drive. The existing file stays there.",
    notReady: "The Google library hasn't loaded yet — try again in a few seconds.",
    pushFail: "Couldn't reach Drive. Check your connection and try again.",
    pullFail: "No backup found in Drive.",
    pulled: "Data restored from Drive."
  },

  rep: {
    title: "Weekly Hifz Report",
    stats: ["new pages","total memorized","total errors","hours"],
    daily: "Day by day",
    th: ["Date","Lesson","Gate","Errors","Minutes","Done"],
    pass: "Pass", fail: "Fail", rest: "Rest",
    gateH: "Gate results",
    gateLine: "Pass <span class=\"ok\">{0}</span> days · Fail <span class=\"no\">{1}</span> days · Lesson completed {2} days. ",
    verdictGood: "Pace is right — worth considering an increase.",
    verdictBad: "The target is too high. Cut the daily line count and hold there two weeks.",
    verdictOk: "Pace is about right. Stay at this target another week.",
    weakH: "Weak pages — listen to these especially",
    notesH: "Student's notes",
    comment: "Teacher's comment",
    signature: "Signature & date",
    print: "Print / PDF",
    close: "Close",
    page: "Page {0}"
  }
};
