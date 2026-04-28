package se.vedret.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.vedret.app.R
import se.vedret.app.ui.theme.LocalVedretExtras

@Composable
fun SearchPill(
    value: String,
    searching: Boolean,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onUseLocation: () -> Unit,
    onFocusChange: (Boolean) -> Unit = {},
) {
    val extras = LocalVedretExtras.current
    val accent = MaterialTheme.colorScheme.primary

    // Tap-to-blank: stash the visible city on focus so we can restore it if the
    // user backs out without typing or submitting. Cleared on every blur — if
    // the user did type or submitted, we won't undo their work.
    var stashed by remember { mutableStateOf<String?>(null) }
    val focusMod = Modifier.onFocusChanged { f ->
        onFocusChange(f.isFocused)
        if (f.isFocused) {
            if (stashed == null && value.isNotEmpty()) {
                stashed = value
                onValueChange("")
            }
        } else {
            val saved = stashed
            stashed = null
            if (value.isEmpty() && saved != null) onValueChange(saved)
        }
    }

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse",
    )
    val borderAlpha by animateFloatAsState(if (searching) pulse else 1f, label = "border")

    val pillTextStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 18.sp,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(
                width = if (searching) 1.5.dp else 1.dp,
                color = if (searching) accent.copy(alpha = borderAlpha) else extras.line,
                shape = CircleShape,
            )
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = pillTextStyle,
                cursorBrush = SolidColor(accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit(value) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .then(focusMod),
            )
            if (value.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_hint),
                    color = extras.faint,
                    style = pillTextStyle.copy(color = extras.faint),
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }
        IconButton(onClick = onUseLocation, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = stringResource(R.string.use_my_location),
                tint = extras.rain,
            )
        }
    }
}
