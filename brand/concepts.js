// Four different marks, each drawn as an illustration, for choosing between.
// A reuses the rehal; the other three are different ideas altogether.
const R = require('./rehal.js');

const wrap = body => '<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">' + body + '</svg>';

// A - the open mushaf on a carved rehal
const A = () => wrap(R.rehalSvg('green'));

// B - the open mushaf held inside a heart of gold: learning it by heart
const HEART = 'M256 452 C118 360 34 276 52 170 C68 88 176 58 256 146 C336 58 444 88 460 170 C478 276 394 360 256 452 Z';
const B = () => wrap(
  R.defs('green') +
  '<defs>' +
    '<radialGradient id="bgB" cx="50%" cy="42%" r="72%"><stop offset="0" stop-color="#2F6B50"/><stop offset="0.6" stop-color="#17412F"/><stop offset="1" stop-color="#0B261B"/></radialGradient>' +
    '<radialGradient id="velvet" cx="50%" cy="38%" r="65%"><stop offset="0" stop-color="#A42A36"/><stop offset="0.7" stop-color="#6B1420"/><stop offset="1" stop-color="#3E0A12"/></radialGradient>' +
    '<linearGradient id="goldB" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#FBE7A1"/><stop offset="0.3" stop-color="#D9B042"/><stop offset="0.55" stop-color="#F6DA7C"/><stop offset="0.8" stop-color="#A67C1A"/><stop offset="1" stop-color="#E7C565"/></linearGradient>' +
    '<clipPath id="heartClip"><path d="' + HEART + '"/></clipPath>' +
  '</defs>' +
  '<rect width="512" height="512" fill="url(#bgB)"/>' +
  '<path d="' + HEART + '" fill="#000" opacity="0.45" filter="url(#soft)" transform="translate(0 10)"/>' +
  '<path d="' + HEART + '" fill="url(#velvet)"/>' +
  '<ellipse cx="256" cy="230" rx="170" ry="120" fill="url(#glow)" clip-path="url(#heartClip)"/>' +
  '<g transform="translate(256 258) scale(0.7) translate(-256 -236)">' + R.book() + '</g>' +
  '<path d="' + HEART + '" fill="none" stroke="url(#goldB)" stroke-width="24" stroke-linejoin="round"/>' +
  '<path d="' + HEART + '" fill="none" stroke="#FFF3C4" stroke-width="2" opacity="0.7" transform="translate(256 256) scale(0.955) translate(-256 -256)"/>' +
  '<path d="' + HEART + '" fill="none" stroke="#6B4A0C" stroke-width="2" opacity="0.6" transform="translate(256 256) scale(1.045) translate(-256 -256)"/>'
);

// C - the closed mushaf: green leather, a gilded border, the name in gold
function eightStar(cx, cy, r) {
  const sq = (rot) => '<rect x="' + (cx - r) + '" y="' + (cy - r) + '" width="' + 2 * r + '" height="' + 2 * r + '" transform="rotate(' + rot + ' ' + cx + ' ' + cy + ')"/>';
  return sq(0) + sq(45);
}
const C = () => wrap(
  R.defs('green') +
  '<defs>' +
    '<radialGradient id="bgC" cx="50%" cy="40%" r="75%"><stop offset="0" stop-color="#F6EAD0"/><stop offset="0.6" stop-color="#E2CDA3"/><stop offset="1" stop-color="#B99A68"/></radialGradient>' +
    '<linearGradient id="leather" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#2A7353"/><stop offset="0.5" stop-color="#1B5139"/><stop offset="1" stop-color="#0F3524"/></linearGradient>' +
    '<linearGradient id="spine" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#0D2E20"/><stop offset="0.6" stop-color="#1D5A40"/><stop offset="1" stop-color="#0F3524"/></linearGradient>' +
    '<linearGradient id="edge" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#F3D77A"/><stop offset="0.5" stop-color="#B98F24"/><stop offset="1" stop-color="#E8C763"/></linearGradient>' +
  '</defs>' +
  '<rect width="512" height="512" fill="url(#bgC)"/>' +
  '<rect x="138" y="94" width="256" height="352" rx="14" fill="#000" opacity="0.35" filter="url(#soft)" transform="translate(10 14)"/>' +
  // gilded page edges showing on the fore-edge side and the foot
  '<rect x="372" y="104" width="22" height="330" rx="4" fill="url(#edge)"/>' +
  '<path d="M377 110 V428 M382 110 V428 M387 110 V428" stroke="#9A7418" stroke-width="1" opacity="0.6"/>' +
  // the cover and its spine
  '<rect x="124" y="92" width="258" height="348" rx="12" fill="url(#leather)"/>' +
  '<rect x="124" y="92" width="26" height="348" rx="10" fill="url(#spine)"/>' +
  '<path d="M152 96 V436" stroke="#0A2618" stroke-width="2"/>' +
  // gold tooling: a double border and corner pieces
  '<rect x="170" y="112" width="192" height="308" rx="6" fill="none" stroke="url(#gold)" stroke-width="5"/>' +
  '<rect x="180" y="122" width="172" height="288" rx="4" fill="none" stroke="url(#gold)" stroke-width="1.6"/>' +
  [[180, 122, 0], [352, 122, 90], [352, 410, 180], [180, 410, 270]].map(([x, y, r]) =>
    '<path d="M0 0 H34 C22 6 10 18 0 34 Z" fill="url(#gold)" transform="translate(' + x + ' ' + y + ') rotate(' + r + ')"/>').join('') +
  // the medallion, and the name
  '<g fill="url(#gold)">' + eightStar(266, 266, 62) + '</g>' +
  '<g fill="#15462F">' + eightStar(266, 266, 54) + '</g>' +
  '<circle cx="266" cy="266" r="46" fill="none" stroke="url(#gold)" stroke-width="2"/>' +
  '<text x="266" y="258" text-anchor="middle" font-family="Traditional Arabic, Arial, sans-serif" font-size="34" font-weight="bold" fill="#F1D27A">القرآن</text>' +
  '<text x="266" y="292" text-anchor="middle" font-family="Traditional Arabic, Arial, sans-serif" font-size="26" fill="#F1D27A">الكريم</text>' +
  '<rect x="236" y="150" width="60" height="6" rx="3" fill="url(#gold)"/><rect x="236" y="376" width="60" height="6" rx="3" fill="url(#gold)"/>' +
  // light on the leather
  '<path d="M150 92 L260 92 L150 260 Z" fill="#FFFFFF" opacity="0.06"/>'
);

// D - the open mushaf giving off light, under a night sky with the crescent
function ray(angle, len, w) {
  const a = angle * Math.PI / 180, cx = 256, cy = 300;
  const x = cx + Math.cos(a) * len, y = cy - Math.sin(a) * len;
  const px = Math.sin(a) * w, py = Math.cos(a) * w;
  return '<polygon points="' + cx + ',' + cy + ' ' + (x - px).toFixed(1) + ',' + (y - py).toFixed(1) + ' ' + (x + px).toFixed(1) + ',' + (y + py).toFixed(1) + '" fill="url(#rayG)"/>';
}
const D = () => wrap(
  R.defs('navy') +
  '<defs>' +
    '<radialGradient id="sky" cx="50%" cy="70%" r="80%"><stop offset="0" stop-color="#2C4476"/><stop offset="0.55" stop-color="#15254A"/><stop offset="1" stop-color="#080F24"/></radialGradient>' +
    '<radialGradient id="rayG" cx="50%" cy="100%" r="100%" gradientUnits="objectBoundingBox"><stop offset="0" stop-color="#FFE7A8" stop-opacity="0.55"/><stop offset="1" stop-color="#FFE7A8" stop-opacity="0"/></radialGradient>' +
    '<radialGradient id="halo" cx="50%" cy="50%" r="50%"><stop offset="0" stop-color="#FFE9B0" stop-opacity="0.75"/><stop offset="1" stop-color="#FFE9B0" stop-opacity="0"/></radialGradient>' +
  '</defs>' +
  '<rect width="512" height="512" fill="url(#sky)"/>' +
  [[70, 70, 2.2], [120, 130, 1.6], [200, 60, 1.8], [330, 50, 1.5], [450, 150, 2], [60, 190, 1.4], [420, 250, 1.6], [150, 40, 1.2]]
    .map(([x, y, r]) => '<circle cx="' + x + '" cy="' + y + '" r="' + r + '" fill="#FFF6D8"/>').join('') +
  // the crescent
  '<mask id="cres"><rect width="512" height="512" fill="#fff"/><circle cx="410" cy="84" r="38" fill="#000"/></mask>' +
  '<circle cx="392" cy="96" r="42" fill="#F7E3A0" mask="url(#cres)"/>' +
  // light rising from the page
  [105, 90, 75, 120, 60, 135, 150, 30].map((a, i) => ray(a, 250 - (i % 3) * 30, 20 + (i % 2) * 10)).join('') +
  '<ellipse cx="256" cy="250" rx="200" ry="130" fill="url(#halo)"/>' +
  // a low mosque skyline, far off
  '<g fill="#070D1F" opacity="0.92">' +
    '<path d="M0 476 H512 V512 H0 Z"/>' +
    '<path d="M88 476 C88 440 110 420 130 414 L132 398 L134 414 C154 420 176 440 176 476 Z"/>' +
    '<path d="M336 476 C336 440 358 420 378 414 L380 398 L382 414 C402 420 424 440 424 476 Z"/>' +
    '<path d="M52 476 V396 L58 384 L64 396 V476 Z"/><path d="M448 476 V396 L454 384 L460 396 V476 Z"/>' +
  '</g>' +
  '<g transform="translate(256 292) scale(0.8) translate(-256 -236)">' + R.book() + '</g>'
);

module.exports = { A, B, C, D };
