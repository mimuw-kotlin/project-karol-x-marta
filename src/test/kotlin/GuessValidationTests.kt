import org.junit.jupiter.api.Test
import kotlin.random.Random

class GuessValidationTests {

    @Test
    fun wrongGuessLength() {
        for (sequenceLength in MIN_SEQ_LENGTH..MAX_SEQ_LENGTH) {
            for (colors in MIN_COLORS..MAX_COLORS) {
                val colorsList = List(colors) { it.toString() }
                val player = Player(sequenceLength, colorsList)
                val guess = MutableList(sequenceLength + 1) { Random.nextInt(0, colors).toString() }
                val guess2 = MutableList(sequenceLength - 1) { Random.nextInt(0, colors).toString() }
                val guess3 = MutableList(sequenceLength) { Random.nextInt(0, colors).toString() }
                assert(!player.validateGuess(guess, colorsList))
                assert(!player.validateGuess(guess2, colorsList))
                assert(player.validateGuess(guess3, colorsList))
            }
        }
    }

    @Test
    fun wrongGuessColors() {
        for (sequenceLength in MIN_SEQ_LENGTH..MAX_SEQ_LENGTH) {
            for (colors in MIN_COLORS..MAX_COLORS) {
                val colorsList = List(colors) { it.toString() }
                val player = Player(sequenceLength, colorsList)
                val guess = MutableList(sequenceLength) { Random.nextInt(0, colors).toString() }
                val guess2 = MutableList(sequenceLength) { Random.nextInt(0, colors).toString() }
                guess2[Random.nextInt(0, sequenceLength)] = "A"
                assert(player.validateGuess(guess, colorsList))
                assert(!player.validateGuess(guess2, colorsList))
            }
        }
    }

    @Test
    fun capitalDoesntMatter() {
        for (sequenceLength in MIN_SEQ_LENGTH..MAX_SEQ_LENGTH) {
            for (colors in MIN_COLORS..MAX_COLORS) {
                val colorsList = List(colors) { ('A' + it).toString() }
                val player = Player(sequenceLength, colorsList)
                val guess = MutableList(sequenceLength) { ('a' + Random.nextInt(0, colors)).toString() }
                assert(player.validateGuess(guess, colorsList))
            }
        }
    }
}