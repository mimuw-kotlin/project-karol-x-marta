class Feedback(val correct: Int, val misplaced: Int) {
    override fun toString(): String {
        return "Correct: $correct, Misplaced: $misplaced"
    }
}
