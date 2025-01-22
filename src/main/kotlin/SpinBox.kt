import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.text.toIntOrNull

@Composable
fun SpinBox(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int,
    maxValue: Int,
    description: String,
    modifier: Modifier = Modifier,
) {
    var textValue by remember { mutableStateOf(value.toString()) }
    var lastValidValue by remember { mutableIntStateOf(value) }
    var isOutOfRange by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        IconButton(
            onClick = {
                if (value > minValue) {
                    val newValue = value - 1
                    textValue = newValue.toString()
                    lastValidValue = newValue
                    onValueChange(newValue)
                    isOutOfRange = false
                }
            },
            enabled = value > minValue,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Decrease $description")
        }
        Column {
            TextField(
                value = textValue,
                onValueChange = {
                    textValue = it
                    isOutOfRange = it.toIntOrNull()?.let { newValue ->
                        newValue !in minValue..maxValue
                    } ?: true
                    val newValue = it.toIntOrNull()
                    if (!isOutOfRange && newValue != null) {
                        lastValidValue = newValue
                        onValueChange(newValue)
                        isOutOfRange = false
                    }
                },
                readOnly = false,
                modifier =
                    Modifier
                        .width(150.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                val newValue = textValue.toIntOrNull()
                                if (newValue == null || newValue !in minValue..maxValue) {
                                    textValue = lastValidValue.toString()
                                    isOutOfRange = false
                                }
                            }
                        }.semantics {
                            contentDescription = description
                        },
            )
            if (isOutOfRange) {
                Text(
                    text = "Allowed range: $minValue - $maxValue",
                    color = androidx.compose.ui.graphics.Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
        IconButton(
            onClick = {
                if (value < maxValue) {
                    val newValue = value + 1
                    textValue = newValue.toString()
                    lastValidValue = newValue
                    onValueChange(newValue)
                    isOutOfRange = false
                }
            },
            enabled = value < maxValue,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Increase $description")
        }
    }
}
