class Game(private val settings: Settings) {
    private val secretCode: List<String> =
        List(settings.sequenceLength) { settings.colorsList.random() }
    private val checker = Checker(secretCode)

    fun start() {
        println("Welcome to Mastermind!")
        println("The secret code has been generated. It consists " +
                "of ${settings.sequenceLength} elements, " +
                "each in one of colors: ${settings.colorsList}. " +
                "You have ${settings.maxAttempts} attempts to guess it. " +
                "Good luck!")
        val player = Player()
        var attempts = 0
        var isSolved = false

        while (attempts < settings.maxAttempts && !isSolved) {
            val guess = player.makeGuess()
            val feedback = checker.checkGuess(guess)
            println("Feedback: \n$feedback")
            isSolved = feedback.correct == 4
            attempts++
        }

        if (isSolved) {
            println("Congratulations! You've guessed the code.")
        } else {
            println("Game over! The correct code was $secretCode")
        }
    }
}
