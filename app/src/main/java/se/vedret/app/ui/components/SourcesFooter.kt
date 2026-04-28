package se.vedret.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.vedret.app.R
import se.vedret.app.data.ProviderRow
import se.vedret.app.ui.theme.LocalVedretExtras
import kotlin.math.roundToInt

@Composable
fun SourcesFooter(providers: List<ProviderRow>) {
    if (providers.isEmpty()) return
    val extras = LocalVedretExtras.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.sources_label).lowercase(), color = extras.muted, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        val joined = providers.joinToString(", ") { "${it.provider} (${it.temperature.roundToInt()}°)" }
        Text(joined, color = extras.faint, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.sources_explainer), color = extras.faint, style = MaterialTheme.typography.bodySmall)
    }
}
