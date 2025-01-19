import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class TimeManager {
    var startTime: Long? by mutableStateOf(null)
    var pausedTime: Long? by mutableStateOf(null)
    var timer: Long by mutableStateOf(0L)
    var gameEndedTime: Long by mutableStateOf(0L)

    fun startTimer() {
        startTime = System.currentTimeMillis()
    }

    fun pauseTimer() {
        pausedTime = System.currentTimeMillis()
    }

    fun resumeTimer() {
        startTime = startTime?.plus(System.currentTimeMillis() - (pausedTime ?: 0L))
        pausedTime = null
    }

    fun updateTimer() {
        startTime?.let {
            timer = System.currentTimeMillis() - it
        }
    }

    fun setTimer() {
        timer = gameEndedTime
    }

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