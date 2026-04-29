package se.vedret.app.data

enum class LocationMode {
    Auto, Static;
    companion object {
        fun parse(value: String?): LocationMode = entries.firstOrNull { it.name == value } ?: Auto
    }
}

enum class ThemeMode {
    RosePine, RosePineDawn;
    companion object {
        fun parse(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: RosePine
    }
}

enum class RefreshInterval(val minutes: Int) {
    Off(0),
    Every15(15),
    Every30(30),
    Every60(60),
    Every120(120),
    Every240(240);

    companion object {
        fun fromMinutes(m: Int): RefreshInterval = entries.firstOrNull { it.minutes == m } ?: Off
    }
}
