import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlin.collections.toMutableList
import kotlin.system.exitProcess
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import androidx.compose.ui.platform.LocalClipboardManager
import java.io.IOException


val MAX_SEQ_LENGTH = 6
val MIN_SEQ_LENGTH = 3
val MAX_ATTEMPTS = 20
val MIN_ATTEMPTS = 3
val MAX_COLORS = 8
val MIN_COLORS = 3
val DEFAULT_SEQ_LENGTH = 4
val DEFAULT_ATTEMPTS = 10
val DEFAULT_COLORS_LIST = listOf("A", "B", "C", "D", "E", "F")


enum class ColorByName(val color: Color) {
    RED(Color(0xffff3333)),
    GREEN(Color(0xff00c853)),
    BLUE(Color(0xff3232cd)),
    ORANGE(Color(0xffff9800)),
    PINK(Color(0xffff80ab)),
    LIGHT_BLUE(Color(0xff00ccff)),
    BROWN(Color(0xff795548)),
    PURPLE(Color(0xffba68c8));
}

val ALL_COLORS = mapOf(
    "A" to ColorByName.RED.color,
    "B" to ColorByName.GREEN.color,
    "C" to ColorByName.BLUE.color,
    "D" to ColorByName.ORANGE.color,
    "E" to ColorByName.PINK.color,
    "F" to ColorByName.LIGHT_BLUE.color,
    "G" to ColorByName.BROWN.color,
    "H" to ColorByName.PURPLE.color
)

enum class DialogState {
    OFF,
    SHOW_SETTINGS,
    SHOW_SCORES,
    IS_PAUSED,
    SHOW_SERVER_ERROR,
    SHOW_CODE_ERROR,
    IS_LOADING,
    SHOW_MULTIPLAYER_MODES,
    SHOW_JOIN_GAME_FIELD,
    SHOW_HOST_GAME_FIELD,
    SHOW_GAME_RESULTS,
    SHOW_WAITING_FOR_RESULTS,
    SERVER_DISCONNECTED
}

//val SERVER_HOST = "localhost"
//val SERVER_PORT = 12345

val DEFAULT_SETTINGS = Settings(sequenceLength = DEFAULT_SEQ_LENGTH, maxAttempts = DEFAULT_ATTEMPTS, colorsList = DEFAULT_COLORS_LIST)



@Composable
@Preview
fun app(serverHost: String, serverPort: Int) {
    // Settings
    var sequenceLength by remember { mutableStateOf(4) }
    var maxAttempts by remember { mutableStateOf(10) }
    var colorsList by remember { mutableStateOf(listOf("A", "B", "C", "D", "E", "F")) }
    val settings = Settings(sequenceLength = sequenceLength, maxAttempts = maxAttempts, colorsList = colorsList)

    // GUI
    var text by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<DialogState>(DialogState.OFF) }
    var resetInput by remember { mutableStateOf(false) }

    // Game Logic
    var game by remember { mutableStateOf(Game(settings)) }
    var gameOver by remember { mutableStateOf(false) }
    val guesses = remember { mutableStateListOf<Pair<List<String>, Feedback>>() }
    var currentGuess by remember { mutableStateOf(List(0) { "" }) }

    // Timer
//    var startTime by remember { mutableStateOf<Long?>(null) }
//    var pausedTime by remember { mutableStateOf<Long?>(null) }
//    var timer by remember { mutableStateOf(0L) }
//    var gameWonTime by remember { mutableStateOf(0L) }

    val timeManager = remember { TimeManager() }

    // Multiplayer Mode
    var isMultiplayer by remember { mutableStateOf(false) }
    var gameCode by remember { mutableStateOf<String?>(null) }
    var joinGameCode by remember { mutableStateOf("") }
    var isHost by remember { mutableStateOf(true) }
    var client: GameClient? by remember { mutableStateOf(null) }
    var results by remember { mutableStateOf("") }
    var timeOuted by remember { mutableStateOf(false) }
    var areResultsSent by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    @Synchronized
    fun endMultiplayerGame(time: Long, isWin: Boolean) {
        if (areResultsSent) return
        areResultsSent = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (timeOuted)
                    client?.sendTimeOut()
                else
                    client?.submitResult(GameResult(isWin, game.attempts, time))
                state = DialogState.SHOW_WAITING_FOR_RESULTS
                val response = client?.receiveResults()
                withContext(Dispatchers.Main) {
                    if (response != null) {
                        results = response
                        state = DialogState.SHOW_GAME_RESULTS
                        println("Results: $results")
                    } else {
                        println("Error: Received null response from server")
                        state = DialogState.SHOW_SERVER_ERROR
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    state = DialogState.SHOW_SERVER_ERROR
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    state = DialogState.SHOW_SERVER_ERROR
                }
            }
        }
    }

    LaunchedEffect(timeManager.startTime, timeManager.pausedTime, state) {


        while (timeManager.startTime != null && !gameOver && timeManager.pausedTime == null &&
            state != DialogState.IS_PAUSED && state != DialogState.SERVER_DISCONNECTED) {

            delay(10L)
            timeManager.updateTimer()
            if ( timeManager.timer > 10000L && isMultiplayer && !timeOuted) {
                timeOuted = true
                timeManager.setGameEndedTime()
                endMultiplayerGame(timeManager.timer, false)
                gameOver = true
            }
        }
        if (game.isGameOver()) {
            timeManager.setTimer()
        }
    }



    fun closeSession() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                client?.let {
                    it.output.close()
                    it.input.close()
                    it.socket.close()
                }
                client = null
            } catch (e: IOException) {
                println("IOException: ${e.message}")
            } catch (e: Exception) {
                println("Exception: ${e.message}")
            }
        }
    }

    fun submitGuess() {


        if (timeManager.startTime == null) {
            timeManager.startTimer()
        }

        var guess = currentGuess.toMutableList()
        val feedback = game.checkGuess(guess)
        guesses.add(guess to feedback)
        if (game.isSolved) {
            text = "Congratulations! You've guessed the sequence in ${game.attempts} attempts.\n"
            timeManager.setGameEndedTime()
            if (isMultiplayer) endMultiplayerGame(timeManager.gameEndedTime, true)
            CoroutineScope(Dispatchers.IO).launch {
                ScoresManager.insertScore(sequenceLength, maxAttempts, colorsList.size, timeManager.gameEndedTime)
            }
            gameOver = true
        } else if (game.isGameOver()) {
            text = "Game Over! \n"
            timeManager.setGameEndedTime()
            if (isMultiplayer) endMultiplayerGame(timeManager.gameEndedTime, false)
            gameOver = true
        } else {
            text = "Try again. Attempts left: ${settings.maxAttempts - game.attempts}\n"
        }
    }

    fun resetGame() {
        areResultsSent = false
        timeOuted = false
        text = ""
        gameOver = false
        guesses.clear()
        game = Game(settings)
        timeManager.reset()
        resetInput = !resetInput
        isMultiplayer = false
        isHost = false
        state = DialogState.OFF
    }

    fun resetSingleplayerGame() {
        areResultsSent = false
        timeOuted = false
        text = ""
        gameOver = false
        guesses.clear()
        game = Game(settings)
        timeManager.reset()
        resetInput = !resetInput
        state = DialogState.SHOW_MULTIPLAYER_MODES
    }

    fun printbeforeresetgame() {
        println(areResultsSent)
        println(timeOuted)
        println(text)
        println(gameOver)
        println(guesses)
        println(game)
        println(timeManager)
        println(resetInput)
        println(isMultiplayer)
        println(isHost)
        println(state)
    }

    fun resetToStartMultiplayer(secret : List<String>?) {
        areResultsSent = false
        timeOuted = false
        text = ""
        gameOver = false
        guesses.clear()
        game = Game(DEFAULT_SETTINGS, secret)
        sequenceLength = DEFAULT_SETTINGS.sequenceLength
        maxAttempts = DEFAULT_SETTINGS.maxAttempts
        colorsList = DEFAULT_SETTINGS.colorsList
        timeManager.resetAndStart()
        resetInput = !resetInput
        state = DialogState.OFF
    }

    fun startMultiplayerGameAsHost() {
        state = DialogState.IS_LOADING
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("Attempting to connect to the server...")

                client = GameClient(
                    serverHost,
                    serverPort,
                    onDisconnection = { if (isMultiplayer )state = DialogState.SERVER_DISCONNECTED },
                    onError = { state = DialogState.SHOW_CODE_ERROR }
                )
                withTimeout(5000L) {
                    client?.connect()
                }

                val response = client?.createGame()
                withContext(Dispatchers.Main) {
                    if (response != null) {
                        gameCode = response
                        println("Game code received: $gameCode")

                        state = DialogState.SHOW_HOST_GAME_FIELD
                        withContext(Dispatchers.IO) {
                            try {
                                val secretStr = client?.receiveSecretCode()
                                resetToStartMultiplayer(secretStr?.split(" ") ?: listOf())
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    state = DialogState.SHOW_SERVER_ERROR
                                }
                            }
                        }

                    } else {
                        state = DialogState.SHOW_SERVER_ERROR
                        isMultiplayer = false
                        println("Error: Response is null")

                    }
                    state = DialogState.OFF
                }
            } catch (e: TimeoutCancellationException) {
                withContext(Dispatchers.Main) {
                    state = DialogState.SHOW_SERVER_ERROR
                    isMultiplayer = false
                    println("Error: Connection timed out")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    state = DialogState.SHOW_SERVER_ERROR
                    isMultiplayer = false
                }
            }
        }


    }

    fun joinMultiplayerGame() {
        state = DialogState.IS_LOADING
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("Attempting to connect to the server...")

                client = GameClient(
                    serverHost,
                    serverPort,
                    onDisconnection = { if (isMultiplayer )state = DialogState.SERVER_DISCONNECTED },
                    onError = { state = DialogState.SHOW_CODE_ERROR }
                )
                withTimeout(5000L) {
                    client?.connect()
                }

                val success = client?.joinGame(joinGameCode) == true
                withContext(Dispatchers.Main) {
                    if (success) {
                        println("Successfully joined the game with code: $joinGameCode")
                        val secretStr = client?.receiveSecretCode()
                        resetToStartMultiplayer(secretStr?.split(" ") ?: listOf())

                    }
                }
            } catch (e: TimeoutCancellationException) {
                withContext(Dispatchers.Main) {
                    state = DialogState.SHOW_SERVER_ERROR
                    println("Error: Connection timed out")
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    state = DialogState.SHOW_SERVER_ERROR
                    println("IOException: ${e.message}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    state = DialogState.SHOW_SERVER_ERROR
                    println("Exception: ${e.message}")
                }
            }
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 30.dp, bottom = 16.dp, end = 295.dp)) {
                Text(text, fontWeight = FontWeight.Bold)
                PreviousGuesses(guesses = guesses)
                if (gameOver) {
                    Text("The secret code was: \n", fontWeight = FontWeight.Bold)
                    DisplayColors(game.secretCode)
                }
            }
            Row(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Text(
                    text = "Time: ${"%.3f".format(timeManager.timer / 1000.0)} s  ",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                if (!isMultiplayer) {

                    if (!gameOver) {
                        Button(
                            onClick = {
                                timeManager.pauseTimer()
                                state = DialogState.IS_PAUSED
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
                        ) { Icon(Icons.Default.Refresh, contentDescription = "Restart") }
                    }

                    IconButton(onClick = {
                        timeManager.pauseTimer()
                        state = DialogState.SHOW_SETTINGS
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }

                    IconButton(onClick = {
                        timeManager.pauseTimer()
                        state = DialogState.SHOW_SCORES
                    }) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = "Trophy")
                    }
                }

                IconButton(
                    onClick = {
                        isMultiplayer = !isMultiplayer
                        if (isMultiplayer) {
                            resetSingleplayerGame()
                        }
                        else if (client != null) {
                            closeSession()
                            resetGame()
                        }
                    }
                ) {
                    if (isMultiplayer) {
                        Icon(Icons.Default.Person, contentDescription = "Single Player")
                    } else {
                        Icon(Icons.Default.Group, contentDescription = "Multiplayer")
                    }
                }


            }
            if (gameOver) {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (isMultiplayer) closeSession()
                            resetGame()
                        }
                    ) {
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
                    GuessInput(
                        colorsList = settings.colorsList,
                        onSubmitGuess = { guess ->
                            currentGuess = guess
                            submitGuess()
                        }, guessSize = settings.sequenceLength, reset = resetInput
                    )
                }
            }


            // Dialogs
            when (state) {
                DialogState.SHOW_SETTINGS -> {
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
                            timeManager.resumeTimer()
                            state = DialogState.OFF
                        },
                        onApply = {
                            resetGame()
                        }
                    )
                }

                DialogState.SHOW_SCORES -> {
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
                            timeManager.resumeTimer()
                            state = DialogState.OFF
                        }
                    )
                }

                DialogState.IS_PAUSED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 1f))
                    )
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Game Paused") },
                        confirmButton = {
                            Button(onClick = {
                                timeManager.resumeTimer()
                                state = DialogState.OFF
                            }) {
                                Text("Play")
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                resetGame()
                                state = DialogState.OFF
                            }) {
                                Text("Restart")
                                Icon(Icons.Default.Refresh, contentDescription = "Restart")
                            }
                        }
                    )
                }

                DialogState.SHOW_SERVER_ERROR -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Error") },
                        text = { Text("Server is unavailable. Please try again later.") },
                        confirmButton = {
                            Button(onClick = {
                                state = DialogState.OFF
                                isMultiplayer = false
                            }) { Text("OK") }
                        }
                    )
                }

                DialogState.SHOW_CODE_ERROR -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Error") },
                        text = { Text("Invalid code or game full.") },
                        confirmButton = {
                            Button(onClick = {
                                state = DialogState.OFF
                                isMultiplayer = false
                            }) { Text("OK") }
                        }
                    )
                }

                DialogState.SHOW_MULTIPLAYER_MODES -> {
                    AlertDialog(
                        onDismissRequest = {
                            state = DialogState.OFF
                            isMultiplayer = false
                        },
                        title = { Text("Multiplayer Mode") },
                        text = { Text("") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    isHost = true
                                    startMultiplayerGameAsHost()
                                }
                            ) { Text("Host Game") }
                            Button(
                                onClick = {
                                    state = DialogState.SHOW_JOIN_GAME_FIELD
                                    isHost = false
                                }
                            ) { Text("Join Game") }
                        }
                    )
                }

                DialogState.SHOW_HOST_GAME_FIELD -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) { Text("Game Code\n") }
                        },
                        text = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$gameCode",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.h4,
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                clipboardManager.setText(AnnotatedString(gameCode ?: ""))
                            }) { Text("Copy code") }
                            Button(onClick = {
                                //client?.handleDisconnection()
                                resetGame()
                                closeSession()
                            }) { Text("Exit") }
                        }
                    )

                }

                DialogState.SHOW_JOIN_GAME_FIELD -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Join Game") },
                        text = {
                            TextField(
                                value = joinGameCode,
                                onValueChange = { joinGameCode = it },
                                label = { Text("Enter game code") }
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    state = DialogState.OFF
                                    joinMultiplayerGame()
                                }
                            ) { Text("Join") }
                            Button(onClick = { resetGame()
                                joinGameCode = ""} )
                            { Text("Exit") }
                        }
                    )
                }

                DialogState.IS_LOADING -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Loading") },
                        text = {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        },
                        buttons = {}
                    )
                }

                DialogState.SHOW_WAITING_FOR_RESULTS -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Waiting for other player") },
                        text = {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        },
                        buttons = {}
                    )
                }

                DialogState.SHOW_GAME_RESULTS -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Game Results") },
                        text = {
                            Text(results)
                        },
                        confirmButton = {
                            Button(onClick = { state = DialogState.OFF }) { Text("OK") }
                        }
                    )
                }

                DialogState.SERVER_DISCONNECTED -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Disconnected") },
                        text = {
                            Text("The server or the other player has disconnected.")
                        },
                        confirmButton = {
                            Button(onClick = {
                                state = DialogState.OFF
                                resetGame()
                            }) { Text("OK") }
                        }
                    )
                }

                DialogState.OFF -> {}
            }
        }
    }
}

@Composable
fun PreviousGuesses(guesses: List<Pair<List<String>, Feedback>>) {
    Column {
        guesses.forEach { (guess, feedback) ->
            Column {
                Row {
                    DisplayColors(guess)
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(feedback.toString())
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun DisplayColors(guess: List<String>) {
    Row {
        guess.forEach { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ALL_COLORS[color] ?: Color.White)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}


fun main(args: Array<String>) = application {

    val serverHost = if (args.isNotEmpty()) args[0] else "localhost"
    val serverPort = if (args.size > 1) args[1].toIntOrNull() else 12345
    if (serverPort == null) {
        println("Invalid port number.")
    }
    else {
        ScoresManager.connect()
        ScoresManager.createScoresTable()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Mastermind - Game",
            state = WindowState(width = 950.dp, height = 900.dp)
        ) {
            app(serverHost, serverPort)
        }
    }

}
