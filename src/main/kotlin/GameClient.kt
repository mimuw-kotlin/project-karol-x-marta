import java.io.IOException
import java.net.Socket
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class GameClient(
    private val host: String,
    private val port: Int,
    private val onDisconnection: () -> Unit
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
        listeningThread = Thread {
            listenForMessages()
        }
        listeningThread?.start()
    }

    private fun listenForMessages() {
        try {
            while (!socket.isClosed && socket.isConnected) {
                val response = input.readObject() as Map<*, *>
                println("Received response: $response")
                when (response["status"]) {
                    "opponentDisconnected" -> handleDisconnection()
                    "secretCode" -> handleSecretCode(response)
                    "results" -> handleResults(response)
                    "gameCode" -> handleGameCode(response)
                    "joined" -> handleJoined()
                    else -> println("Unknown status: ${response["status"]}")
                }
            }
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
                println("Sending game settings to the server...")
                output.writeObject(mapOf("action" to "createGame")) // Send settings to the server
                while (gameCode == null) {
                    (this as java.lang.Object).wait()
                }
                val code = gameCode
                //gameCode = null
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
                    (this as java.lang.Object).wait()
                }
                val response = joinGameResponse
                joinGameResponse = null
                return response ?: false
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

    @Synchronized
    private fun handleJoined() {
        joinGameResponse = true
        println("Player joined the game")
        (this as java.lang.Object).notifyAll()
    }

    @Synchronized
    private fun handleGameCode(response: Map<*, *>) {
        gameCode = response["gameCode"] as? String
        println("Game code received: $gameCode")
        (this as java.lang.Object).notifyAll()
    }

    @Synchronized
    private fun handleSecretCode(response: Map<*, *>) {
        secretCode = response["secretCode"] as? String
        println("Secret code received: $secretCode")
        (this as java.lang.Object).notifyAll()
    }

    @Synchronized
    private fun handleResults(response: Map<*, *>) {
        results = response["results"] as? String
        println("Results received: $results")
        (this as java.lang.Object).notifyAll()
    }

    @Synchronized
    fun receiveSecretCode(): String {
        while (secretCode == null) {
            (this as java.lang.Object).wait()
        }
        val code = secretCode!!
        secretCode = null
        return code
    }

    @Synchronized
    fun receiveResults(): String {
        while (results == null) {
            (this as java.lang.Object).wait()
        }
        val res = results!!
        results = null
        return res
    }


    fun handleDisconnection() {
        println("Disconnected from server.")
        onDisconnection()
    }
}