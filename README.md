# Emojis

A simple emoji picker for the Light Phone III, built natively against the [Light SDK](https://github.com/lightphone/light-sdk).

Browse all emoji organized by category, search by name or keyword. Tap to add emoji to your selection, tap COPY to copy them, and CLEAR to start over.

---

## Features

- Full emoji set organized by category, plus a dedicated Search screen that filters live as you type
- Tap any emoji to add it to your selection tray; COPY copies the selection, CLEAR resets it
- Recents tab: tracks usage counts and recency
- Optional "Top Used" section on the main screen (Settings), showing your most-used emoji at a glance
- 
> **Known limitation:** the Light SDK doesn't yet expose a clipboard API, so COPY is currently a
> stub. It'll be wired up once the SDK supports it.

---

## Building

This app is built against the [Light SDK](https://github.com/lightphone/light-sdk).

### Prerequisites

- JDK 17
- Android SDK
- A GitHub personal access token with `read:packages` scope, for the `lp3keyboard` dependency hosted on GitHub Packages

### Steps

```bash
git clone --recurse-submodules https://github.com/zacksimpson/emoji-tool.git
cd emoji-tool
# if you cloned without --recurse-submodules:
git submodule update --init

# GitHub Packages credentials — either env vars:
export GH_PACKAGES_USER=your_username
export GH_PACKAGES_TOKEN=your_token
# or a local.properties file (gitignored):
echo "gpr.user=your_username" >> local.properties
echo "gpr.key=your_token" >> local.properties

./gradlew :tool:installDebug   # build + install to a connected device/emulator
./gradlew :tool:assembleRelease
```

---

## Installing on Light Phone III

Once Light's tool-distribution pipeline is live, this will be installable directly from the Light dashboard/Obtainium. Until then, sideload a debug/release build via `adb install`.

---

## Support

If any of my tools have been useful to you, I'd love to hear from you! Feel free to reach out [here](mailto:zacksimpson24@gmail.com). Another way to support is to [consider sponsoring](https://github.com/sponsors/zacksimpson). Either way, it means a lot!

---

## Credits

- [Unicode Full Emoji List](https://unicode.org/emoji/charts-13.1/full-emoji-list.html) – for the full official index of emojis, names, and keywords
- [The Light Phone](https://www.thelightphone.com) – for building a phone worth making apps for
