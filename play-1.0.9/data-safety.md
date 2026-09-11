# Data safety form — the answers to give

Play asks what the app **can** do, not what it does by default. By default nothing
leaves the device; but the user can switch on either backup path (`sync.php` with
their own code, or Google Drive), and then the whole record is transmitted. Answer
for the capability.

| Question | Answer |
|---|---|
| Does your app collect or share user data? | **Yes** |
| Data type | App activity → App interactions (the daily hifz record) |
| Purpose | App functionality (backup and moving to a new device) |
| Required or optional? | **Optional** — the user turns it on |
| Encrypted in transit? | **Yes** (HTTPS) |
| Can users request deletion? | **Yes** — from Settings |

**Say separately that the microphone recordings stay on the device** and are never
transmitted.

An incorrect data safety form is one of the most common causes of rejection.
