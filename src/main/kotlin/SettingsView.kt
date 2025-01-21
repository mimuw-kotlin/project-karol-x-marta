import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.awt.Color

@Composable
fun settingsDialog(
    sequenceLength: Int,
    onSequenceLengthChange: (Int) -> Unit,
    maxAttempts: Int,
    onMaxAttemptsChange: (Int) -> Unit,
    colorsList: List<String>,
    onColorsListChange: (List<String>) -> Unit,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit
) {
    var selectedSequenceLength by remember { mutableStateOf(sequenceLength) }
    var selectedMaxAttempts by remember { mutableStateOf(maxAttempts) }
    var selectedColorsList by remember { mutableStateOf(colorsList.joinToString(" ")) }
 //   var errorMessage by remember { mutableStateOf(" ") }
    var isInputValid by remember { mutableStateOf(true) }

    var showError by remember { mutableStateOf(false) }

    var selectedColors by remember { mutableStateOf(colorsList) }
    val allColors = ALL_COLORS.keys

    fun validateColorsList(input: List<String>): Boolean {
        val colors = input // .split(" ").map(String::trim)
        return colors.all { it.matches(Regex("^[A-Z]+$")) } &&
            colors.size in MIN_COLORS..MAX_COLORS &&
            colors.toSet().size == colors.size
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Game Settings") },
        text = {
            Column {
                Text("Sequence Length\n")
                SpinBox(
                    value = selectedSequenceLength,
                    onValueChange = {
                        selectedSequenceLength = it
                        onSequenceLengthChange(it)
                    },
                    minValue = MIN_SEQ_LENGTH,
                    maxValue = MAX_SEQ_LENGTH,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    description = "Sequence Length"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Max Attempts\n")
                SpinBox(
                    value = selectedMaxAttempts,
                    onValueChange = {
                        selectedMaxAttempts = it
                        onMaxAttemptsChange(it)
                    },
                    minValue = MIN_ATTEMPTS,
                    maxValue = MAX_ATTEMPTS,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    description = "Max Attempts"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Colors \n")



                Row {
                    allColors.forEach { color ->
                        val isChecked = selectedColors.contains(color)
                        val canUncheck = selectedColors.size > MIN_COLORS

                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (isChecked && canUncheck) {
                                    selectedColors = selectedColors - color
                                } else if (!isChecked) {
                                    selectedColors = selectedColors + color
                                } else {
                                    showError = true
                                }
                                onColorsListChange(selectedColors)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = (ALL_COLORS[color] ?: Color.WHITE) as androidx.compose.ui.graphics.Color,
                                uncheckedColor = (ALL_COLORS[color] ?: Color.WHITE) as androidx.compose.ui.graphics.Color
                            ),
                            modifier = Modifier.semantics {
                                contentDescription = "Color"
                                testTag = color
                            }
                        )
                    }
                }

                if (showError) {
                    Text(
                        text = "You must select at least $MIN_COLORS colors.",
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )

                    LaunchedEffect(Unit) {
                        delay(1000L)
                        showError = false
                    }
                }

//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    TextField(
//                        value = selectedColorsList,
//                        onValueChange = {
//                            selectedColorsList = it
//                            val listOfColors = it.uppercase().split(" ").map(String::trim)
//                            isInputValid = validateColorsList(listOfColors)
//                            if (isInputValid) {
//                                errorMessage = " "
//                                onColorsListChange(listOfColors)
//                            } else {
//                                errorMessage = "Invalid format. Please enter colors separated by spaces."
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth(0.9f)
//                            .onFocusChanged { focusState ->
//                                if (!focusState.isFocused && !isInputValid) {
//                                    errorMessage = "Invalid format. Please enter colors separated by spaces."
//                                }
//                            },
//                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
//                        keyboardActions = KeyboardActions(onDone = {
//                            // Hide the keyboard when the user presses the Done button
//                        })
//                    )
//                    IconButton(onClick = {
//                        selectedColorsList = colorsList.joinToString(" ")
//                        errorMessage = ""
//                    }) {
//                        Icon(Icons.Default.Refresh, contentDescription = "Reset to previous value")
//                    }
//                }
//                if (errorMessage.isNotEmpty()) {
//                    Text(
//                        text = errorMessage,
//                        color = MaterialTheme.colors.error,
//                        style = MaterialTheme.typography.caption,
//                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
//                    )
//                }
            }
        },
        confirmButton = {
            Button(onClick = onApply, enabled = isInputValid) {
                Text("Apply and Restart")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
