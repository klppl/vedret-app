# vedret.se — Android app plan

A native Android client that mirrors the look and behavior of vedret.se. This document is self-contained so it can be moved into the new app repo unchanged.

---

## 1. Goal

Reproduce the vedret.se experience on Android:

- Same content: consensus current weather, 5 hourly slots for today, 5-day expandable forecast, sources list with per-provider temperatures.
- Same look: orange accent on extralight large temperature, rounded pill search, light/dark theming, tabular numerals.
- Same Swedish copy (Idag / Imorgon / weekday names, "Källor", condition vocabulary `Klart / Halvklart / Molnigt / Regn / Snöbyar`).

---

## 2. Backend approach: use vedret.se as the API

**Recommendation: do not reimplement the aggregation in the app. Call `https://vedret.se/?...&format=json`.**

Why:

- The Go backend already does the hard parts: parallel fan-out to 6 providers, condition-vocabulary normalization, median-vs-mean tradeoffs (rain prob is median to resist outliers), 90-min slot tolerance, sunrise/sunset, geocoding, and caching. Reimplementing it in Kotlin would duplicate ~1500 lines of carefully-tuned code and force you to ship API keys (OpenWeather, WeatherAPI, Pirate Weather) inside an APK where they would leak.
- CORS is already on (`main.go:1453`) and the JSON contract is stable.
- Adding a provider on the server immediately benefits the app — no app release needed.
- Caching already lives server-side (30-min TTL per `(lat,lon)`), so the app can stay simple.

Cost of this choice: the app depends on vedret.se uptime. Mitigation: keep a 30-min in-app cache of the last successful response per location so a brief outage degrades gracefully. If you ever want true independence, SMHI + Open-Meteo are both keyless and would cover Sweden alone — but treat that as a v2 idea, not v1.

The base URL should be a build-config field (`BuildConfig.API_BASE`) so you can point a debug build at a local `go run main.go` on `:8088` over LAN.

---

## 3. JSON contract (what the app consumes)

Endpoint: `GET https://vedret.se/?<query>&format=json`

Query forms:
- `?ort=<name>&format=json` — by city name
- `?lat=<f>&lon=<f>&format=json` — by coordinates
- `?geo=ip&format=json` — IP-based fallback

Response (verified against live API, 2026-04-28):

```json
{
  "city": "Stockholm",
  "location": {
    "name": "Stockholm",
    "lat": 59.3293,
    "lon": 18.0686
  },
  "current": {
    "time": "2026-04-28T09:29:17+02:00",
    "temperature": 6.0,
    "condition": "Klart",
    "wind_speed": 4.8,
    "rain_probability": 0,
    "uv_index": 1.9
  },
  "providers": [
    { "provider": "SMHI", "temperature": 6.9 },
    { "provider": "YR",   "temperature": 5.6 }
  ],
  "today":        [ /* 5 Slot, every 3h from current hour */ ],
  "today_day":    { /* DayForecast for today, slots at 08/13/18/21 */ },
  "days":         [ /* 5 DayForecast for tomorrow..+5 */ ],
  "sunrise":      "2026-04-28T05:24:00+02:00",
  "sunset":       "2026-04-28T21:08:00+02:00",
  "generated_at": "2026-04-28T09:29:17+02:00",
  "error":        ""
}
```

`Slot` = `{time, temperature, condition, wind_speed, rain_probability, uv_index}`.
`DayForecast` = `Slot` + `{date, label, high, low, slots[]}`. `slots` may be omitted (`omitempty`) — handle null.
`location` is the resolved geocoded place — present on success, omitted on geocode failure. Persist `lat`/`lon` after a city-name search so the next launch can fetch by coordinates without re-geocoding.
`generated_at` is the server's aggregation timestamp. Combined with the `Cache-Control: public, max-age=300, stale-while-revalidate=1800` response header, this gives the HTTP client (Ktor/OkHttp) everything it needs to handle freshness automatically — no manual TTL bookkeeping in the app.
`error` is non-empty when no provider responded; render the city header + the error message and skip everything else (mirrors the web error branch). Error responses come back with `Cache-Control: no-store`, so HTTP-level caching won't trap a transient failure.

Condition strings are a fixed enum: `Klart`, `Halvklart`, `Molnigt`, `Regn`, `Snöbyar`. Map these to emoji and to a sealed Kotlin type — never invent new values.

City autocomplete corpus: `GET https://vedret.se/orter.json` returns a JSON array of ~1980 Swedish place names, sorted by population descending. Cache in `assets/` at build time *or* fetch once and persist; either works.

---

## 4. Tech stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Min SDK:** 26 (Android 8.0) — covers >95% of devices; Material 3 fully supported
- **Target SDK:** 35 (Android 15)
- **HTTP:** Ktor Client (`ktor-client-android`, `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`) — lightweight, coroutine-native. Retrofit+OkHttp is a fine alternative if you prefer.
- **JSON:** kotlinx.serialization
- **Time:** `kotlinx-datetime` with `Europe/Stockholm` zone
- **Persistence:** DataStore (Preferences) for theme + last-known location + last-response cache
- **Location:** Google Play Services `FusedLocationProviderClient`
- **Async:** Coroutines + Flow; Compose `viewModel()` from `androidx.lifecycle:lifecycle-viewmodel-compose`
- **Build:** Gradle Kotlin DSL, version catalog (`libs.versions.toml`)

Deliberately not using: Hilt (one screen, no DI needed), Room (no relational data), Retrofit (Ktor is enough). Resist adding these unless a concrete need appears.

---

## 5. Project structure

```
app/
  src/main/
    java/se/vedret/app/
      MainActivity.kt
      WeatherApp.kt              -- @Composable root, theme + nav host
      data/
        Models.kt                -- @Serializable Consensus, Location, Slot, DayForecast, ProviderRow
        Condition.kt             -- enum + emoji map + display label
        VedretApi.kt             -- Ktor client, getByCity / getByCoords / getByIp / getCities
        WeatherRepository.kt     -- single source of truth, in-memory cache (30 min)
        Prefs.kt                 -- DataStore: theme, lastQuery, lastCoords
      ui/
        WeatherScreen.kt         -- the single screen
        components/
          SearchPill.kt          -- city pill + location button + autocomplete dropdown
          Hero.kt                -- big temperature + condition + wind/rain/UV row
          TodayRow.kt            -- 5-slot horizontal scroll (3h spacing)
          DayCard.kt             -- expandable card with 4 slots (08/13/18/21)
          SourcesFooter.kt       -- per-provider temps
        theme/
          Color.kt               -- vedret palette light/dark
          Theme.kt               -- VedretTheme { ... }
          Type.kt                -- Inter, tabular num features
      location/
        LocationProvider.kt      -- wraps FusedLocationProviderClient as suspend fun
    res/
      values/strings.xml         -- Swedish strings
      font/inter_*.ttf           -- bundled, do not rely on Google Fonts at runtime
      drawable/                  -- vector sun, crosshair, chevron
    assets/orter.json            -- baked-in autocomplete corpus
```

Single-screen architecture; no Navigation Compose needed. If/when you add an "Om" page, lift then.

---

## 6. UI design — match the website

Pull the exact palette from `main.go:644-709` (`:root` and `[data-theme="dark"]` CSS variables). These map cleanly to Compose `ColorScheme`:

| CSS var       | Light       | Dark        | Compose role                  |
|---------------|-------------|-------------|-------------------------------|
| `--bg`        | `#f5f6f8`   | `#0f1115`   | `background`                  |
| `--surface`   | `#ffffff`   | `#181b22`   | `surface`                     |
| `--ink`       | `#1a1d23`   | `#e6e6e6`   | `onBackground` / `onSurface`  |
| `--muted`     | `#5a6068`   | `#9aa0a8`   | secondary text                |
| `--faint`     | `#8a8f96`   | `#6e747c`   | tertiary text                 |
| `--line`      | `#e3e5e9`   | `#262a32`   | dividers / borders            |
| `--accent`    | `#e89611`   | `#f5a623`   | `primary` (the orange)        |
| `--rain`      | `#3a86ff`   | `#5aa0ff`   | rain probability + crosshair  |

Typography:
- Inter (bundle the variable font in `res/font/`).
- Hero temperature: `fontWeight = ExtraLight (200)`, `fontSize = 80.sp` (mobile) — analogous to the web's `text-[5rem]` / `font-extralight`.
- All numbers: `fontFeatureSettings = "tnum, ss01"` for tabular numerals (`main.go:726`).
- Section labels and condition text: lowercase to match the web ("klart", "halvklart").

Layout (top to bottom on a single scroll):

1. **Header bar** — small "vedret" wordmark left, "Om" link right. The rotating-sun favicon-ish mark is optional v1; a static vector is fine.
2. **Search pill** — pinned not-quite-top. Rounded `Surface(shape = CircleShape, ...)` with a `BasicTextField` that auto-sizes to its content (Compose: measure with `rememberTextMeasurer` + key off the text). Trailing crosshair icon = "use my location". Below it, a Material `DropdownMenu` for autocomplete suggestions, max 8 entries, accent-folded substring match preserving population order.
3. **Hero** — current temperature (huge, accent color), condition + emoji, then a row of `wind m/s · rain % · UV` (UV only if ≥ 3.0, matches web behavior).
4. **Today** — `LazyRow` with 5 cards, time → emoji → temp → wind → rain → UV. Bordered rounded container.
5. **Day cards** (today + 5 future) — Compose `AnimatedVisibility` for expand/collapse. Summary row: label, emoji+condition, high/low, wind, rain%, chevron. Expanded: 4 slot cards (08/13/18/21).
6. **Sources footer** — "Källor" then comma-separated provider names with their measured temps in parentheses. Below it the explainer line about averaging.

Match the web's "details/summary" affordance with a `Row(Modifier.clickable { expanded = !expanded })` and a chevron that rotates 180° on expand.

Empty/error: if `consensus.error` is non-empty, show the search pill + a centered Swedish error string ("ingen källa svarade", "okänd ort", etc.). No hero, no slots, no days. Mirrors `main.go` partial template error branch.

---

## 7. Behaviors

**On launch:**

1. Read `lastQuery` and `lastCoords` from DataStore.
2. If `lastCoords` exists → request by coords. Else if `lastQuery` exists → request by city. Else → request by `?geo=ip`.
3. While loading, show the previous successful response from the in-memory/disk cache if any, with a subtle pulsing accent on the search pill (the web's `searching` class, `main.go:718-724`).

**Search pill submit:**

- Calls `getByCity(name)`, persists `lastQuery = name`, **and persists `lastCoords` from the response's `location` field**. Subsequent launches use coords directly — faster and avoids re-geocoding on the server.

**Crosshair button:**

- Request `ACCESS_COARSE_LOCATION` runtime permission.
- If granted: `FusedLocationProviderClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY)` with a 10s timeout → `getByCoords(lat, lon)`. Persist coords.
- If denied or timed out: fall back to `getByIp()`. Do **not** fall back to IP if the user explicitly denied — match the website's UX (`main.go:604-638`).

**Theme:**

- `ThemeMode = System | Light | Dark`, default `System`. Persist in DataStore. Toggle in a small overflow menu next to "Om" (the web has a JS toggle; native equivalent is fine in a menu).

**Refresh:**

- `pullRefresh` (Material 3) on the screen invalidates the in-memory cache for the current location and refetches.
- Optional: `WorkManager` periodic task (60–90 min) to refresh in the background for the widget. Skip in v1 unless you build the widget.

**Caching:**

- Let the HTTP client honor server `Cache-Control` headers — Ktor's `HttpCache` plugin / OkHttp's `Cache` will store the response and serve it without a network round-trip while `max-age` (300s) is in effect, and serve stale during revalidation. No manual TTL code needed in the repository layer.
- On cold start, hydrate from a single `last_response.json` in `filesDir` so the screen can render immediately before the first network call resolves. One file, one record — do not over-engineer.

---

## 8. Edge cases to mirror

- `today_day.slots` may be missing → render the day summary only.
- `current.uv_index == 0` → hide the UV chip (the web treats 0 as "not reported"; `main.go:946` etc.). Same for `wind_speed == 0` in slot rows.
- Rain probability is `int`, capped 0–100 server-side.
- Times come back with offset `+02:00` / `+01:00` depending on DST. Parse to `Instant` and format in `Europe/Stockholm` — never use device timezone, since the entire product is Sweden-only and a user in Asia should still see Swedish local times.
- City names from autocomplete preserve diacritics (`Västerås`, `Malmö`); accent-fold only for matching, never for display (`main.go:806-938`).
- Slugs in the JSON-LD / canonical URL are server-side; the app does not need to slugify.

---

## 9. Roadmap

**v0.1 — minimum viable**

- [ ] Project scaffold, Compose, theme matching website palette, Inter bundled
- [ ] `VedretApi` with three endpoints, `kotlinx.serialization` models
- [ ] `WeatherScreen` with hero + today + day cards + sources
- [ ] City pill with manual input (no autocomplete yet)
- [ ] Persist `lastQuery`, restore on launch
- [ ] Light/dark via system

**v0.2 — feature parity with website**

- [ ] Autocomplete (load `orter.json` from app assets, accent-folded substring filter)
- [ ] Crosshair location button, runtime permission, IP fallback
- [ ] Pull-to-refresh
- [ ] Manual theme toggle override (System/Light/Dark)
- [ ] In-memory + disk cache (30 min)
- [ ] "Om" screen — port the `omLayout` content

**v0.3 — Android-native extras (optional)**

- [ ] Home-screen widget (current temp + city + 4 named slots), backed by `WorkManager` periodic refresh
- [ ] Quick Settings Tile showing current temp for last city
- [ ] Share-as-image / share-as-text (the server's `?text` and `?md` formats are useful here — just call them and pipe to `Intent.ACTION_SEND`)
- [ ] Calendar subscribe button → open `?ics` URL in default calendar app

---

## 10. Open questions to decide before coding

1. **Bundle `orter.json` in assets, or fetch on first launch?** Bundling = ~22 KB, instant autocomplete, but gets stale if the corpus changes. Fetching on first launch with on-disk cache is more correct. Recommend: bundle in v0.2, fetch-with-fallback in v0.3.
2. **Fonts at runtime vs. bundled?** Bundle Inter — runtime Google Fonts adds a network dep and a cold-start delay for no benefit.
3. **Single screen or bottom nav?** Single screen for v0.x. Add nav only when you have a second destination users will visit often (Saved cities? Settings? "Om"?).
4. **Saved cities / favorites?** Not on the website. Decide whether the app should diverge here. If yes, `Room` becomes justified; if no, keep the single-DataStore-key approach.
5. **Analytics / crash reporting?** None on the website. If you add Firebase Crashlytics, match the website's privacy posture in a privacy policy.

---

## 11. References

- Website source: `github.com/klppl/vedret.se` (this repo)
- Public API base: `https://vedret.se/`
- Live test: `curl 'https://vedret.se/?ort=Stockholm&format=json'`
- Color/typography source of truth: `main.go:644-709` (CSS variables) and `main.go:715-735` (font features)
- Condition vocabulary: `providers/providers.go` `Conditions` slice
- Autocomplete corpus: `https://vedret.se/orter.json`
