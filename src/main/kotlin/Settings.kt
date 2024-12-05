class Settings(val sequenceLength: Int, val maxAttempts: Int, val colorsList: List<String>) {
    val numberOfColors = colorsList.size
    init {
        require(sequenceLength in 3..6) { "Sequence length should be from 3 to 6" }
        require(maxAttempts in 3..20) { "Maximum number of attempts should be from 1 to 20" }
        require(numberOfColors in 3..8) { "Number of colors should be from 4 to 8" }
        // check if colors are unique
        require(colorsList.toSet().size == numberOfColors) { "Colors should be unique" }
    }
}