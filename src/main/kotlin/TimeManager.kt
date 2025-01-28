import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TimeManager(
    val timeProvider: TimeProvider,
) {
    var startTime: Long? by mutableStateOf(null)
    var pausedTime: Long? by mutableStateOf(null)
    var timer: Long by mutableLongStateOf(0L)
    var gameEndedTime: Long by mutableLongStateOf(0L)

    fun startTimer() {
        startTime = timeProvider.getCurrentTime()
    }

    fun pauseTimer() {
        pausedTime = timeProvider.getCurrentTime()
    }

    fun resumeTimer() {
        startTime = startTime?.plus(timeProvider.getCurrentTime() - (pausedTime ?: 0L))
        pausedTime = null
    }

    fun updateTimer() {
        startTime?.let {
            timer = timeProvider.getCurrentTime() - it
        }
    }

    // służy do dokładnego zapamiętania dokładnego czasu trwania rozgrywki, ponieważ wyświetlany na bieżąco
    // timer inaczej minimalnie różniłby się od przesłanego użytkownikowi czasu rozgrywki
    fun setTimer() {
        timer = gameEndedTime
    }

    // a to po prostu ustawia czas trwania gry od razu, gdy ta się skończy
    fun setGameEndedTime() {
        gameEndedTime = timer
    }

    fun reset() {
        startTime = null
        pausedTime = null
        timer = 0L
        gameEndedTime = 0L
    }

    fun resetAndStart() {
        reset()
        startTimer()
    }
}
