// The mark: an open mushaf resting on a carved wooden rehal, drawn as an
// illustration rather than a pictogram - wood with a grain and an edge, pages
// with thickness and a gold-framed text block, light falling on the book.
// Everything is in a 512-unit square; brand/build.js renders it at any size.

const W = 512;

// the Fatiha, for the two pages (it is the opening of every printed mushaf)
const LEFT = ['بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ', 'الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ', 'الرَّحْمَٰنِ الرَّحِيمِ', 'مَالِكِ يَوْمِ الدِّينِ'];
const RIGHT = ['إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ', 'اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ', 'صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ', 'غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ'];

function board(x0, y0, x1, y1, hw) {
  const dx = x1 - x0, dy = y1 - y0, L = Math.hypot(dx, dy);
  const nx = -dy / L * hw, ny = dx / L * hw;
  const p = [[x0 + nx, y0 + ny], [x1 + nx, y1 + ny], [x1 - nx, y1 - ny], [x0 - nx, y0 - ny]];
  return p.map(q => q[0].toFixed(1) + ',' + q[1].toFixed(1)).join(' ');
}

function star(cx, cy, r) {
  const pts = [];
  for (let i = 0; i < 16; i++) {
    const a = Math.PI / 8 * i - Math.PI / 2, rr = i % 2 ? r * 0.55 : r;
    pts.push((cx + Math.cos(a) * rr).toFixed(1) + ',' + (cy + Math.sin(a) * rr).toFixed(1));
  }
  return pts.join(' ');
}

const mirror = d => d; // paths below are written out for both sides

function pageText(lines, side) {
  // lines follow the page's slight curve: each starts a little lower toward the spine
  return lines.map((t, i) => {
    const y = 174 + i * 19;
    const x = side < 0 ? 172 : 340;
    const rot = side < 0 ? -5 : 5;
    return '<text x="' + x + '" y="' + y + '" transform="rotate(' + rot + ' ' + x + ' ' + y + ')" text-anchor="middle" ' +
      'font-family="Traditional Arabic, Arial, sans-serif" font-size="13.5" fill="#2B2416" direction="rtl">' + t + '</text>';
  }).join('');
}

function rehalSvg(bg) {
  const BG = bg === 'navy'
    ? ['#23365A', '#152440', '#0C1628']
    : ['#2F6B50', '#17412F', '#0C2A1E'];
  return '' +
  '<defs>' +
    '<radialGradient id="bg" cx="50%" cy="40%" r="70%">' +
      '<stop offset="0" stop-color="' + BG[0] + '"/><stop offset="0.55" stop-color="' + BG[1] + '"/><stop offset="1" stop-color="' + BG[2] + '"/></radialGradient>' +
    '<radialGradient id="glow" cx="50%" cy="50%" r="50%">' +
      '<stop offset="0" stop-color="#FFE3A3" stop-opacity="0.42"/><stop offset="1" stop-color="#FFE3A3" stop-opacity="0"/></radialGradient>' +
    '<linearGradient id="wood" x1="0" y1="0" x2="1" y2="0">' +
      '<stop offset="0" stop-color="#6E3F1C"/><stop offset="0.35" stop-color="#B7773F"/><stop offset="0.6" stop-color="#C98C52"/><stop offset="1" stop-color="#7A4722"/></linearGradient>' +
    '<linearGradient id="woodEdge" x1="0" y1="0" x2="0" y2="1">' +
      '<stop offset="0" stop-color="#5A3216"/><stop offset="1" stop-color="#3A1F0C"/></linearGradient>' +
    '<linearGradient id="pgL" x1="1" y1="0" x2="0" y2="0">' +
      '<stop offset="0" stop-color="#CDBB90"/><stop offset="0.12" stop-color="#EFE3C4"/><stop offset="0.6" stop-color="#FBF4E2"/><stop offset="1" stop-color="#EADCB8"/></linearGradient>' +
    '<linearGradient id="pgR" x1="0" y1="0" x2="1" y2="0">' +
      '<stop offset="0" stop-color="#CDBB90"/><stop offset="0.12" stop-color="#EFE3C4"/><stop offset="0.6" stop-color="#FBF4E2"/><stop offset="1" stop-color="#EADCB8"/></linearGradient>' +
    '<linearGradient id="gold" x1="0" y1="0" x2="0" y2="1">' +
      '<stop offset="0" stop-color="#F3D77A"/><stop offset="0.5" stop-color="#C9A227"/><stop offset="1" stop-color="#8C6A14"/></linearGradient>' +
    '<filter id="soft" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="9"/></filter>' +
  '</defs>' +
  '<rect width="512" height="512" fill="url(#bg)"/>' +
  '<ellipse cx="256" cy="230" rx="220" ry="170" fill="url(#glow)"/>' +

  // ---- the rehal: two carved boards crossing, the far one first
  '<ellipse cx="256" cy="466" rx="170" ry="14" fill="#000" opacity="0.35" filter="url(#soft)"/>' +
  '<polygon points="' + board(362, 262, 142, 452, 23) + '" fill="url(#woodEdge)" transform="translate(0 7)"/>' +
  '<polygon points="' + board(362, 262, 142, 452, 23) + '" fill="url(#wood)"/>' +
  '<polygon points="' + star(197, 405, 12) + '" fill="#2A1608"/><circle cx="197" cy="405" r="3" fill="url(#gold)"/>' +
  '<polygon points="' + board(150, 262, 370, 452, 23) + '" fill="url(#woodEdge)" transform="translate(0 7)"/>' +
  '<polygon points="' + board(150, 262, 370, 452, 23) + '" fill="url(#wood)"/>' +
  '<path d="M' + [158, 272, 356, 442].join(' ') + '" stroke="#E2A866" stroke-width="2" opacity="0.5"/>' +
  '<path d="M' + [162, 262, 362, 432].join(' ') + '" stroke="#5E3417" stroke-width="1.2" opacity="0.45"/>' +
  '<polygon points="' + star(315, 405, 12) + '" fill="#2A1608"/><circle cx="315" cy="405" r="3" fill="url(#gold)"/>' +
  '<circle cx="256" cy="353" r="9" fill="url(#gold)" stroke="#6B4A0C" stroke-width="2"/>' +
  '<rect x="114" y="446" width="60" height="16" rx="6" fill="url(#wood)" stroke="#4A2810" stroke-width="2"/><rect x="338" y="446" width="60" height="16" rx="6" fill="url(#wood)" stroke="#4A2810" stroke-width="2"/>' +

  // ---- the book's shadow on the stand, then the cover, the page block, the pages
  '<path d="M256 318 C214 292 146 286 80 298 L256 330 L432 298 C366 286 298 292 256 318 Z" fill="#000" opacity="0.4" filter="url(#soft)"/>' +
  '<path d="M256 150 C220 122 146 112 70 126 L60 300 C144 288 214 294 256 320 C298 294 368 288 452 300 L442 126 C366 112 292 122 256 150 Z" fill="#1C4A33" stroke="url(#gold)" stroke-width="3"/>' +
  // page block thickness, stacked edges
  '<path d="M256 144 C220 118 150 108 80 120 L72 292 C148 280 216 286 256 312 C296 286 364 280 440 292 L432 120 C362 108 292 118 256 144 Z" fill="#D9CAA2"/>' +
  '<path d="M76 206 L74 290 M79 206 L77 288 M436 206 L438 290 M433 206 L435 288" stroke="#B9A77C" stroke-width="1"/>' +
  '<path d="M80 290 C150 279 216 284 256 309 C296 284 362 279 432 290" stroke="#BCA97D" stroke-width="1.2" fill="none"/>' +
  '<path d="M256 140 C220 114 152 104 86 115 L80 283 C150 271 216 277 256 302 Z" fill="url(#pgL)"/>' +
  '<path d="M256 140 C292 114 360 104 426 115 L432 283 C362 271 296 277 256 302 Z" fill="url(#pgR)"/>' +
  // the gold-framed text block on each page
  '<path d="M242 156 C212 136 160 128 104 136 L100 262 C156 254 208 258 242 278 Z" fill="none" stroke="url(#gold)" stroke-width="4"/>' +
  '<path d="M236 162 C208 144 162 137 110 143 L107 255 C158 248 206 252 236 270 Z" fill="none" stroke="#1C4A33" stroke-width="1.4"/>' +
  '<path d="M270 156 C300 136 352 128 408 136 L412 262 C356 254 304 258 270 278 Z" fill="none" stroke="url(#gold)" stroke-width="4"/>' +
  '<path d="M276 162 C304 144 350 137 402 143 L405 255 C354 248 306 252 276 270 Z" fill="none" stroke="#1C4A33" stroke-width="1.4"/>' +
  // surah cartouches
  '<g transform="rotate(-6 172 160)"><rect x="128" y="148" width="92" height="18" rx="9" fill="url(#gold)"/><rect x="138" y="152" width="72" height="10" rx="5" fill="#1C4A33"/></g>' +
  '<g transform="rotate(6 340 160)"><rect x="292" y="148" width="92" height="18" rx="9" fill="url(#gold)"/><rect x="302" y="152" width="72" height="10" rx="5" fill="#1C4A33"/></g>' +
  pageText(LEFT.map((t, i) => t), -1).replace(/y="(\d+)"/g, (m, v) => 'y="' + (+v + 14) + '"') +
  pageText(RIGHT, 1).replace(/y="(\d+)"/g, (m, v) => 'y="' + (+v + 14) + '"') +
  // the gutter's shade
  '<path d="M250 146 C254 190 254 250 252 300 L260 300 C258 250 258 190 262 146 Z" fill="#8C7A50" opacity="0.35"/>' +
  // a ribbon marker falling from the spine
  '<path d="M252 300 L248 372 L256 362 L264 372 L260 300 Z" fill="#9C2F2A"/>' +
  '';
}

function svg(size, bg) {
  return Buffer.from('<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size +
    '" viewBox="0 0 512 512">' + rehalSvg(bg) + '</svg>');
}

module.exports = { svg, rehalSvg };
