import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UiTests {
    private val minColorList = DEFAULT_COLORS_LIST.subList(0, MIN_COLORS)

    private fun ComposeUiTest.spinBoxTest(
        description: String,
        startValue: Int,
        minValue: Int,
        maxValue: Int,
    ) {
        onNodeWithContentDescription(description).assertExists()

        // Check if the initial value is correct
        onNodeWithContentDescription(description).assertTextEquals("$startValue")

        // Check if the value can be changed
        onNodeWithContentDescription(description).performTextClearance()
        onNodeWithContentDescription(description).performTextInput("5")
        onNodeWithContentDescription(description).assertTextEquals("5")

        // Check if the information about the allowed range is displayed when the value is out of range
        onNodeWithContentDescription(description).performTextClearance()
        onNodeWithContentDescription(description).performTextInput("100")
        onNodeWithText("Allowed range: $minValue - $maxValue").assertExists()

        // Check if arrows can be clicked when value is in range and can be increased and decreased
        onNodeWithContentDescription(description).performTextClearance()
        onNodeWithContentDescription(description).performTextInput("$startValue")
        onNodeWithContentDescription("Decrease $description").assertExists()
        onNodeWithContentDescription("Increase $description").assertIsEnabled()
        onNodeWithContentDescription("Decrease $description").assertExists()
        onNodeWithContentDescription("Increase $description").assertIsEnabled()

        // Check if arrows cannot be clicked while the value is at the minimum or maximum
        onNodeWithContentDescription(description).performTextClearance()
        onNodeWithContentDescription(description).performTextInput("$minValue")
        onNodeWithContentDescription("Decrease $description").assertIsNotEnabled()
        onNodeWithContentDescription("Increase $description").assertIsEnabled()
        onNodeWithContentDescription(description).performTextClearance()
        onNodeWithContentDescription(description).performTextInput("$maxValue")
        onNodeWithContentDescription("Decrease $description").assertIsEnabled()
        onNodeWithContentDescription("Increase $description").assertIsNotEnabled()

        // Check if the value is changed after clicking the arrows
        onNodeWithContentDescription(description).performTextClearance()
        onNodeWithContentDescription(description).performTextInput("$startValue")
        onNodeWithContentDescription("Increase $description").performClick()
        onNodeWithContentDescription(description).assertTextEquals("${startValue + 1}")
        onNodeWithContentDescription("Decrease $description").performClick()
        onNodeWithContentDescription(description).assertTextEquals("$startValue")
    }

    private fun ComposeUiTest.colorsTest() {
        // Check if there are 8 checkboxes
        assertEquals(onAllNodes(hasContentDescription("Color")).fetchSemanticsNodes().size, MAX_COLORS)

        // Check if correct ones are checked
        for (color in ALL_COLORS.keys) {
            if (color in DEFAULT_COLORS_LIST) {
                onNodeWithTag(color).assertExists().assertIsOn()
            } else {
                onNodeWithTag(color).assertExists().assertIsOff()
            }
        }

        // Check if clicking on checkbox changes its state
        for (color in ALL_COLORS.keys) {
            if (color in DEFAULT_COLORS_LIST) {
                onNodeWithTag(color).performClick()
                onNodeWithTag(color).assertIsOff()
                onNodeWithTag(color).performClick()
                onNodeWithTag(color).assertIsOn()
            } else {
                onNodeWithTag(color).performClick()
                onNodeWithTag(color).assertIsOn()
                onNodeWithTag(color).performClick()
                onNodeWithTag(color).assertIsOff()
            }
        }

        // Check if clicking checking box off doesn't work when there are only 3 checked
        var clicked = DEFAULT_COLORS_LIST.size
        while (clicked > MIN_COLORS) {
            onNodeWithTag(DEFAULT_COLORS_LIST[--clicked]).performClick()
        }
        onNodeWithTag(DEFAULT_COLORS_LIST[0]).performClick().assertIsOn()
    }

    @Test
    fun testInitialWindow() =
        runComposeUiTest {
            setContent {
                App("localhost", 12345)
            }
            onNodeWithText("Submit").assertExists()
            //   onNodeWithText("Time: 0.000 s  ").assertExists()
            onNodeWithContentDescription("Settings").assertExists()
            onNodeWithContentDescription("Trophy").assertExists()
            onNodeWithContentDescription("Multiplayer").assertExists()
            onNodeWithText("||").assertExists()

            // Choosing colors to start game view
            for (i in 0 until DEFAULT_SEQ_LENGTH) {
                onNodeWithTag("$i").assertExists()
            }
            onNodeWithTag("0").performClick()
            onNodeWithTag("No Color").assertExists()
            for (color in DEFAULT_COLORS_LIST) {
                onNodeWithTag(color).assertExists()
            }
        }

    @Test
    fun testSettingsView() =
        runComposeUiTest {
            setContent {
                App("localhost", 12345)
            }
            onNodeWithContentDescription("Settings").assertExists()
            onNodeWithContentDescription("Settings").performClick()
            spinBoxTest("Sequence Length", DEFAULT_SEQ_LENGTH, MIN_SEQ_LENGTH, MAX_SEQ_LENGTH)
            spinBoxTest("Max Attempts", DEFAULT_ATTEMPTS, MIN_ATTEMPTS, MAX_ATTEMPTS)
            colorsTest()
        }

    @Test
    fun testScoresView() =
        runComposeUiTest {
            setContent {
                ScoresManager.connect()
                ScoresManager.createScoresTable()
                App("localhost", 12345)
            }
            onNodeWithContentDescription("Trophy").assertExists()
            onNodeWithContentDescription("Trophy").performClick()
            onNodeWithText("Scores for chosen parameters:").assertExists()
            runBlocking {
                withTimeout(1000L) {
                    launch(Dispatchers.IO) {
                        ScoresManager
                            .getFilteredScores(DEFAULT_SEQ_LENGTH, DEFAULT_ATTEMPTS, DEFAULT_COLORS_LIST.size)
                            .forEachIndexed { index, score ->
                                if (index < 8) {
                                    onNodeWithText("${index + 1}. ${score}s").assertExists()
                                    // widać maksymalnie 8 wyników bez scrollowania
                                }
                            }
                    }.join()
                }
            }
            spinBoxTest("Sequence Length", DEFAULT_SEQ_LENGTH, MIN_SEQ_LENGTH, MAX_SEQ_LENGTH)
            spinBoxTest("Max Attempts", DEFAULT_ATTEMPTS, MIN_ATTEMPTS, MAX_ATTEMPTS)
            spinBoxTest("Colors Number", DEFAULT_COLORS_LIST.size, MIN_COLORS, MAX_COLORS)
        }

    @Test
    fun testGameSinglePlayer() =
        runComposeUiTest {
            setContent {
                App("localhost", 12345)
            }
            for (attempt in 0 until DEFAULT_ATTEMPTS) {
                for (index in 0 until DEFAULT_SEQ_LENGTH) {
                    onNodeWithTag(index.toString()).assertExists().performClick()
                    val color = DEFAULT_COLORS_LIST[Random.nextInt(DEFAULT_COLORS_LIST.size)]
                    onNodeWithTag(color).assertExists().performClick()
                }
                onNodeWithText("Submit").assertExists().performClick()
                val ended =
                    try {
                        onNodeWithText("The secret code was: \n").assertExists()
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                if (ended) {
                    break
                }
            }
            onNodeWithText("The secret code was: \n").assertExists()
            onNodeWithText("Submit").assertDoesNotExist()
            onNodeWithText("New Game").assertExists()
            onNodeWithText("Exit").assertExists()
        }

    private fun ComposeUiTest.changeSettingsToMinimal() {
        onNodeWithContentDescription("Settings").assertExists().performClick()
        onNodeWithContentDescription("Sequence Length").assertExists()
        onNodeWithContentDescription("Sequence Length").performTextClearance()
        onNodeWithContentDescription("Sequence Length").performTextInput(MIN_SEQ_LENGTH.toString())
        var clicked = DEFAULT_COLORS_LIST.size
        while (clicked > MIN_COLORS) {
            onNodeWithTag(DEFAULT_COLORS_LIST[--clicked]).assertExists().performClick()
        }
        onNodeWithText("Apply and Restart").assertExists().performClick()
    }

    @Test
    fun applyMinimalSettings() =
        runComposeUiTest {
            setContent {
                App("localhost", 12345)
            }
            // zmieniamy liste kolorów i długość sekwencji na minimalne
            changeSettingsToMinimal()

            // sprawdzamy czy ui zmieniła się długość sekwencji
            for (index in 0 until MIN_SEQ_LENGTH) {
                onNodeWithTag(index.toString()).assertExists()
            }
            onNodeWithTag(MIN_SEQ_LENGTH.toString()).assertDoesNotExist()
            onNodeWithTag("0").performClick()

            // sprawdzamy czy ui zmieniła się lista dostępnych kolorów
            onNodeWithTag("No Color").assertExists()
            for (color in DEFAULT_COLORS_LIST) {
                if (color in minColorList) {
                    onNodeWithTag(color).assertExists()
                } else {
                    onNodeWithTag(color).assertDoesNotExist()
                }
            }
        }

    @Test
    fun winSinglePlayerGame() =
        runComposeUiTest {
            setContent {
                App("localhost", 12345)
            }
            changeSettingsToMinimal()
            val allPossibleGuesses = mutableListOf<List<String>>()
            for (color1 in minColorList) {
                for (color2 in minColorList) {
                    allPossibleGuesses.add(listOf(color1, color2))
                }
            }
            for (index in 0 until DEFAULT_ATTEMPTS) {
                val guess = allPossibleGuesses[index]
                onNodeWithTag("0").performClick()
                onNodeWithTag(guess[0]).performClick()
                onNodeWithTag("1").performClick()
                onNodeWithTag(guess[1]).performClick()
                onNodeWithText("Submit").performClick()
                val won =
                    try {
                        onNodeWithText("Congratulations! You've guessed the sequence in ${index + 1} attempts.\n").assertExists()
                        true
                    } catch (e: AssertionError) {
                        false
                    }
                if (won) {
                    break
                }
            }
            onNodeWithText("Submit").assertDoesNotExist()
            onNodeWithText("New Game").assertExists()
            onNodeWithText("Exit").assertExists()
        }
}
