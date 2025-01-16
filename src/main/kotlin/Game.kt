class Game(private val settings: Settings, val secret: List<String>? = null) {
    var secretCode: List<String> = secret ?:
        List(settings.sequenceLength) { settings.colorsList.random() }
    val checker = Checker(secretCode)
    val player = Player(settings.sequenceLength, settings.colorsList)
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

    fun start() {
        println("Welcome to Mastermind!")
        println(
            "The secret code has been generated. It consists " +
                "of ${settings.sequenceLength} elements, " +
                "each in one of colors: ${settings.colorsList}. " +
                "You have ${settings.maxAttempts} attempts to guess it. " +
                "Good luck!"
        )

        val startTime = System.currentTimeMillis()

        while (!isGameOver()) {
            val guess = player.makeGuess()
            val feedback = checkGuess(guess)
            println("Feedback: \n$feedback")
        }

        val elapsedTime = System.currentTimeMillis() - startTime

        if (isSolved) {
            println("Congratulations! You've guessed the code.")
        } else {
            println("Game over! The correct code was $secretCode")
        }
        println("Time score: ${"%.3f".format(elapsedTime / 1000.0)} seconds")
    }
}
