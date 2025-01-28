import java.io.EOFException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class GameClient(
    private val host: String,
    private val port: Int,
    private val onDisconnection: () -> Unit,
    private val onError: () -> Unit,
) {
    internal lateinit var socket: Socket
    internal lateinit var input: ObjectInputStream
    internal lateinit var output: ObjectOutputStream

    private var gameCode: String? = null
    private var listeningThread: Thread? = null
    private var secretCode: String? = null
    private var results: String? = null
    private var joinGameResponse: Boolean? = null

    fun connect() {
        try {
            println("Creating socket...")
            socket = Socket(host, port)
            println("Socket created successfully.")
            output = ObjectOutputStream(socket.getOutputStream())
            input = ObjectInputStream(socket.getInputStream())
            println("Connected to the server.")
            startListening()
        } catch (e: IOException) {
            println("IOException: ${e.message}")
            throw e
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            throw e
        }
    }

    private fun startListening() {
        listeningThread =
            Thread {
                listenForMessages()
            }
        listeningThread?.start()
    }

    private fun listenForMessages() {
        try {
            while (!socket.isClosed && socket.isConnected) {
                val response = input.readObject() as Map<*, *>
                println("Received response: $response")
                synchronized(this) {
                    when (response["status"]) {
                        "opponentDisconnected" -> handleDisconnection()
                        "secretCode" -> handleSecretCode(response)
                        "results" -> handleResults(response)
                        "gameCode" -> handleGameCode(response)
                        "joined" -> handleJoined()
                        "error" -> handleError()
                        else -> {
                            println("Unknown status: ${response["status"]}")
                            handleDisconnection()
                        }
                    }
                }
            }
        } catch (e: EOFException) {
            if (results == null) {
                handleDisconnection()
            }
            // else: we have the results so we don't need server anymore and disconnection is ok
        } catch (e: ClassCastException) {
            println("ClassCastException in listening thread: ${e.message}")
            handleDisconnection()
        } catch (e: IOException) {
            println("IOException in listening thread: ${e.message}")
            handleDisconnection()
        } catch (e: Exception) {
            println("Exception in listening thread: ${e.message}")
            handleDisconnection()
        }
    }

    fun createGame(): String? {
        synchronized(this) {
            try {
                println("Creating game on server...")
                output.writeObject(mapOf("action" to "createGame"))
                while (gameCode == null) {
                    (this as Object).wait()
                }
                val code = gameCode
                return code
            } catch (e: IOException) {
                println("IOException: ${e.message}")
                return null
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                return null
            }
        }
    }

    fun joinGame(code: String): Boolean {
        synchronized(this) {
            try {
                println("Sending join game request to the server...")
                output.writeObject(mapOf("action" to "joinGame", "gameCode" to code))
                while (joinGameResponse == null) {
                    (this as Object).wait()
                }
                val response = joinGameResponse
                joinGameResponse = null
                return response == true
            } catch (e: IOException) {
                println("IOException: ${e.message}")
                return false
            } catch (e: Exception) {
                println("Exception: ${e.message}")
                return false
            }
        }
    }

    fun submitResult(result: GameResult) {
        try {
            output.writeObject(mapOf("action" to "submitResult", "result" to result))
        } catch (e: IOException) {
            println("IOException: ${e.message}")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
        }
    }

    fun sendTimeOut() {
        println("Sending timeout to the server.")
        val result = GameResult(false, 0, 0, true)
        submitResult(result)
    }

    @Synchronized
    private fun handleJoined() {
        joinGameResponse = true
        println("Player joined the game")
        (this as Object).notifyAll()
    }

    @Synchronized
    private fun handleGameCode(response: Map<*, *>) {
        gameCode = response["gameCode"] as? String
        println("Game code received: $gameCode")
        (this as Object).notifyAll()
    }

    @Synchronized
    private fun handleSecretCode(response: Map<*, *>) {
        secretCode = response["secretCode"] as? String
        println("Received secret code.")
        (this as Object).notifyAll()
    }

    @Synchronized
    private fun handleResults(response: Map<*, *>) {
        results = response["results"] as? String
        println("Received results.")
        (this as Object).notifyAll()
    }

    @Synchronized
    fun receiveSecretCode(): String {
        while (secretCode == null) {
            (this as Object).wait()
        }
        val code = secretCode!!
        secretCode = null
        return code
    }

    @Synchronized
    fun receiveResults(): String {
        while (results == null) {
            (this as Object).wait()
        }
        val res = results!!
        return res
    }

    @Synchronized
    fun handleDisconnection() {
        println("Disconnected from the server.")
        onDisconnection()
    }

    @Synchronized
    fun handleError() {
        println("Game full or doesn't exist.")
        onError()
    }
}
