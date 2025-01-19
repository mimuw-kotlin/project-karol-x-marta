import java.io.EOFException
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.get
import kotlin.concurrent.thread

data class GameResult(val isWin: Boolean, val attempts: Int, val time: Long, val isTimeOut: Boolean = false): java.io.Serializable

class GameServer(private val port: Int) {
    private val serverSocket = ServerSocket(port)
    private val games = ConcurrentHashMap<String, GameSession>()
    private val clients = ConcurrentHashMap<Socket, String>()

    fun start() {
        println("Server started on port $port")
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
                        val gameCode = generateGameCode()
                        val gameSession = GameSession()
                        games[gameCode] = gameSession
                        gameSession.addPlayer(clientSocket, output)
                        clients[clientSocket] = gameCode
                        output.writeObject(mapOf("status" to "gameCode", "gameCode" to gameCode))
                    }
                    "joinGame" -> {
                        val gameCode = request["gameCode"] as? String
                        if (gameCode != null) {
                            println("Joining game $gameCode")
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
                                output.writeObject(mapOf("status" to "error", "message" to "Invalid game code or game full"))
                            }
                        } else {
                            println("Joining game failed: game code is null")
                            output.writeObject(mapOf("status" to "error", "message" to "Game code is null"))
                        }
                    }
                    "submitResult" -> synchronized(this) {
                        val gameCode = clients[clientSocket]
                        if (gameCode != null) {
                            val result = request["result"] as GameResult
                            // jesli jest timeout to na ostatnim polu jest true
                            val gameSession = games[gameCode]
                            gameSession?.submitResult(result)
                            if (gameSession?.isComplete() == true) {
                                val results = gameSession.getResults()
                                gameSession.notifyPlayers(results)
                                games.remove(gameCode)
                                disconnectClient(gameSession.players[0])
                                disconnectClient(gameSession.players[1])
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
        }
        catch (e: EOFException) {
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
                games.remove(gameCode)

                it.players.forEach { player ->
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
        return (1000..9999).random().toString()
    }
}

class GameSession() {
    internal val players = mutableListOf<Socket>()
    private val outputs = mutableListOf<ObjectOutputStream>()
    private val results = mutableListOf<GameResult>()

    fun addPlayer(player: Socket, output: ObjectOutputStream): Boolean {
        if (players.size < 2) {
            players.add(player)
            outputs.add(output)
            return true
        }
        return false
    }

    fun isReadyToStart(): Boolean {
        return players.size == 2
    }

    fun startGame() {
        val secretCode = List(DEFAULT_SETTINGS.sequenceLength) { DEFAULT_SETTINGS.colorsList.random() }.joinToString(" ")
        outputs.forEach { output ->
            println("Sending secret code to player")
            output.writeObject(mapOf("status" to "secretCode", "secretCode" to secretCode))
        }
    }

    fun submitResult(result: GameResult) {
        results.add(result)
    }

    fun isComplete(): Boolean {
        return results.size == 2
    }

    fun getResults(): List<GameResult> {
        return results
    }

    fun notifyPlayers(results: List<GameResult>) {
        var player1Msg = ""
        var player2Msg = ""
        val player1Results = if (results[0].isTimeOut) {
            "Time out\n"
        } else {
            (if (results[0].isWin) "Success, " else "Failure, ") +
                "Attempts: ${results[0].attempts}, Time: ${"%.3f".format(results[0].time / 1000.0)} seconds\n"
        }
        val player2Results = if (results[1].isTimeOut) {
            "Time out\n"
        } else {
            (if (results[1].isWin) "Success, " else "Failure, ") +
                "Attempts: ${results[1].attempts}, Time: ${"%.3f".format(results[1].time / 1000.0)} seconds\n"
        }


        if (results[0].isWin && results[1].isWin) {
            if (results[0].time < results[1].time) {
                player1Msg = "You win!\n\nYour score\n$player1Results\nOponent score\n$player2Results"
                player2Msg = "You lose\n\nYour score\n$player2Results\nOponent score\n$player1Results"
            }
        } else if (results[0].isWin) {
            player1Msg = "You win!\n\nYour score\n$player1Results\nOponent score\n$player2Results"
            player2Msg = "You lose!\n\nYour score\n$player2Results\nOponent score\n$player1Results"
        } else if (results[1].isWin) {
            player1Msg = "You lose\nYour score\n$player1Results\nOponent score\n$player2Results"
            player2Msg = "You win!\n\nYour score\n$player2Results\nOponent score\n$player1Results"
        } else {
            player1Msg = "Noone wins\n\nYour score\n$player1Results\nOponent score\n$player2Results"
            player2Msg = "Noone wins\n\nYour score\n$player2Results\nOponent score\n$player1Results"
        }
        outputs[0].writeObject(mapOf("status" to "results", "results" to player1Msg))
        outputs[1].writeObject(mapOf("status" to "results", "results" to player2Msg))
    }

    fun closeSession() {
        players.forEach { player ->
            try {
                player.close()
            } catch (e: IOException) {
                println("IOException while closing player socket: ${e.message}")
            }
        }
        outputs.forEach { output ->
            try {
                output.close()
            } catch (e: IOException) {
                println("IOException while closing output stream: ${e.message}")
            }
        }
    }

}

fun main() {
    val server = GameServer(12345)
    server.start()
}