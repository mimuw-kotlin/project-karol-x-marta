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
    private var nextCodes = (1000..9999).shuffled().toMutableList()

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
                        synchronized (this) {
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
                    }
                    "submitResult" -> {
                        synchronized (this) {
                            val gameCode = clients[clientSocket]
                            if (gameCode != null) {
                                val result = request["result"] as GameResult
                                // jesli jest timeout to na ostatnim polu jest true
                                val gameSession = games[gameCode]
                                gameSession?.submitResult(clientSocket, result)
                                if (gameSession?.isComplete() == true) {
                                    val results = gameSession.getResults()
                                    gameSession.notifyPlayers(results)
//                                    games.remove(gameCode)
//                                    nextCodes.add(gameCode.toInt())
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

//    private fun generateGameCode(): String {
//        return (1000..9999).random().toString()
//    }

    private fun generateGameCode(): String {
        val code = nextCodes.first().toString()
        nextCodes = nextCodes.drop(1).toMutableList()
        return code
    }
}

class GameSession() {
    internal val players = mutableMapOf<Socket, ObjectOutputStream>()
    private val results = mutableMapOf<Socket, GameResult>()

    fun addPlayer(player: Socket, output: ObjectOutputStream): Boolean {
        if (players.size < 2) {
            players.put(player, output)
            return true
        }
        return false
    }

    fun isReadyToStart(): Boolean {
        return players.size == 2
    }

    fun startGame() {
        val secretCode = List(DEFAULT_SETTINGS.sequenceLength) { DEFAULT_SETTINGS.colorsList.random() }.joinToString(" ")
        players.values.forEach { output ->
            println("Sending secret code to player")
            output.writeObject(mapOf("status" to "secretCode", "secretCode" to secretCode))
        }
    }

    fun submitResult(socket: Socket, result: GameResult) {
        results.put(socket, result)
    }

    fun isComplete(): Boolean {
        return results.size == 2
    }

    fun getResults(): Map<Socket, GameResult> {
        return results
    }

    fun notifyPlayers(results: Map<Socket, GameResult>) {
        val sockets = results.keys
        val result1 = results[sockets.elementAt(0)] ?: return
        val result2 = results[sockets.elementAt(1)] ?: return
        var player1Msg = ""
        var player2Msg = ""
        val player1Results = if (result1.isTimeOut) {
            "Time out\n"
        } else {
            (if (result1.isWin) "Success, " else "Failure, ") +
                "Attempts: ${result1.attempts}, Time: ${"%.3f".format(result1.time / 1000.0)} seconds\n"
        }
        val player2Results = if (result2.isTimeOut) {
            "Time out\n"
        } else {
            (if (result2.isWin) "Success, " else "Failure, ") +
                "Attempts: ${result2.attempts}, Time: ${"%.3f".format(result2.time / 1000.0)} seconds\n"
        }


        if (result1.isWin && result2.isWin) {
            if (result1.time < result2.time) {
                player1Msg = "You win!\n\nYour score\n$player1Results\nOponent score\n$player2Results"
                player2Msg = "You lose\n\nYour score\n$player2Results\nOponent score\n$player1Results"
            }
        } else if (result1.isWin) {
            player1Msg = "You win!\n\nYour score\n$player1Results\nOponent score\n$player2Results"
            player2Msg = "You lose!\n\nYour score\n$player2Results\nOponent score\n$player1Results"
        } else if (result2.isWin) {
            player1Msg = "You lose\nYour score\n$player1Results\nOponent score\n$player2Results"
            player2Msg = "You win!\n\nYour score\n$player2Results\nOponent score\n$player1Results"
        } else {
            player1Msg = "Noone wins\n\nYour score\n$player1Results\nOponent score\n$player2Results"
            player2Msg = "Noone wins\n\nYour score\n$player2Results\nOponent score\n$player1Results"
        }
        players[sockets.elementAt(0)]?.writeObject(mapOf("status" to "results", "results" to player1Msg))
        players[sockets.elementAt(1)]?.writeObject(mapOf("status" to "results", "results" to player2Msg))
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

fun main() {
    val server = GameServer(12345)
    server.start()
}