package se.vedret.app.data

enum class Condition(val raw: String, val emoji: String) {
    Klart("Klart", "☀️"),         // ☀️
    Halvklart("Halvklart", "🌤️"), // 🌤️
    Molnigt("Molnigt", "☁️"),     // ☁️
    Regn("Regn", "🌧️"),     // 🌧️
    Snobyar("Snöbyar", "🌨️"); // 🌨️

    val display: String get() = raw.lowercase()

    companion object {
        fun parse(raw: String?): Condition? = entries.firstOrNull { it.raw == raw }
        fun emojiFor(raw: String?): String = parse(raw)?.emoji ?: ""
        fun displayFor(raw: String?): String = parse(raw)?.display ?: (raw?.lowercase().orEmpty())
    }
}
