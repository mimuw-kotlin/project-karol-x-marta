import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.system.exitProcess
import androidx.compose.ui.text.font.FontWeight
import kotlin.collections.toMutableList
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlin.div

@Composable
@Preview
fun app() {
    var text by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var placeholder by remember { mutableStateOf("Enter your guess (space-separated colors)") }
    var gameOver by remember { mutableStateOf(false) }

    // TODO: dodać możliwość zmiany ustawień gry
    val settings = Settings(sequenceLength = 4, maxAttempts = 10, colorsList = listOf("A", "B", "C", "D", "E", "F"))
    var game by remember { mutableStateOf(Game(settings, manualCode = "")) }
    val guesses = remember { mutableStateListOf<Pair<List<String>, Feedback>>() }

    var startTime by remember { mutableStateOf<Long?>(null) }
    var timer by remember { mutableStateOf(0L) }

    LaunchedEffect(startTime) {
        while (startTime != null && !gameOver) {
            delay(10L)
            startTime?.let {
                timer = System.currentTimeMillis() - it
            }
        }
    }

    fun submitGuess() {
        if (startTime == null) {
            startTime = System.currentTimeMillis()
        }

        var guess = input.split(" ").map(String::trim).toMutableList()
        while (!game.player.validateGuess(guess, settings.colorsList)) {
            input = ""
            placeholder = "Your guess has bad format. Please enter ${settings.sequenceLength} colors separated by spaces."
            return
        }
        val feedback = game.checkGuess(guess)
        guesses.add(guess to feedback)
        if (game.isSolved) {
            text = "Congratulations! You've guessed the sequence in ${game.attempts} attempts.\n"
            gameOver = true
        } else if (game.isGameOver()) {
            text = "Game Over! The correct sequence was: ${game.getSecretCode().joinToString(", ")}\n"
            gameOver = true
        } else {
            text = "Try again. Attempts left: ${settings.maxAttempts - game.attempts}\n"
        }
        input = ""
        placeholder = "Enter your guess (space-separated colors)"
    }

    fun resetGame() {
        text = "Enter your guess (space-separated colors):"
        input = ""
        gameOver = false
        guesses.clear()
        game = Game(settings, manualCode = "")
        startTime = null
        timer = 0L
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // TODO: dodać zegar (nwm czy to na pewno właściwe miejsce)
                Text(text, fontWeight = FontWeight.Bold)
                guesses.forEach { (guess, feedback) ->
                    Text("Guess: ${guess.joinToString(", ")} - Feedback: $feedback")
                }
            }
            Text(
                text = "Time: ${"%.3f".format(timer / 1000.0)}s",
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            )
            if (gameOver) {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { resetGame() }) {
                        Text("New Game")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { exitProcess(0) }) {
                        Text("Exit")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text(placeholder) },
                        modifier = Modifier.weight(1f).onKeyEvent {
                            if (it.key == Key.Enter) {
                                submitGuess()
                                true
                            } else {
                                false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { submitGuess() }) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

//TODO: potem skasować, przydatne do testowania
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Mastermind - Game") {
        app()
    }
}