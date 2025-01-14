import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlin.collections.toMutableList
import kotlin.system.exitProcess

val MAX_SEQ_LENGTH = 6
val MIN_SEQ_LENGTH = 3
val MAX_ATTEMPTS = 20
val MIN_ATTEMPTS = 3
val MAX_COLORS = 8
val MIN_COLORS = 3

@Composable
@Preview
fun app() {
    var text by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var placeholder by remember { mutableStateOf("Enter your guess (space-separated colors)") }
    var gameOver by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showScores by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    var sequenceLength by remember { mutableStateOf(4) }
    var maxAttempts by remember { mutableStateOf(10) }
    var colorsList by remember { mutableStateOf(listOf("A", "B", "C", "D", "E", "F")) }

    val settings = Settings(sequenceLength = sequenceLength, maxAttempts = maxAttempts, colorsList = colorsList)
    var game by remember { mutableStateOf(Game(settings)) }
    val guesses = remember { mutableStateListOf<Pair<List<String>, Feedback>>() }

    var startTime by remember { mutableStateOf<Long?>(null) }
    var pausedTime by remember { mutableStateOf<Long?>(null) }
    var timer by remember { mutableStateOf(0L) }

    var gameWonTime by remember { mutableStateOf(0L) }

    LaunchedEffect(startTime, pausedTime) {
        while (startTime != null && !gameOver && pausedTime == null && !isPaused) {
            delay(10L)
            startTime?.let {
                timer = System.currentTimeMillis() - it
            }
        }
        if (game.isSolved) {
            timer = gameWonTime
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
            gameWonTime = timer
            ScoresManager.insertScore(sequenceLength, maxAttempts, colorsList.size, gameWonTime)
            gameOver = true
        } else if (game.isGameOver()) {
            text = "Game Over! The correct sequence was: ${game.secretCode.joinToString(", ")}\n"
            gameOver = true
        } else {
            text = "Try again. Attempts left: ${settings.maxAttempts - game.attempts}\n"
        }
        input = ""
        placeholder = "Enter your guess (space-separated colors)"
    }

    fun resetGame() {
        placeholder = "Enter your guess (space-separated colors)"
        input = ""
        gameOver = false
        guesses.clear()
        game = Game(settings)
        startTime = null
        pausedTime = null
        timer = 0L
        isPaused = false
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 30.dp, bottom = 16.dp, end = 295.dp)) {
                Text(text, fontWeight = FontWeight.Bold)
                Column {
                    Row {
                        if (!guesses.isEmpty()) {
                            Text("Guess", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Feedback", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }
                    }
                    guesses.forEach { (guess, feedback) ->
                        Row {
                            Text(guess.joinToString(", "), modifier = Modifier.weight(1f))
                            Text(feedback.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Row(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Text(
                    text = "Time: ${"%.3f".format(timer / 1000.0)} s  ",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                if (!gameOver) {
                    Button(
                        onClick = {
                            pausedTime = System.currentTimeMillis()
                            isPaused = true
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                    ) {
                        Text("||", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            resetGame()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart")
                    }
                }

                IconButton(onClick = {
                    pausedTime = System.currentTimeMillis()
                    showSettings = true
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }

                IconButton(onClick = {
                    pausedTime = System.currentTimeMillis()
                    showScores = true
                }) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = "Trophy")
                }
            }
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

            if (showSettings) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 1f))
                )
                settingsDialog(
                    sequenceLength = sequenceLength,
                    onSequenceLengthChange = { sequenceLength = it },
                    maxAttempts = maxAttempts,
                    onMaxAttemptsChange = { maxAttempts = it },
                    colorsList = colorsList,
                    onColorsListChange = { colorsList = it },
                    onDismissRequest = {
                        startTime = startTime?.plus(System.currentTimeMillis() - (pausedTime ?: 0L))
                        pausedTime = null
                        showSettings = false
                    },
                    onApply = {
                        resetGame()
                        showSettings = false
                    }
                )
            }

            if (showScores) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 1f))
                )
                scoresDialog(
                    sequenceLength = sequenceLength,
                    maxAttempts = maxAttempts,
                    colorsNumber = colorsList.size,
                    scoresManager = ScoresManager,
                    onDismissRequest = {
                        startTime = startTime?.plus(System.currentTimeMillis() - (pausedTime ?: 0L))
                        pausedTime = null
                        showScores = false
                    }
                )
            }

            if (isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 1f))
                )
                AlertDialog(
                    onDismissRequest = { isPaused = false },
                    title = { Text("Game Paused") },
                    confirmButton = {
                        Button(onClick = {
                            startTime = startTime?.plus(System.currentTimeMillis() - (pausedTime ?: 0L))
                            pausedTime = null
                            isPaused = false
                        }) {
                            Text("Play")
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            resetGame()
                            isPaused = false
                        }) {
                            Text("Restart")
                            Icon(Icons.Default.Refresh, contentDescription = "Restart")
                        }
                    }
                )
            }
        }
    }
}

fun main() = application {
    ScoresManager.connect()
    ScoresManager.createScoresTable()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Mastermind - Game",
        state = WindowState(width = 950.dp, height = 700.dp)
    ) {
        app()
    }
}
