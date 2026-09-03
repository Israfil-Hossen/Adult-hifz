# Play Store upload — version 1.0.8

Everything in this folder is ready to paste or upload, except two things: the
screenshots, which have to be taken on the phone (see `screenshots/README.md`),
and the bundle itself, which is a build output and is kept out of git. Build it
with `cd android && ./gradlew bundleRelease`, then copy
`android/app/build/outputs/bundle/release/app-release.aab` in here.

## The build

| | |
|---|---|
| File | `app-release.aab` — build it, see above |
| Size | about 12 MB |
| versionCode | **3** |
| versionName | **1.0.8** |
| Signed with | `adult-hifz.jks` — the same key as the previous release |
| Package | `com.israfilhossen.hifz` |

The versionCode rises with every upload; Play refuses a duplicate. 1.0.5 was
prepared but never published, so this release carries everything since 1.0.4.
Signing with the same keystore is what lets this update the existing listing
instead of becoming a new app — keep that `.jks` and its passwords backed up
off this machine, or this listing can never be updated again.

## What goes where in Play Console

| Play Console field | File here |
|---|---|
| Production / Testing → Create release → upload | `app-release.aab` |
| Release notes (en-US) | `listing-en-US/whats-new.txt` |
| Release notes (bn-BD) | `listing-bn-BD/whats-new.txt` |
| Main store listing → App name | `listing-en-US/app-name.txt` |
| Main store listing → Short description | `listing-en-US/short-description.txt` |
| Main store listing → Full description | `listing-en-US/full-description.txt` |
| Store listing → App icon | `graphics/icon-512.png` |
| Store listing → Feature graphic | `graphics/feature-graphic-1024x500.png` |
| Store listing → Phone screenshots | `screenshots/` — **you must add these** |
| App content → Privacy policy | host `privacy-policy.md` and give Play the URL |
| App content → Data safety | `data-safety.md` |

Bengali listing: **Store listings → Add language → বাংলা (bn-BD)**, then paste
from `listing-bn-BD/`.

## Category and contact

| Field | Value |
|---|---|
| App or game | App |
| Category | Education |
| Tags | Quran, Islam, Memorisation, Education |
| Contact email | israfil.crp@gmail.com |
| Website | https://israfil-hossen.github.io/Adult-hifz/ |
| Privacy policy | https://israfil-hossen.github.io/Adult-hifz/PRIVACY.html |

## Before you upload

- [ ] Take the screenshots on the phone
- [ ] Confirm the privacy policy URL actually resolves — Play checks it
- [ ] Fill the data safety form from `data-safety.md`, including the microphone note
- [ ] If a release from earlier is still in review, wait: a new one queues behind it

## If this account is a personal developer account

Google requires **12 testers on closed testing for 14 continuous days** before
production, for personal accounts opened after November 2023. Upload the bundle
to closed testing first and start the clock — the screenshots and descriptions
can be finished during those 14 days. Company accounts are exempt.
