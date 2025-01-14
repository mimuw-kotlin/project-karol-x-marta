class Checker(val secretCode: List<String>) {

    fun checkGuess(guess: List<String>): Feedback {
        var correctCount = 0
        var misplacedCount = 0
        val secretMap: HashMap<String, Int> = HashMap()
        val guessMap: HashMap<String, Int> = HashMap()

        for (i in guess.indices) {
            if (guess[i] == secretCode[i]) {
                correctCount++
            } else {
                secretMap[secretCode[i]] = secretMap.getOrDefault(secretCode[i], 0) + 1
                guessMap[guess[i]] = guessMap.getOrDefault(guess[i], 0) + 1
            }
        }

        for (color in guessMap.keys) {
            if (secretMap.containsKey(color)) {
                misplacedCount += minOf(guessMap.getValue(color), secretMap.getValue(color))
            }
        }

        return Feedback(correctCount, misplacedCount)
    }
}
