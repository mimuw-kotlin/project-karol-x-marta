class Player (private val sequenceLength: Int) {
    fun makeGuess(): List<String> {
        while (true) {
            println("Enter your guess ($sequenceLength colors separated by spaces):")
            val input = readLine() ?: ""
            val guess = input.split(" ")
            if (guess.size == sequenceLength) {
                return guess.map { it.replaceFirstChar { char -> char.uppercase() } }
            }
            else {
                println("Wrong input. Please enter $sequenceLength colors separated by spaces.")
            }
        }
    }
}