class Checker(val secretCode: List<String>) {

    fun checkGuess(guess: List<String>): Feedback{
        var correctCount = 0
        var misplacedCount = 0
        val secretSet: MutableList<String> = mutableListOf()
        val guessSet: MutableList<String> = mutableListOf()

        for (i in guess.indices) {
            if (guess[i] == secretCode[i]) correctCount++
            else {
                secretSet.add(secretCode[i])
                guessSet.add(guess[i])
            }
        }

        for (i in guessSet.indices) {
            for (j in secretSet.indices) {
                if (guessSet[i] == secretSet[j]) {
                    misplacedCount++
                    secretSet.removeAt(j)
                    break
                }
            }
        }

        return Feedback(correctCount, misplacedCount)
    }
}