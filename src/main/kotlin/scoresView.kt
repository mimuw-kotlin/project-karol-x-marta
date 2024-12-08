import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlin.text.get

@Composable
fun scoresDialog(
    sequenceLength : Int,
    maxAttempts : Int,
    colorsNumber: Int,
    scoresManager: ScoresManager,
    onDismissRequest: () -> Unit
) {
    var selectedSequenceLength by remember { mutableStateOf(sequenceLength) }
    var selectedMaxAttempts by remember { mutableStateOf(maxAttempts) }
    var selectedColorsNumber by remember { mutableStateOf(colorsNumber) }
    var showScores by remember { mutableStateOf(false) }
    var scores by remember { mutableStateOf(listOf<Double>()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Scores") },
        text = {
            Column {
                Text("Sequence Length\n")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    IconButton(
                        onClick = {
                            if (selectedSequenceLength > MIN_SEQ_LENGTH) {
                                selectedSequenceLength--
                            }
                        },
                        enabled = selectedSequenceLength > MIN_SEQ_LENGTH
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Decrease")
                    }
                    TextField(
                        value = selectedSequenceLength.toString(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.width(150.dp)
                    )
                    IconButton(
                        onClick = {
                            if (selectedSequenceLength < MAX_SEQ_LENGTH) {
                                selectedSequenceLength++
                            }
                        },
                        enabled = selectedSequenceLength < MAX_SEQ_LENGTH
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Increase")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Max Attempts\n")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    IconButton(
                        onClick = {
                            if (selectedMaxAttempts > MIN_ATTEMPTS) {
                                selectedMaxAttempts--
                            }
                        },
                        enabled = selectedMaxAttempts > MIN_ATTEMPTS
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Decrease")
                    }
                    TextField(
                        value = selectedMaxAttempts.toString(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.width(150.dp)
                    )
                    IconButton(
                        onClick = {
                            if (selectedMaxAttempts < MAX_ATTEMPTS) {
                                selectedMaxAttempts++
                            }
                        },
                        enabled = selectedMaxAttempts < MAX_ATTEMPTS
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Increase")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Colors Number\n")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    IconButton(
                        onClick = {
                            if (selectedColorsNumber > MIN_COLORS) {
                                selectedColorsNumber--
                            }
                        },
                        enabled = selectedColorsNumber > MIN_COLORS
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Decrease")
                    }
                    TextField(
                        value = selectedColorsNumber.toString(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.width(150.dp)
                    )
                    IconButton(
                        onClick = {
                            if (selectedColorsNumber < MAX_COLORS) {
                                selectedColorsNumber++
                            }
                        },
                        enabled = selectedColorsNumber < MAX_COLORS
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Increase")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showScores) {
                    Column {
                        Text("Scores for chosen parameters:")
                        LazyColumn {
                            items(scores.size) { index ->
                                Text("${index + 1}. ${scores[index]}s")
                            }
                        }
                    }
                }

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scores = scoresManager.getFilteredScores(selectedSequenceLength, selectedMaxAttempts, selectedColorsNumber)
                    showScores = true
                }
            ) {
                Text("Show Scores")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}