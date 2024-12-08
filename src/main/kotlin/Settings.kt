
class Settings(val sequenceLength: Int, val maxAttempts: Int, val colorsList: List<String>) {
    val numberOfColors = colorsList.size
    init {
        require(sequenceLength in MIN_SEQ_LENGTH..MAX_SEQ_LENGTH) {
            "Sequence length should be from $MIN_SEQ_LENGTH to $MAX_SEQ_LENGTH"
        }
        require(maxAttempts in MIN_ATTEMPTS..MAX_ATTEMPTS) {
            "Maximum number of attempts should be from $MIN_ATTEMPTS to $MAX_ATTEMPTS"
        }
        require(numberOfColors in MIN_COLORS..MAX_COLORS) {
            "Number of colors should be from $MIN_COLORS to $MAX_COLORS"
        }
        require(colorsList.toSet().size == numberOfColors) {
            "Colors should be unique"
        }
    }
}