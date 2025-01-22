import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ScoresDialog(
    sequenceLength: Int,
    maxAttempts: Int,
    colorsNumber: Int,
    scoresManager: ScoresManager,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSequenceLength by remember { mutableIntStateOf(sequenceLength) }
    var selectedMaxAttempts by remember { mutableIntStateOf(maxAttempts) }
    var selectedColorsNumber by remember { mutableIntStateOf(colorsNumber) }
    var showScores by remember { mutableStateOf(false) }
    var scores by remember { mutableStateOf(listOf<Double>()) }

    LaunchedEffect(selectedSequenceLength, selectedMaxAttempts, selectedColorsNumber) {
        scores =
            withContext(Dispatchers.IO) {
                scoresManager.getFilteredScores(selectedSequenceLength, selectedMaxAttempts, selectedColorsNumber)
            }
        showScores = true
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Scores") },
        text = {
            Column {
                Text("Sequence Length Value\n")
                SpinBox(
                    value = selectedSequenceLength,
                    onValueChange = { selectedSequenceLength = it },
                    minValue = MIN_SEQ_LENGTH,
                    maxValue = MAX_SEQ_LENGTH,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    description = "Sequence Length",
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Max Attempts Value\n")
                SpinBox(
                    value = selectedMaxAttempts,
                    onValueChange = { selectedMaxAttempts = it },
                    minValue = MIN_ATTEMPTS,
                    maxValue = MAX_ATTEMPTS,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    description = "Max Attempts",
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Colors Number\n")
                SpinBox(
                    value = selectedColorsNumber,
                    onValueChange = { selectedColorsNumber = it },
                    minValue = MIN_COLORS,
                    maxValue = MAX_COLORS,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    description = "Colors Number",
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (showScores) {
                    Column {
                        Text("Scores for chosen parameters:")
                        LazyColumn(modifier = Modifier.height(150.dp)) {
                            items(scores.size) { index ->
                                Text("${index + 1}. ${scores[index]}s")
                            }
                        }
                    }
                }
            }
        },
        buttons = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        },
    )
}
