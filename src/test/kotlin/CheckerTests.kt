import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class CheckerTests {
    @Test
    fun checkAllCorrect() {
        for (sequenceLength in MIN_SEQ_LENGTH..MAX_SEQ_LENGTH) {
            for (colors in MIN_COLORS..MAX_COLORS) {
                val code = List(sequenceLength) { Random.nextInt(0, colors).toString() }
                val checker = Checker(code)
                checker.checkGuess(code).apply {
                    assertEquals(sequenceLength, correct)
                    assertEquals(0, misplaced)
                }
            }
        }
    }

    @Test
    fun checkAllMisplaced() {
        val code = listOf("A", "B", "C", "D")
        val guess = listOf("B", "C", "D", "A")
        val code2 = listOf("A", "B", "C", "D", "A")
        val guess2 = listOf("B", "C", "A", "A", "D")
        val code3 = listOf("A", "B", "C", "D", "A", "B")
        val guess3 = listOf("B", "C", "B", "A", "D", "A")
        val checker = Checker(code)
        val checker2 = Checker(code2)
        val checker3 = Checker(code3)
        checker.checkGuess(guess).apply {
            assertEquals(0, correct)
            assertEquals(4, misplaced)
        }
        checker2.checkGuess(guess2).apply {
            assertEquals(0, correct)
            assertEquals(5, misplaced)
        }
        checker3.checkGuess(guess3).apply {
            assertEquals(0, correct)
        }
    }

    @Test
    fun checkMixed() {
        val code = listOf("A", "B", "C", "D")
        val guess = listOf("B", "C", "A", "D")
        val code2 = listOf("B", "B", "C", "D", "D")
        val guess2 = listOf("A", "B", "B", "C", "D")
        val code3 = listOf("A", "B", "B", "D", "A", "B")
        val guess3 = listOf("B", "A", "D", "B", "A", "C")
        val checker = Checker(code)
        val checker2 = Checker(code2)
        val checker3 = Checker(code3)
        checker.checkGuess(guess).apply {
            assertEquals(1, correct)
            assertEquals(3, misplaced)
        }
        checker2.checkGuess(guess2).apply {
            assertEquals(2, correct)
            assertEquals(2, misplaced)
        }
        checker3.checkGuess(guess3).apply {
            assertEquals(1, correct)
            assertEquals(4, misplaced)
        }
    }
}
