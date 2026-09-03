# The dhikr recordings

Twelve short files. Drop them in this folder with exactly these names and the
app plays them; leave the folder empty and nothing breaks — the reminder still
arrives, with the phone's own notification sound, which is what happens today.

Nothing else has to change. The app checks for each file when it starts, tells
Android which ones exist, and Android plays them from inside the app — no
network, no installed voice, and with the screen off.

| file | what it says |
|---|---|
| `subhanallah.mp3` | سُبْحَانَ اللّٰه |
| `alhamdulillah.mp3` | اَلْحَمْدُ لِلّٰه |
| `lailahaillallah.mp3` | لَا إِلٰهَ إِلَّا اللّٰه |
| `allahuakbar.mp3` | اَللّٰهُ أَكْبَر |
| `astaghfirullah.mp3` | أَسْتَغْفِرُ اللّٰه |
| `lahawla.mp3` | لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰه |
| `subhanallahwabihamdih.mp3` | سُبْحَانَ اللّٰهِ وَبِحَمْدِهِ |
| `sallialamuhammad.mp3` | اَللّٰهُمَّ صَلِّ عَلٰى مُحَمَّد |
| `hasbunallah.mp3` | حَسْبُنَا اللّٰهُ وَنِعْمَ الْوَكِيل |
| `durood-ibrahim.mp3` | اَللّٰهُمَّ صَلِّ عَلٰى مُحَمَّدٍ وَعَلٰى آلِ مُحَمَّد |
| `sallallahualayhi.mp3` | صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّم |
| `sallwasallim.mp3` | اَللّٰهُمَّ صَلِّ وَسَلِّمْ عَلٰى نَبِيِّنَا مُحَمَّد |

## Recording them

Mono, 48 kbps is plenty for a voice, 1–3 seconds each. That comes to roughly
20 KB a file and 250 KB for the set — nothing next to a 16 MB app.

    ffmpeg -i raw.wav -ac 1 -b:a 48k -af "silenceremove=1:0:-45dB" subhanallah.mp3

Trim the silence at both ends. A reminder that starts with half a second of
room tone sounds broken.

## Licensing

Whatever goes in here ships inside the app and reaches every reader, so it has
to be something you have the right to distribute: your own recording, or a file
whose licence plainly allows redistribution. A file that is merely free to
download is not the same thing.
