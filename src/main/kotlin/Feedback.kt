class Feedback(
    val correct: Int,
    val misplaced: Int,
) {
    override fun toString(): String = "Correct: $correct, Misplaced: $misplaced"
}
