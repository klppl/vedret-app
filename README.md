# vedret

![license](https://img.shields.io/github/license/klppl/vedret-app)
![release](https://img.shields.io/github/v/release/klppl/vedret-app)
![android](https://img.shields.io/badge/android-8.0%2B-3DDC84?logo=android&logoColor=white)
![kotlin](https://img.shields.io/badge/kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![källor](https://img.shields.io/badge/väderkällor-7_st,_1_sanning-F97316)
![dalecarlian](https://img.shields.io/badge/dalecarlian-%E2%9D%A4-801818)

Native Android client for [vedret.se](https://vedret.se) — consensus weather for **Swedish locations** 🇸🇪. Instead of trusting one forecast, vedret asks SMHI, YR, Open-Meteo (plus ECMWF, DWD ICON and DMI models), OpenWeather, WeatherAPI and Pirate Weather about the same place and shows you what they agree on.

> 🇸🇪 **Sweden only.** Forecasts cover Swedish locations, and the app speaks Swedish — precis som det ska vara.

<p>
  <img src="docs/screenshot1.png" alt="Main screen: current weather for Örebro, hourly forecast row and 5-day outlook" width="270">
  <img src="docs/screenshot2.png" alt="Settings: location mode, auto-update interval and Rosé Pine themes" width="270">
</p>

## Features

- Current weather, hour-by-hour scrollable forecast, and a 5-day expandable outlook
- Rain that never hides: the day flips to 🌧️ when the sources agree rain is coming, even if the dry hours outnumber the wet ones
- Two home-screen widgets (current + upcoming hours), Glance-based
- Location by GPS, city search with autocomplete, or IP fallback
- Light/dark theme following the system

## Building

```bash
./gradlew assembleDebug
```

Release builds are signed in CI; locally the release build falls back to the debug keystore.

## License

[GPL-3.0](LICENSE)
