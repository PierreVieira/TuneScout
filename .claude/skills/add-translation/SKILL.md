---
name: add-translation
description: "Add a new language to the app: register it in the translations check, translate every module's strings.xml, verify the build and update the docs."
---

# Add Translation

This skill adds a whole new language to TuneScout. English in `values/strings.xml` is the source of
truth; every other language lives in a `values-<qualifier>/strings.xml` next to it, in every module
that has strings.

The `translations` workflow runs [`scripts/check_translations.py`](../../../scripts/check_translations.py)
on every pull request that touches a `strings.xml`. It fails when a locale listed in its
`REQUIRED_LOCALES` is missing a file, is missing a key, or declares a key the English file doesn't.
Adding the new locale there is what turns "a translation" into "a supported language": from then on,
nobody can add an English string without translating it into this language too.

## Step-by-step workflow

### 1. Pick the resource qualifier

Ask the user which language only if the request doesn't name it. Then choose the folder qualifier:

- **Language only** (`values-es`, `values-fr`, `values-de`) by default. It covers every region of
  that language — `values-es` serves Spain, Mexico and Argentina alike — and Android falls back to
  it from any regional locale.
- **Language + region** (`values-pt-rBR`, `values-es-rMX`) only when the copy really differs by
  region *and* the user asked for that specific variant. Brazilian Portuguese is regional because
  European Portuguese differs enough to matter.

Check the qualifier isn't already present:

```bash
find . -type d -name 'values-<qualifier>' -not -path '*/build/*'
```

### 2. Register the locale in the check first

Add the folder name to `REQUIRED_LOCALES` in `scripts/check_translations.py`:

```python
REQUIRED_LOCALES = ('values-pt-rBR', 'values-<qualifier>')
```

Then run the check before translating anything:

```bash
python3 scripts/check_translations.py
```

It must fail with one `file is missing (N untranslated strings)` line per module. That list is the
work to do, and it proves the check sees the new locale — if it passes here, the qualifier is wrong.

### 3. Translate every module

For each file the check reported, read the English `values/strings.xml` **and** the existing
`values-pt-rBR/strings.xml` next to it (the Portuguese shows which strings are casual, which are
button labels and which are content descriptions), then write `values-<qualifier>/strings.xml`.

Rules:

- **Same keys, same order** as the English file. Keep the XML header and `<resources>` root.
- **Skip `translatable="false"` entries entirely** — don't copy them (e.g. `widget_preview_elapsed`).
- **Keep the brand**: `TuneScout` and `iTunes` are never translated (`app_name`,
  `widget_now_playing_label`, …).
- **Keep format arguments exactly**: `%1$s`, `%1$d` stay, with the same index and type. Reorder
  them inside the sentence if the grammar needs to, but never drop one.
- **Escape apostrophes** as `\'`, like the English files do. Quotes need `\"`; `&` needs `&amp;`.
- **Plurals**: translate every `<plurals>` and give each quantity the language needs. Two
  (`one`, `other`) cover English, Portuguese and Spanish; languages like Russian or Polish also need
  `few`/`many`. Look up the language's CLDR plural rules when unsure.
- **Tone**: informal and direct, addressing the user as "you" (Spanish *tú*, French *tu*,
  Portuguese *você*), short sentences, the same as the English copy.
- **Terms stay consistent across modules**: the same English word (Like, Queue, Library, Playlist)
  gets the same translation everywhere — `ui_component`, `core/playback`, `song_options` and the
  widget all repeat labels. Follow what the platform's music apps use in that language (e.g. many
  keep "playlist" untranslated).
- **Content descriptions** (`ui_play`, `ui_skip_next`, `queue_reorder`, …) are read by TalkBack:
  translate them as spoken phrases, not as abbreviations.

### 4. Verify

Run the check again — it must now pass and list the new locale:

```bash
python3 scripts/check_translations.py
```

Then compile the resources, so a malformed XML, a broken escape or a bad plural fails here instead
of in CI:

```bash
./gradlew assembleDebug
```

Optionally see it on a device or emulator by switching the system language. (The app declares no
`localeConfig`, so it doesn't appear in Android 13's per-app language settings.)

### 5. Decide on a screenshot variant

`tools/screenshot_tests/.../ScreenshotVariant.kt` has a `LARGE_FONT_PT_BR` variant because
Portuguese copy is longer than English, and a longer language at a 1.5x font scale is what
truncates first. Don't add a variant per language — it multiplies the images in the repository.

- If the new language is **not noticeably longer than Portuguese** (Spanish, French, Italian),
  don't add a variant; the Portuguese one already covers the long-copy case.
- If it **is** longer (German, Finnish, Russian), ask the user whether it should replace or join
  the Portuguese variant. A new variant needs its references recorded on CI (see
  [`docs/screenshots.md`](../../../docs/screenshots.md) and
  [`docs/testing/screenshot-tests.md`](../../../docs/testing/screenshot-tests.md)), never on macOS.

### 6. Update the docs

Every place that lists the supported languages or describes the check:

- `README.md` — the feature bullet that lists the languages.
- `docs/features.md` — the line that lists the languages.
- `docs/getting-started.md` — the `check_translations.py` row in the commands table.
- `docs/ci.md` — the `translations` row in the workflows table.
- The module docstring at the top of `scripts/check_translations.py`, if it names locales.

Find anything else with:

```bash
grep -rn "Brazilian Portuguese\|values-pt-rBR" --include='*.md' --include='*.py' .
```

### 7. Wrap up

Summarize for the user: the qualifier used, how many modules were translated, the check result, and
any term you had to choose between (so a native speaker can review it). Then offer the `create-pr`
skill — the branch type is `feature`, since users get a new language.

## Adding strings later

Once a language is in `REQUIRED_LOCALES`, every new English string needs its translation in the
same pull request — the `translations` workflow fails otherwise. When the check reports
`missing translation for: <keys>`, add those keys to each listed file following the rules in step 3.
When it reports `not declared in values/strings.xml`, the key was renamed or removed in English:
rename or remove it in the translation too.
