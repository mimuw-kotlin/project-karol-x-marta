import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.onFocusChanged

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
    var errorMessage by remember { mutableStateOf(" ") }
    var isInputValid by remember { mutableStateOf(true) }

    fun validateColorsList(input: String): Boolean {
        val colors = input.split(" ").map(String::trim)
        return colors.all { it.matches(Regex("^[A-Z]+$")) }
                && colors.size in MIN_COLORS..MAX_COLORS
                && colors.toSet().size == colors.size
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Game Settings") },
        text = {
            Column {
                Text("Sequence Length\n")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    IconButton(
                        onClick = {
                            if (selectedSequenceLength > MIN_SEQ_LENGTH) {
                                selectedSequenceLength--
                                onSequenceLengthChange(selectedSequenceLength)
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
                                onSequenceLengthChange(selectedSequenceLength)
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
                                onMaxAttemptsChange(selectedMaxAttempts)
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
                                onMaxAttemptsChange(selectedMaxAttempts)
                            }
                        },
                        enabled = selectedMaxAttempts < MAX_ATTEMPTS
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Increase")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Colors List\n")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = selectedColorsList,
                        onValueChange = {
                            selectedColorsList = it
                            isInputValid = validateColorsList(it.uppercase())
                            if (isInputValid) {
                                errorMessage = " "
                                onColorsListChange(it.uppercase().split(" ").map(String::trim))
                            } else {
                                errorMessage = "Invalid format. Please enter colors separated by spaces."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && !isInputValid) {
                                    errorMessage = "Invalid format. Please enter colors separated by spaces."
                                }
                            },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            // Hide the keyboard when the user presses the Done button
                        })
                    )
                    IconButton(onClick = {
                        selectedColorsList = colorsList.joinToString(" ")
                        errorMessage = ""
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset to previous value")
                    }
                }
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
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