package se.vedret.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.vedret.app.data.Condition
import se.vedret.app.data.Slot
import se.vedret.app.ui.theme.LocalVedretExtras
import kotlin.math.roundToInt

@Composable
fun TodayRow(slots: List<Slot>) {
    val extras = LocalVedretExtras.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, extras.line, RoundedCornerShape(16.dp)),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(slots) { slot -> SlotCell(slot) }
        }
    }
}

@Composable
private fun SlotCell(slot: Slot) {
    val extras = LocalVedretExtras.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp),
    ) {
        Text(timeLabel(slot.time), style = MaterialTheme.typography.bodySmall, color = extras.muted)
        Spacer(Modifier.height(6.dp))
        Text(Condition.emojiFor(slot.condition), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(2.dp))
        Text("${slot.temperature.roundToInt()}°", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        Text("${slot.windSpeed.roundToInt()} m/s", style = MaterialTheme.typography.bodySmall, color = extras.faint)
        Text("${slot.rainProbability}%", style = MaterialTheme.typography.bodySmall, color = extras.rain)
        if (slot.uvIndex >= 3.0) {
            Text("UV ${slot.uvIndex}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

internal fun timeLabel(iso: String): String {
    // ISO like "2026-04-28T15:00:00+02:00" → "15:00"
    val t = iso.indexOf('T').takeIf { it >= 0 } ?: return iso
    val rest = iso.substring(t + 1)
    val end = rest.indexOfFirst { it == '+' || it == '-' || it == 'Z' }
        .takeIf { it > 0 } ?: rest.length
    return rest.substring(0, end).take(5)
}
