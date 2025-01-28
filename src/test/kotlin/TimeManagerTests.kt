import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TimeManagerTests {
    private class TestTimeProvider : TimeProvider() {
        private var currentTime: Long = 0L

        override fun getCurrentTime(): Long = currentTime

        fun setTime(time: Long) {
            currentTime = time
        }
    }

    private val testTimeProvider = TestTimeProvider()
    private val timeManager = TimeManager(testTimeProvider)

    @Test
    fun testResumeTimer() {
        testTimeProvider.setTime(0L)
        timeManager.startTimer()
        testTimeProvider.setTime(2000L)
        timeManager.pauseTimer()
        testTimeProvider.setTime(5000L)
        timeManager.resumeTimer()
        assertEquals(3000L, timeManager.startTime)
        // zmienna startTime jest "sztuczna" i trzyma ona taki czas,
        // aby różnica między nią a currentTime wynosiła tyle ile trwała rozgrywka z wyłączeniem przerw,
        // czyli w tym przypadku rozgrywka trwała 2 sekundy, nastęopnie nastąpił 3-sekundowa pauza;
        // skoro aktualny czas to 5 sekund, to startTime powinno wynosić: 5 - 2 = 3,
    }

    @Test
    fun testUpdateTimer() {
        testResumeTimer()
        timeManager.updateTimer()
        assertEquals(2000L, timeManager.timer)
        testTimeProvider.setTime(6000L)
        timeManager.updateTimer()
        assertEquals(3000L, timeManager.timer)
        // timer po updatowaniu powinien być równy czasem nieprzerwanej rozgrywki,
        // czyli wg tego co powyżej być różnicą między currentTime a startTime
    }
}
