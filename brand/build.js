// Brand asset pipeline for Quran: Adult Memorization ("Quran Hifz" on the phone).
//
// Single source of truth: mark B in brand/concepts.js - an open mushaf held
// inside a heart of gold, on green. Memorising is learning by heart, and the
// book says Qur'an at any size. It is drawn as an illustration (light, gilt,
// velvet, the Fatiha on the pages) because a flat pictogram read as nothing
// in particular.
//
// Run `node brand/build.js`, then `npx capacitor-assets generate --android`,
// then delete the full-screen splash.png files that tool adds (Android 12+
// shows ic_splash on the theme colour instead; they only cost space).
const fs = require('fs');
const path = require('path');
const sharp = require('sharp');
const K = require('./concepts.js');

const GREEN = '#17412F';     // the adaptive icon's background layer, flat
const NAVY = '#131F35';      // dark splash ground, unchanged
const PARCH = '#F3EAD7';     // light splash ground, unchanged
const GOLD = '#C9A227';
const MUTED_D = '#8FA39B';

const NAME = 'Quran: Adult Memorization';

const out = [];
const note = p => { out.push(p); return p; };
const ensure = p => { fs.mkdirSync(path.dirname(p), { recursive: true }); return p; };
const save = async (buf, file) => { fs.writeFileSync(ensure(file), buf); note(file); };

const full = size => sharp(Buffer.from(K.B(true))).resize(size, size).png().toBuffer();
const heart = size => sharp(Buffer.from(K.B(false))).resize(size, size).png().toBuffer();

const canvas = (size, bg) => sharp({
  create: { width: size, height: size, channels: 4, background: bg || { r: 0, g: 0, b: 0, alpha: 0 } },
});

// the heart alone, `share` of the canvas, centred (optionally nudged up)
async function heartOn(size, bg, share, dy) {
  const w = Math.round(size * share);
  return canvas(size, bg).composite([{ input: await heart(w), left: Math.round((size - w) / 2), top: Math.round((size - w) / 2 + (dy || 0)) }]).png().toBuffer();
}

// The opening screen: the heart, and under it the name - the one line that
// tells someone seeing the app for the first time what it is for.
async function lockup(size, bg, textFill) {
  const s = size / 1024, px = n => (n * s).toFixed(1);
  const text = Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size + '">' +
    '<rect width="' + size + '" height="' + size + '" fill="' + bg + '"/>' +
    '<text x="' + size / 2 + '" y="' + px(752) + '" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="' + px(66) + '" fill="' + textFill + '">' + NAME + '</text>' +
    '<rect x="' + px(412) + '" y="' + px(792) + '" width="' + px(200) + '" height="' + px(4) + '" fill="' + GOLD + '"/>' +
    '</svg>');
  const w = Math.round(size * 0.52);
  return sharp(text).composite([{ input: await heart(w), left: Math.round((size - w) / 2), top: Math.round(size * 0.40 - w / 2) }]).png().toBuffer();
}

(async () => {
  // launcher + PWA + Play icon: the whole picture, square (launchers and Play
  // round it themselves)
  const flat = await full(1024);
  await save(flat, 'assets/icon.png');
  // adaptive: the heart on transparency over a flat green layer. 0.86 keeps the
  // gilt rim inside the circle mask.
  await save(await heartOn(1024, null, 0.86), 'assets/icon-foreground.png');
  await save(await canvas(1024, GREEN).png().toBuffer(), 'assets/icon-background.png');

  await save(await sharp(flat).resize(192).png().toBuffer(), 'hifz-1.0.8/icon-192.png');
  await save(await sharp(flat).resize(512).png().toBuffer(), 'hifz-1.0.8/icon-512.png');
  await save(await sharp(await heartOn(1024, GREEN, 0.72)).resize(512).png().toBuffer(), 'hifz-1.0.8/icon-maskable-512.png');
  await save(await sharp(flat).resize(512).png().toBuffer(), 'play-1.0.9/graphics/icon-512.png');

  // splash sources for capacitor-assets, and the in-app opening screen
  await save(await lockup(2732, PARCH, NAVY), 'assets/splash.png');
  await save(await lockup(2732, NAVY, PARCH), 'assets/splash-dark.png');
  await save(await lockup(1024, PARCH, NAVY), 'hifz-1.0.8/splash-lockup.png');
  await save(await lockup(1024, NAVY, PARCH), 'hifz-1.0.8/splash-lockup-dark.png');

  // Android 12+ system splash icon: 288dp canvas, only the inner 192dp circle
  // is safe - the heart sits inside it on the theme's own ground
  const DENSITIES = { mdpi: 288, hdpi: 432, xhdpi: 576, xxhdpi: 864, xxxhdpi: 1152 };
  for (const d of Object.keys(DENSITIES)) {
    const size = DENSITIES[d];
    const icon = await heartOn(size, null, 176 / 288 * 0.98);
    await save(icon, 'android/app/src/main/res/drawable-' + d + '/ic_splash.png');
    await save(icon, 'android/app/src/main/res/drawable-night-' + d + '/ic_splash.png');
  }

  // Play Store feature graphic (1024x500); Play crops the edges, so everything
  // sits well inside
  const W = 1024, H = 500, hw = 250;
  const plate = await sharp({ create: { width: W, height: H, channels: 4, background: GREEN } })
    .composite([{ input: Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + W + '" height="' + H + '">' +
      '<defs><radialGradient id="g" cx="30%" cy="50%" r="80%"><stop offset="0" stop-color="#2F6B50"/><stop offset="1" stop-color="#0B261B"/></radialGradient></defs>' +
      '<rect width="' + W + '" height="' + H + '" fill="url(#g)"/>' +
      '<text x="660" y="236" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="50" fill="' + PARCH + '">Quran:</text>' +
      '<text x="660" y="300" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="50" fill="' + PARCH + '">Adult Memorization</text>' +
      '<rect x="580" y="330" width="160" height="3" fill="' + GOLD + '"/>' +
      '</svg>') }, { input: await heart(hw * 1.2), left: 90, top: Math.round((H - hw * 1.2) / 2) }])
    .png().toBuffer();
  await save(plate, 'play-1.0.9/graphics/feature-graphic-1024x500.png');

  console.log(out.length + ' files written');
})();
