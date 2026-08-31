<?php
/* =========================================================
   হিফজ · দৈনিক সঙ্গী — সার্ভার সেভ

   অ্যাপ এই ফাইলটার সাথে কথা বলে। কোডই একমাত্র চাবি —
   অ্যাকাউন্ট নেই, পাসওয়ার্ড নেই।

     GET  sync.php?code=XXXXXX-XXXXXX-XXXXXX
          → {"ok":true,"empty":true}
          → {"ok":true,"data":{...},"at":1699999999}

     POST sync.php?code=XXXXXX-XXXXXX-XXXXXX   (body = JSON)
          → {"ok":true,"at":1699999999}
          → {"ok":true,"stale":true,"data":{...}}   সার্ভারেরটা নতুন হলে

   রাখার জায়গা: এই ফোল্ডারের ভিতরে sync-data/ — ওয়েব থেকে
   পড়া যায় না (.htaccess), আর কোড ছাড়া কিছুই ফেরত আসে না।

   দ্রষ্টব্য: GitHub Pages-এ PHP চলে না। এই ফাইল কাজ করতে
   হলে PHP চালু আছে এমন হোস্টিং লাগবে।
   ========================================================= */

header("Content-Type: application/json; charset=utf-8");
header("Cache-Control: no-store");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");

if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") { http_response_code(204); exit; }

function out($a){ echo json_encode($a, JSON_UNESCAPED_UNICODE); exit; }
function fail($why){ out(array("ok" => false, "error" => $why)); }

/* কোডটাই চাবি, তাই আকারটা কড়া করে মেলানো হয় — যা মেলে না
   তা ফাইলের নাম হতে পারে না, কাজেই পথ ভাঙার সুযোগও নেই */
$code = isset($_GET["code"]) ? strtoupper(trim($_GET["code"])) : "";
if (!preg_match('/^[A-Z0-9]{6}-[A-Z0-9]{6}-[A-Z0-9]{6}$/', $code)) fail("bad code");

$dir = __DIR__ . "/sync-data";
if (!is_dir($dir)) {
  if (!@mkdir($dir, 0700, true)) fail("no store");
  /* সার্ভার ভুল করে ফাইলগুলো পরিবেশন করলেও যেন না দেয় */
  @file_put_contents($dir . "/.htaccess", "Require all denied\nDeny from all\n");
  @file_put_contents($dir . "/index.html", "");
}

/* কোড থেকে নামটা হ্যাশ করে বসানো হয়, যাতে ফোল্ডার দেখে
   কারো কোড পড়ে ফেলা না যায় */
$file = $dir . "/" . hash("sha256", $code) . ".json";

if ($_SERVER["REQUEST_METHOD"] === "GET") {
  if (!is_file($file)) out(array("ok" => true, "empty" => true));
  $raw = @file_get_contents($file);
  $data = json_decode($raw, true);
  if ($data === null) out(array("ok" => true, "empty" => true));
  out(array("ok" => true, "data" => $data, "at" => filemtime($file)));
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") fail("method");

$raw = file_get_contents("php://input");
if ($raw === false || $raw === "") fail("empty body");
if (strlen($raw) > 8 * 1024 * 1024) fail("too big");

$incoming = json_decode($raw, true);
if (!is_array($incoming)) fail("bad json");

$mine = isset($incoming["updatedAt"]) ? (float)$incoming["updatedAt"] : 0;

/* সার্ভারেরটা যদি নতুন হয়, তবে সেটাকে চাপা দেওয়া হয় না —
   ফেরত পাঠানো হয়, অ্যাপ নিজেই ওটা নিয়ে নেবে */
if (is_file($file)) {
  $have = json_decode(@file_get_contents($file), true);
  $theirs = (is_array($have) && isset($have["updatedAt"])) ? (float)$have["updatedAt"] : 0;
  if ($theirs > $mine) out(array("ok" => true, "stale" => true, "data" => $have));
}

/* একই সাথে দুই ফোন লিখলে যেন আধখানা ফাইল না থেকে যায় */
$tmp = $file . "." . getmypid() . ".tmp";
if (@file_put_contents($tmp, $raw) === false) fail("write");
if (!@rename($tmp, $file)) { @unlink($tmp); fail("write"); }

out(array("ok" => true, "at" => time()));
