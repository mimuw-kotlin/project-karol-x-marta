import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun GuessInput(
    colorsList: List<String>,
    onSubmitGuess: (List<String>) -> Unit,
    guessSize: Int,
    reset: Boolean
) {
    var currentGuess by remember(reset) { mutableStateOf(List(guessSize) { "" }) }
    var expandedIndex by remember { mutableStateOf(-1) }

    Row {
        currentGuess.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ALL_COLORS[color] ?: Color.White)
                    .border(2.dp, if (color.isEmpty()) Color.Gray else Color.Transparent, CircleShape)
                    .clickable {
                        expandedIndex = if (expandedIndex == index) -1 else index
                    }
                    .testTag("$index")
            ) {
                DropdownMenu(
                    expanded = expandedIndex == index,
                    onDismissRequest = { expandedIndex = -1 },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        onClick = {
                            currentGuess = currentGuess.toMutableList().apply { set(index, "") }
                            expandedIndex = -1
                        },
                        modifier = Modifier.height(30.dp).padding(vertical = 2.dp)
                            .testTag("No Color")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = color.isEmpty(),
                                onClick = {
                                    currentGuess = currentGuess.toMutableList().apply { set(index, "") }
                                    expandedIndex = -1
                                },
                            )
                            Text("No Color")
                        }
                    }
                    colorsList.forEach { colorOption ->
                        DropdownMenuItem(
                            onClick = {
                                currentGuess = currentGuess.toMutableList().apply { set(index, colorOption) }
                                expandedIndex = -1
                            },
                            modifier = Modifier.height(30.dp).padding(vertical = 2.dp)
                                .testTag(colorOption)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = color == colorOption,
                                    onClick = {
                                        currentGuess = currentGuess.toMutableList().apply { set(index, colorOption) }
                                        expandedIndex = -1
                                    },
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(ALL_COLORS[colorOption] ?: Color.White)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Button(
            onClick = {
            onSubmitGuess(currentGuess)
            currentGuess = List(guessSize) { "" }
            },
            Modifier.semantics {
                contentDescription = "Submit"
            },
            enabled = currentGuess.none { it.isEmpty() }
        ) {
            Text("Submit")
        }
    }
}