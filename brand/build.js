// Brand asset pipeline for Adult Hifz.
// Single source of truth: the word حفظ set in Alkalami (SIL OFL, see fonts/Alkalami-OFL.txt).
// Run `node brand/build.js` after any change here to regenerate every asset.
const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

const NAVY = '#131F35';
const GOLD = '#C9A227';
const PARCH = '#F3EAD7';
const MUTED_D = '#8FA39B';
const MUTED_L = '#6B7A72';

const FONT = 'brand/fonts/Alkalami-Regular.ttf';
const HIFZ = 'حفظ';

const out = [];
const note = p => { out.push(p); return p; };
const ensure = p => { fs.mkdirSync(path.dirname(p), { recursive: true }); return p; };

// The word, rendered by pango (so Arabic shaping is correct), trimmed to its
// ink bounds and scaled to `width`. Everything else is placed relative to this.
async function word(colour, width) {
  const raw = await sharp({
    text: { text: '<span foreground="' + colour + '">' + HIFZ + '</span>', font: 'Alkalami', fontfile: FONT, rgba: true, width: 2400, height: 1400 },
  }).png().toBuffer();
  const tight = await sharp(raw).trim().png().toBuffer();
  const buf = await sharp(tight).resize({ width: Math.round(width) }).png().toBuffer();
  const meta = await sharp(buf).metadata();
  return { buf, w: meta.width, h: meta.height };
}

const canvas = (size, bg) => sharp({
  create: { width: size, height: size, channels: 4, background: bg || { r: 0, g: 0, b: 0, alpha: 0 } },
});

// word centred horizontally, its optical middle sitting at `midY`
async function markOn(size, bg, colour, wordWidth, midY) {
  const { buf, w, h } = await word(colour, wordWidth);
  return canvas(size, bg)
    .composite([{ input: buf, left: Math.round((size - w) / 2), top: Math.round(midY - h / 2) }])
    .png().toBuffer();
}

const svgLockup = (size, bg, textFill, ruleFill, tagFill) => {
  const s = size / 1024, px = n => (n * s).toFixed(1);
  return Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size + '">' +
    '<rect width="' + size + '" height="' + size + '" fill="' + bg + '"/>' +
    '<text x="' + size / 2 + '" y="' + px(700) + '" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="' + px(120) + '" fill="' + textFill + '">Adult Hifz</text>' +
    '<rect x="' + px(382) + '" y="' + px(736) + '" width="' + px(260) + '" height="' + px(4) + '" fill="' + ruleFill + '"/>' +
    '<text x="' + size / 2 + '" y="' + px(812) + '" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="' + px(46) + '" fill="' + tagFill + '">daily companion</text>' +
    '</svg>');
};

async function lockup(size, bg, markColour, textFill, tagFill) {
  const { buf, w, h } = await word(markColour, size * 0.42);
  return sharp(svgLockup(size, bg, textFill, GOLD, tagFill))
    .composite([{ input: buf, left: Math.round((size - w) / 2), top: Math.round(size * 0.37 - h / 2) }])
    .png().toBuffer();
}

const save = async (buf, file) => { fs.writeFileSync(ensure(file), buf); note(file); };

(async () => {
  // launcher + PWA icons. 0.68 of canvas for unmasked art; 0.58 for the
  // adaptive foreground, which keeps the word inside the 66% safe circle.
  const flat = await markOn(1024, NAVY, GOLD, 1024 * 0.68, 512);
  await save(flat, 'assets/icon.png');
  await save(await markOn(1024, null, GOLD, 1024 * 0.58, 512), 'assets/icon-foreground.png');
  await save(await sharp({ create: { width: 1024, height: 1024, channels: 4, background: NAVY } }).png().toBuffer(), 'assets/icon-background.png');

  await save(await sharp(flat).resize(192).png().toBuffer(), 'hifz-1.0.4/icon-192.png');
  await save(await sharp(flat).resize(512).png().toBuffer(), 'hifz-1.0.4/icon-512.png');
  await save(await sharp(await markOn(1024, NAVY, GOLD, 1024 * 0.58, 512)).resize(512).png().toBuffer(), 'hifz-1.0.4/icon-maskable-512.png');

  // splash sources for capacitor-assets, and the in-app launch lockups
  await save(await lockup(2732, PARCH, NAVY, NAVY, MUTED_L), 'assets/splash.png');
  await save(await lockup(2732, NAVY, GOLD, PARCH, MUTED_D), 'assets/splash-dark.png');
  await save(await lockup(1024, PARCH, NAVY, NAVY, MUTED_L), 'hifz-1.0.4/splash-lockup.png');
  await save(await lockup(1024, NAVY, GOLD, PARCH, MUTED_D), 'hifz-1.0.4/splash-lockup-dark.png');

  // Android 12+ system splash icon: 288dp canvas, only the inner 192dp is
  // safe, so the word is capped at 176/288 of whatever density we emit.
  const DENSITIES = { mdpi: 288, hdpi: 432, xhdpi: 576, xxhdpi: 864, xxxhdpi: 1152 };
  for (const d of Object.keys(DENSITIES)) {
    const size = DENSITIES[d];
    await save(await markOn(size, null, NAVY, size * (176 / 288), size / 2), 'android/app/src/main/res/drawable-' + d + '/ic_splash.png');
    await save(await markOn(size, null, GOLD, size * (176 / 288), size / 2), 'android/app/src/main/res/drawable-night-' + d + '/ic_splash.png');
  }

  // the vector version is superseded by the density PNGs above
  fs.rmSync('android/app/src/main/res/drawable/ic_splash.xml', { force: true });

  console.log(out.length + ' files written');
})();

// --- Play Store feature graphic (1024x500) -------------------------------
// Play crops the edges on some surfaces, so everything sits well inside.
(async () => {
  const W = 1024, H = 500;
  const { buf, w, h } = await word(GOLD, 300);
  const text = Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + W + '" height="' + H + '">' +
    '<rect width="' + W + '" height="' + H + '" fill="' + NAVY + '"/>' +
    '<text x="' + W / 2 + '" y="345" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="74" fill="' + PARCH + '">Adult Hifz</text>' +
    '<rect x="' + (W / 2 - 100) + '" y="372" width="200" height="3" fill="' + GOLD + '"/>' +
    '<text x="' + W / 2 + '" y="422" text-anchor="middle" font-family="Georgia, \'Times New Roman\', serif" font-size="29" fill="' + MUTED_D + '">daily companion</text>' +
    '</svg>');
  const out = await sharp(text)
    .composite([{ input: buf, left: Math.round((W - w) / 2), top: Math.round(175 - h / 2) }])
    .png().toBuffer();
  fs.mkdirSync('store', { recursive: true });
  fs.writeFileSync('store/feature-graphic.png', out);
  console.log('store/feature-graphic.png (1024x500)');
})();
