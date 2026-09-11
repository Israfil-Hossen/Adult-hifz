// Brand asset pipeline for Quran: Adult Memorization ("Quran Hifz" on the phone).
//
// Single source of truth: the mark below - an open mushaf whose two pages
// meet as a heart. Memorising is learning by heart, and the book is plainly
// the Qur'an at any size, which the word حفظ in a calligraphic face was not:
// handsome, but at launcher size most people could not read it.
//
// Run `node brand/build.js`, then `npx capacitor-assets generate --android`
// to regenerate every asset from it.
const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

const GREEN = '#244F3F';     // the tile
const NAVY = '#131F35';      // dark splash ground, unchanged
const PARCH = '#F3EAD7';     // light splash ground, unchanged
const GOLD = '#D4AF37';
const MUTED_D = '#8FA39B';
const MUTED_L = '#6B7A72';

const NAME = 'Quran: Adult Memorization';
const TAG = 'daily companion';

const out = [];
const note = p => { out.push(p); return p; };
const ensure = p => { fs.mkdirSync(path.dirname(p), { recursive: true }); return p; };

// The mark, in a 108-unit box. Its ink runs x 21-87, y 24-89.
const MARK =
  '<path d="M54 36 C46 24 24 24 22 44 C21 60 38 72 54 88 Z" fill="#F4E9CF"/>' +
  '<path d="M54 36 C62 24 84 24 86 44 C87 60 70 72 54 88 Z" fill="#E6D3A6"/>' +
  '<path d="M54 36 L54 88" stroke="#C9A227" stroke-width="2"/>' +
  '<path d="M31 45 C37 42 44 43 49 47 M30 53 C37 50 44 51 49 55 M34 61 C39 59 44 60 49 63 ' +
        'M59 47 C64 43 71 42 77 45 M59 55 C64 51 71 50 78 53 M59 63 C64 60 69 59 74 61" ' +
        'stroke="#9C7A22" stroke-width="1.5" fill="none" stroke-linecap="round"/>' +
  '<path d="M22 44 C21 60 38 72 54 88 C70 72 87 60 86 44" stroke="' + GOLD + '" stroke-width="3" fill="none"/>';

// the mark alone, `w` pixels wide, on transparency (viewBox crops to the ink
// with a little room for the gold rim)
function markSvg(w) {
  const vb = [19, 22, 70, 70];
  return Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + w + '" height="' + w +
    '" viewBox="' + vb.join(' ') + '">' + MARK + '</svg>');
}

// the tile: the mark on green, as the launcher shows it. `r` is the corner
// radius as a share of the side (0 for the square Play wants).
function tileSvg(w, r) {
  return Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + w + '" height="' + w +
    '" viewBox="0 0 108 108"><rect width="108" height="108" rx="' + (108 * r) + '" fill="' + GREEN + '"/>' +
    '<g transform="translate(54 55) scale(1.02) translate(-54 -56.5)">' + MARK + '</g></svg>');
}

const png = svg => sharp(svg).png().toBuffer();

const canvas = (size, bg) => sharp({
  create: { width: size, height: size, channels: 4, background: bg || { r: 0, g: 0, b: 0, alpha: 0 } },
});

// the mark centred on a canvas, `share` of its width
async function markOn(size, bg, share) {
  const w = Math.round(size * share);
  const m = await png(markSvg(w));
  return canvas(size, bg).composite([{ input: m, left: Math.round((size - w) / 2), top: Math.round((size - w) / 2) }]).png().toBuffer();
}

// the Android 12 splash icon: a green disc (the system masks this area to a
// circle anyway) with the mark on it
async function splashIcon(size) {
  const d = size * (176 / 288);
  const svg = Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size + '">' +
    '<circle cx="' + size / 2 + '" cy="' + size / 2 + '" r="' + d / 2 + '" fill="' + GREEN + '"/></svg>');
  const mw = Math.round(d * 0.66);
  const m = await png(markSvg(mw));
  return sharp(svg).composite([{ input: m, left: Math.round((size - mw) / 2), top: Math.round((size - mw) / 2) }]).png().toBuffer();
}

const svgLockup = (size, bg, textFill, ruleFill, tagFill) => {
  const s = size / 1024, px = n => (n * s).toFixed(1);
  return Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size + '">' +
    '<rect width="' + size + '" height="' + size + '" fill="' + bg + '"/>' +
    '<text x="' + size / 2 + '" y="' + px(700) + '" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="' + px(64) + '" fill="' + textFill + '">' + NAME + '</text>' +
    '<rect x="' + px(402) + '" y="' + px(738) + '" width="' + px(220) + '" height="' + px(4) + '" fill="' + ruleFill + '"/>' +
    '<text x="' + size / 2 + '" y="' + px(806) + '" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="' + px(42) + '" fill="' + tagFill + '">' + TAG + '</text>' +
    '</svg>');
};

async function lockup(size, bg, textFill, tagFill) {
  const t = Math.round(size * 0.36);
  const tile = await png(tileSvg(t, 0.22));
  return sharp(svgLockup(size, bg, textFill, GOLD, tagFill))
    .composite([{ input: tile, left: Math.round((size - t) / 2), top: Math.round(size * 0.37 - t / 2) }])
    .png().toBuffer();
}

const save = async (buf, file) => { fs.writeFileSync(ensure(file), buf); note(file); };

(async () => {
  // launcher + PWA icons. The flat icon is the tile, square: Play and the
  // launchers round it themselves. The adaptive foreground is the mark alone,
  // small enough to stay inside the circle mask; the green is the background.
  const flat = await png(tileSvg(1024, 0));
  await save(flat, 'assets/icon.png');
  await save(await markOn(1024, null, 0.6), 'assets/icon-foreground.png');
  await save(await canvas(1024, GREEN).png().toBuffer(), 'assets/icon-background.png');

  await save(await sharp(flat).resize(192).png().toBuffer(), 'hifz-1.0.8/icon-192.png');
  await save(await sharp(flat).resize(512).png().toBuffer(), 'hifz-1.0.8/icon-512.png');
  await save(await sharp(await markOn(1024, GREEN, 0.5)).resize(512).png().toBuffer(), 'hifz-1.0.8/icon-maskable-512.png');
  await save(await sharp(flat).resize(512).png().toBuffer(), 'play-1.0.8/graphics/icon-512.png');

  // splash sources for capacitor-assets, and the in-app launch lockups
  await save(await lockup(2732, PARCH, NAVY, MUTED_L), 'assets/splash.png');
  await save(await lockup(2732, NAVY, PARCH, MUTED_D), 'assets/splash-dark.png');
  await save(await lockup(1024, PARCH, NAVY, MUTED_L), 'hifz-1.0.8/splash-lockup.png');
  await save(await lockup(1024, NAVY, PARCH, MUTED_D), 'hifz-1.0.8/splash-lockup-dark.png');

  // Android 12+ system splash icon: 288dp canvas, only the inner 192dp is safe
  const DENSITIES = { mdpi: 288, hdpi: 432, xhdpi: 576, xxhdpi: 864, xxxhdpi: 1152 };
  for (const d of Object.keys(DENSITIES)) {
    const size = DENSITIES[d];
    const icon = await splashIcon(size);
    await save(icon, 'android/app/src/main/res/drawable-' + d + '/ic_splash.png');
    await save(icon, 'android/app/src/main/res/drawable-night-' + d + '/ic_splash.png');
  }

  // --- Play Store feature graphic (1024x500). Play crops the edges on some
  // surfaces, so everything sits well inside.
  const W = 1024, H = 500, mw = 190;
  const text = Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + W + '" height="' + H + '">' +
    '<rect width="' + W + '" height="' + H + '" fill="' + GREEN + '"/>' +
    '<text x="' + W / 2 + '" y="345" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="56" fill="' + PARCH + '">' + NAME + '</text>' +
    '<rect x="' + (W / 2 - 90) + '" y="370" width="180" height="3" fill="' + GOLD + '"/>' +
    '<text x="' + W / 2 + '" y="420" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="28" fill="' + MUTED_D + '">' + TAG + '</text>' +
    '</svg>');
  const m = await png(markSvg(mw));
  const fg = await sharp(text).composite([{ input: m, left: Math.round((W - mw) / 2), top: 70 }]).png().toBuffer();
  await save(fg, 'play-1.0.8/graphics/feature-graphic-1024x500.png');

  console.log(out.length + ' files written');
})();
