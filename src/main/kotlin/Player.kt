class Player {
    fun makeGuess(): List<String> {
        println("Enter your guess (4 colors separated by spaces):")
        val input = readLine() ?: ""
        return input.split(" ").map { it.capitalize() }
    }
}