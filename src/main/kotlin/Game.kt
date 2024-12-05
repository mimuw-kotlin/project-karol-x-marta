// src/main/kotlin/Game.kt
class Game(private val settings: Settings) {
    private val secretCode: List<String> =
        List(settings.sequenceLength) { settings.colorsList.random() }
    val checker = Checker(secretCode)
    val player = Player()
    var attempts = 0
    var isSolved = false

    fun checkGuess(guess: List<String>): Feedback {
        val feedback = checker.checkGuess(guess)
        isSolved = feedback.correct == settings.sequenceLength
        attempts++
        return feedback
    }

    fun isGameOver(): Boolean {
        return attempts >= settings.maxAttempts || isSolved
    }

    fun getSecretCode(): List<String> {
        return secretCode
    }

    fun start() {
        println("Welcome to Mastermind!")
        println("The secret code has been generated. It consists " +
                "of ${settings.sequenceLength} elements, " +
                "each in one of colors: ${settings.colorsList}. " +
                "You have ${settings.maxAttempts} attempts to guess it. " +
                "Good luck!")

        while (!isGameOver()) {
            val guess = player.makeGuess()
            val feedback = checkGuess(guess)
            println("Feedback: \n$feedback")
        }

        if (isSolved) {
            println("Congratulations! You've guessed the code.")
        } else {
            println("Game over! The correct code was $secretCode")
        }
    }
}