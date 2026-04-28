package se.vedret.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import se.vedret.app.data.Condition
import se.vedret.app.data.DayForecast
import se.vedret.app.ui.theme.LocalVedretExtras
import kotlin.math.roundToInt

@Composable
fun DayCard(day: DayForecast) {
    var expanded by remember { mutableStateOf(false) }
    val extras = LocalVedretExtras.current
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, extras.line, RoundedCornerShape(16.dp)),
    ) {
        Column(modifier = Modifier.clickable { expanded = !expanded }.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(day.label.lowercase(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.width(96.dp))
                Text(Condition.emojiFor(day.condition), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                Text(Condition.displayFor(day.condition), color = extras.muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Text("${day.high.roundToInt()}° / ${day.low.roundToInt()}°", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = extras.muted,
                    modifier = Modifier.rotate(rotation),
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${day.windSpeed.roundToInt()} m/s", style = MaterialTheme.typography.bodySmall, color = extras.faint)
                Text("${day.rainProbability}%", style = MaterialTheme.typography.bodySmall, color = extras.rain)
                if (day.uvIndex >= 3.0) Text("uv ${day.uvIndex}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            AnimatedVisibility(visible = expanded) {
                val slots = day.slots.orEmpty()
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        slots.forEach { s ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(timeLabel(s.time), style = MaterialTheme.typography.bodySmall, color = extras.muted)
                                Text(Condition.emojiFor(s.condition), style = MaterialTheme.typography.titleLarge)
                                Text("${s.temperature.roundToInt()}°", style = MaterialTheme.typography.bodyLarge)
                                Text("${s.rainProbability}%", style = MaterialTheme.typography.bodySmall, color = extras.rain)
                            }
                        }
                    }
                }
            }
        }
    }
}
