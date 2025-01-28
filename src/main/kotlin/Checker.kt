class Checker(
    val secretCode: List<String>,
) {
    fun checkGuess(guess: List<String>): Feedback {
        var correctCount = 0
        var misplacedCount = 0

        // mapa trzymająca dla każdego rodzaju koloru ile razy wystąpił w secret code
        // nie zostając zgadniętym na odpowiadającej pozycji guessa
        // (mają szansę być oznaczone jako misplaced)
        val secretMap: HashMap<String, Int> = HashMap()

        // mapa trzymająca dla każdego rodzaju koloru ile razy wystąpił w guessie
        // i nie zgadzał się z odpowiadającą mu pozycją w secret code
        // (mają szansę być oznaczone jako misplaced)
        val guessMap: HashMap<String, Int> = HashMap()

        // zlicza liczbę kolorów, które są na odpowiednich pozycjach i uzupełnia mapy
        for (i in guess.indices) {
            if (guess[i] == secretCode[i]) {
                correctCount++
            } else {
                secretMap[secretCode[i]] = secretMap.getOrDefault(secretCode[i], 0) + 1
                guessMap[guess[i]] = guessMap.getOrDefault(guess[i], 0) + 1
            }
        }

        // sprawdza dla każdego koloru ile razy wystąpił w obu mapach i zlicza misplacedCount
        for (color in guessMap.keys) {
            if (secretMap.containsKey(color)) {
                misplacedCount += minOf(guessMap.getValue(color), secretMap.getValue(color))
            }
        }

        return Feedback(correctCount, misplacedCount)
    }
}
