import java.io.EOFException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.get
import kotlin.concurrent.thread

data class GameResult(
    val isWin: Boolean,
    val attempts: Int,
    val time: Long,
    val isTimeOut: Boolean = false,
) : java.io.Serializable

class GameServer(
    private val port: Int,
) {
    private var serverSocket: ServerSocket
    private val games = ConcurrentHashMap<String, GameSession>()
    private val clients = ConcurrentHashMap<Socket, String>()
    private var nextCodes = (1000..9999).shuffled().toMutableList()

    init {
        try {
            serverSocket = ServerSocket(port)
            println("Server started on port $port")
        } catch (e: IOException) {
            println("Failed to start server on port $port: ${e.message}")
            throw e
        }
    }

    fun start() {
        while (true) {
            val clientSocket = serverSocket.accept()
            thread { handleClient(clientSocket) }
        }
    }

    private fun handleClient(clientSocket: Socket) {
        val input = ObjectInputStream(clientSocket.getInputStream())
        val output = ObjectOutputStream(clientSocket.getOutputStream())

        try {
            while (true) {
                val request = input.readObject() as Map<*, *>
                when (request["action"]) {
                    "createGame" -> {
                        synchronized(this) {
                            val gameCode = generateGameCode()
                            val gameSession = GameSession()
                            games[gameCode] = gameSession
                            gameSession.addPlayer(clientSocket, output)
                            clients[clientSocket] = gameCode
                            output.writeObject(mapOf("status" to "gameCode", "gameCode" to gameCode))
                        }
                    }
                    "joinGame" -> {
                        synchronized(this) {
                            val gameCode = request["gameCode"] as? String
                            if (gameCode != null) {
                                println("Joining game with code: $gameCode")
                                val gameSession = games[gameCode]
                                if (gameSession != null && gameSession.addPlayer(clientSocket, output)) {
                                    clients[clientSocket] = gameCode
                                    output.writeObject(mapOf("status" to "joined"))
                                    println("Player joined game $gameCode")
                                    if (gameSession.isReadyToStart()) {
                                        println("Game started")
                                        gameSession.startGame()
                                    }
                                } else {
                                    println("Joining game failed: invalid game code or game full")
                                    output.writeObject(mapOf("status" to "error", "message" to "Invalid game code or game full"))
                                }
                            } else {
                                println("Joining game failed: game code is null")
                                output.writeObject(mapOf("status" to "error", "message" to "Game code is null"))
                            }
                        }
                    }
                    "submitResult" -> {
                        synchronized(this) {
                            val gameCode = clients[clientSocket]
                            if (gameCode != null) {
                                val result = request["result"] as GameResult
                                val gameSession = games[gameCode]
                                gameSession?.submitResult(clientSocket, result)
                                if (gameSession?.isComplete() == true) {
                                    val results = gameSession.getResults()
                                    gameSession.notifyPlayers(results)
                                    disconnectClient(gameSession.players.keys.elementAt(0))
                                    disconnectClient(gameSession.players.keys.elementAt(1))
                                }
                            }
                        }
                    }
                    else -> {
                        println("Unknown action: ${request["action"]}")
                        disconnectClient(clientSocket)
                    }
                }
            }
        } catch (e: ClassCastException) {
            println("ClassCastException: ${e.message}")
        } catch (e: EOFException) {
            println("Client disconnected: ${clientSocket.inetAddress.hostAddress}")
        } catch (e: SocketException) {
            println("SocketException: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            disconnectClient(clientSocket)
        }
    }

    private fun disconnectClient(clientSocket: Socket) {
        val gameCode = clients.remove(clientSocket)
        if (gameCode != null) {
            val gameSession = games[gameCode]
            gameSession?.let {
                it.closeSession()
                val removedGame = games.remove(gameCode)
                if (removedGame != null) {
                    nextCodes.add(gameCode.toInt())
                }
                it.players.keys.forEach { player ->
                    if (player != clientSocket) {
                        try {
                            val output = ObjectOutputStream(player.getOutputStream())
                            output.writeObject(mapOf("status" to "opponentDisconnected"))
                            player.close()
                        } catch (e: IOException) {
                            println("IOException while closing player socket: ${e.message}")
                        }
                    }
                }
            }
        }
        try {
            clientSocket.close()
        } catch (e: IOException) {
            println("IOException while closing client socket: ${e.message}")
        }
    }

    private fun generateGameCode(): String {
        val code = nextCodes.first().toString()
        nextCodes = nextCodes.drop(1).toMutableList()
        return code
    }
}

class GameSession {
    internal val players = mutableMapOf<Socket, ObjectOutputStream>()
    private val results = mutableMapOf<Socket, GameResult>()

    fun addPlayer(
        player: Socket,
        output: ObjectOutputStream,
    ): Boolean {
        if (players.size < 2) {
            players.put(player, output)
            return true
        }
        return false
    }

    fun isReadyToStart(): Boolean = players.size == 2

    fun startGame() {
        val secretCode = List(DEFAULT_SETTINGS.sequenceLength) { DEFAULT_SETTINGS.colorsList.random() }.joinToString(" ")
        players.values.forEach { output ->
            println("Sending secret code to player")
            output.writeObject(mapOf("status" to "secretCode", "secretCode" to secretCode))
        }
    }

    fun submitResult(
        socket: Socket,
        result: GameResult,
    ) {
        results.put(socket, result)
    }

    fun isComplete(): Boolean = results.size == 2

    fun getResults(): Map<Socket, GameResult> = results

    fun generateResultMessage(result: GameResult): String =
        if (result.isTimeOut) {
            "Time out\n"
        } else {
            (if (result.isWin) "Success, " else "Failure, ") +
                "Attempts: ${result.attempts}, Time: ${"%.3f".format(result.time / 1000.0)} seconds\n"
        }

    fun notifyPlayers(results: Map<Socket, GameResult>) {
        val sockets = results.keys
        val result1 = results[sockets.elementAt(0)] ?: return
        val result2 = results[sockets.elementAt(1)] ?: return

        val player1Results = generateResultMessage(result1)
        val player2Results = generateResultMessage(result2)
        var player1Info = "\nYour score\n$player1Results\nOponent score\n$player2Results"
        var player2Info = "\nYour score\n$player2Results\nOponent score\n$player1Results"

        val (player1Message, player2Message) =
            when {
                result1.isWin && result2.isWin ->
                    when {
                        result1.time < result2.time -> "You win!\n$player1Info" to "You lose\n$player2Info"
                        result2.time < result1.time -> "You lose\n$player1Info" to "You win!\n$player2Info"
                        else -> "Draw\n$player1Info" to "Draw\n$player2Info"
                    }
                result1.isWin -> "You win!\n$player1Info" to "You lose\n$player2Info"
                result2.isWin -> "You lose\n$player1Info" to "You win!\n$player2Info"
                else -> "Noone wins\n$player1Info" to "Noone wins\n$player2Info"
            }

        players[sockets.elementAt(0)]?.writeObject(mapOf("status" to "results", "results" to player1Message))
        players[sockets.elementAt(1)]?.writeObject(mapOf("status" to "results", "results" to player2Message))
    }

    fun closeSession() {
        players.keys.forEach { player ->
            try {
                player.close()
            } catch (e: IOException) {
                println("IOException while closing player socket: ${e.message}")
            }
        }
        players.values.forEach { output ->
            try {
                output.close()
            } catch (e: IOException) {
                println("IOException while closing output stream: ${e.message}")
            }
        }
    }
}

fun main(args: Array<String>) {
    try {
        val port = if (args.isNotEmpty()) args[0].toInt() else 12345
        val server = GameServer(port)
        server.start()
    } catch (e: NumberFormatException) {
        println("Port must be a number.")
    } catch (e: IOException) {
    }
}
