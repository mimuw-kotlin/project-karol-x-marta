open class TimeProvider {
    open fun getCurrentTime(): Long = System.currentTimeMillis()
}
