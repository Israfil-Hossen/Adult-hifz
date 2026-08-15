<?php
/* =========================================================
   হিফজ · দৈনিক সঙ্গী — সার্ভার সেভ (কোড-ভিত্তিক, অ্যাকাউন্ট ছাড়া)

   বসানোর নিয়ম:
   1. এই ফাইলটা index.html-এর পাশে আপলোড করুন।
   2. পাশে একটা ফোল্ডার বানান: hifz-data  (permission 755)
   3. index.html-এর config.js-এ syncUrl বসান, যেমন:
        syncUrl: "https://your-domain.com/sync.php"

   কাজ করে যেভাবে: প্রতিটি ইউজারের একটা গোপন কোড থাকে।
   কোডটাই চাবি — অ্যাকাউন্ট, ইমেইল, পাসওয়ার্ড কিছু লাগে না।
   ফাইলের নাম কোডের হ্যাশ, তাই কোড না জানলে ডেটা পাওয়া যায় না।
   ========================================================= */

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json; charset=utf-8");
if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") { http_response_code(204); exit; }

const DATA_DIR  = __DIR__ . "/hifz-data";
const MAX_BYTES = 2097152;          // ২ মেগাবাইট — যথেষ্ট বেশি
const SALT      = "hifz-v1";        // চাইলে বদলান; বদলালে পুরনো ফাইল আর মিলবে না

function fail($msg, $code = 400) {
  http_response_code($code);
  echo json_encode(["ok" => false, "error" => $msg], JSON_UNESCAPED_UNICODE);
  exit;
}

$code = isset($_GET["code"]) ? trim($_GET["code"]) : "";
if (!preg_match('/^[A-Za-z0-9\-]{8,64}$/', $code)) fail("bad code");

if (!is_dir(DATA_DIR)) {
  if (!@mkdir(DATA_DIR, 0755, true)) fail("cannot create data dir", 500);
}
$file = DATA_DIR . "/" . hash("sha256", SALT . $code) . ".json";

if ($_SERVER["REQUEST_METHOD"] === "GET") {
  if (!is_file($file)) { echo json_encode(["ok" => true, "empty" => true]); exit; }
  $raw = file_get_contents($file);
  echo json_encode([
    "ok"   => true,
    "data" => json_decode($raw, true),
    "at"   => filemtime($file) * 1000
  ], JSON_UNESCAPED_UNICODE);
  exit;
}

if ($_SERVER["REQUEST_METHOD"] === "POST") {
  $raw = file_get_contents("php://input");
  if ($raw === false || $raw === "") fail("empty body");
  if (strlen($raw) > MAX_BYTES)     fail("too large", 413);
  $in = json_decode($raw, true);
  if (!is_array($in)) fail("not json");

  /* পুরনো ডেটা নতুনের উপরে যেন না বসে: updatedAt মিলিয়ে নিই */
  if (is_file($file)) {
    $old = json_decode(file_get_contents($file), true);
    $oldAt = is_array($old) && isset($old["updatedAt"]) ? (int)$old["updatedAt"] : 0;
    $newAt = isset($in["updatedAt"]) ? (int)$in["updatedAt"] : 0;
    if ($newAt > 0 && $oldAt > $newAt) {
      echo json_encode(["ok" => true, "stale" => true, "data" => $old, "at" => $oldAt], JSON_UNESCAPED_UNICODE);
      exit;
    }
  }

  /* একই সময়ে দুই ডিভাইস লিখলেও ফাইল যেন আধা-লেখা না থাকে */
  $tmp = $file . "." . getmypid() . ".tmp";
  if (@file_put_contents($tmp, $raw) === false) fail("cannot write", 500);
  @rename($tmp, $file);
  echo json_encode(["ok" => true, "at" => time() * 1000]);
  exit;
}

fail("method not allowed", 405);
