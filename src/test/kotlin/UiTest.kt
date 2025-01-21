import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.jupiter.api.Test


class UiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testStartingWindow() = runComposeUiTest {

        setContent {
            app("localhost", 12345)
        }

        onNodeWithText("Submit").assertExists()

    }
}