package se.vedret.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.vedret.app.data.Condition
import se.vedret.app.data.Slot
import se.vedret.app.ui.theme.LocalVedretExtras
import kotlin.math.roundToInt

@Composable
fun Hero(current: Slot?) {
    if (current == null) return
    val extras = LocalVedretExtras.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${current.temperature.roundToInt()}",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraLight,
        )
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "grader och ${Condition.displayFor(current.condition)} ${Condition.emojiFor(current.condition)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val parts = buildList {
                add("${current.windSpeed.roundToInt()} m/s vind")
                add("${current.rainProbability}% risk för regn")
                if (current.uvIndex >= 3.0) add("UV ${current.uvIndex}")
            }
            Text(
                text = parts.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = extras.muted,
            )
        }
    }
}
