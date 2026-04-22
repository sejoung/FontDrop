package io.github.sejoung.fontdrop.util

class FakeClock(private var current: Long = 0L) : Clock {
    override fun nowMillis(): Long = current
    fun advanceTo(millis: Long) {
        current = millis
    }
    fun advanceBy(delta: Long) {
        current += delta
    }
}
