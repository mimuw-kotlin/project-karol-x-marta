class Player (private val sequenceLength: Int, private val colorsList: List<String>) {
    fun makeGuess(): List<String> {
        while (true) {
            println("Enter your guess ($sequenceLength colors separated by spaces):")
            val input = readLine() ?: ""
            val guess = input.split(" ").toMutableList()
            if (validateGuess(guess, colorsList)) {
                return guess
            }
            println("Wrong input. Please enter $sequenceLength colors separated by spaces.")
        }
    }

    fun validateGuess(guess: MutableList<String>, colorsList: List<String>): Boolean {
        // TODO: sprawdzanie kolorów
        if (guess.size == sequenceLength) {
            for (i in guess.indices) {
                guess[i] = guess[i].replaceFirstChar { char -> char.uppercase() }
            }
            return true
        }
        return false
    }
}