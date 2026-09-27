package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class FpsMetrics(
    val currentFps: Int = 60,
    val averageFps: Float = 60.0f,
    val onePercentLowFps: Int = 54,
    val frameTimeMs: Float = 16.6f,
    val frameStabilityPercent: Int = 96,
    val isReliable: Boolean = true,
    val measurementMethod: String = "Choreographer Frame Timing (Native Display Sync)"
)

class PerformanceMonitor(private val context: Context) {

    private val _metrics = MutableStateFlow(FpsMetrics())
    val metrics: StateFlow<FpsMetrics> = _metrics.asStateFlow()

    private var isMonitoring = false
    private var lastFrameTimeNanos: Long = 0L
    private val frameDeltas = ArrayDeque<Long>(60)

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring) return

            if (lastFrameTimeNanos != 0L) {
                val delta = frameTimeNanos - lastFrameTimeNanos
                if (delta > 0) {
                    if (frameDeltas.size >= 60) {
                        frameDeltas.removeFirst()
                    }
                    frameDeltas.addLast(delta)
                    computeMetrics()
                }
            }
            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start() {
        if (isMonitoring) return
        isMonitoring = true
        lastFrameTimeNanos = 0L
        frameDeltas.clear()
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        isMonitoring = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        frameDeltas.clear()
    }

    private fun computeMetrics() {
        if (frameDeltas.isEmpty()) return

        val recentDeltas = frameDeltas.toList()
        val count = recentDeltas.size
        val totalNanos = recentDeltas.sum()
        if (totalNanos <= 0) return

        val avgDeltaMs = (totalNanos.toDouble() / count) / 1_000_000.0
        val currentDeltaMs = recentDeltas.last() / 1_000_000.0
        val instFps = if (currentDeltaMs > 0) (1000.0 / currentDeltaMs).coerceIn(1.0, 144.0).roundToInt() else 60
        val avgFps = if (avgDeltaMs > 0) (1000.0 / avgDeltaMs).coerceIn(1.0, 144.0).toFloat() else 60.0f

        // 1% low calculation from sorted worst frames
        val sortedWorst = recentDeltas.sortedDescending()
        val onePercentCount = (count * 0.10).coerceAtLeast(1.0).toInt()
        val worstSubset = sortedWorst.take(onePercentCount)
        val worstAvgDeltaMs = (worstSubset.sum().toDouble() / worstSubset.size) / 1_000_000.0
        val onePctLow = if (worstAvgDeltaMs > 0) (1000.0 / worstAvgDeltaMs).coerceIn(1.0, 144.0).roundToInt() else (avgFps * 0.85).roundToInt()

        // Stability percentage based on deviation from 16.6ms / target
        val expectedFrameMs = 1000.0 / avgFps.coerceAtLeast(30.0f)
        val jitterMs = recentDeltas.map { (it / 1_000_000.0) - expectedFrameMs }.map { kotlin.math.abs(it) }.average()
        val stability = (100 - (jitterMs * 4).coerceIn(0.0, 50.0)).roundToInt()

        _metrics.value = FpsMetrics(
            currentFps = instFps,
            averageFps = (avgFps * 10).roundToInt() / 10f,
            onePercentLowFps = onePctLow,
            frameTimeMs = (currentDeltaMs * 10).roundToInt() / 10f,
            frameStabilityPercent = stability,
            isReliable = true,
            measurementMethod = "Choreographer Frame Timing (Native Display Sync)"
        )
    }

    fun getMemoryStats(): Pair<Long, Long> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val info = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(info)
        val freeMb = info.availMem / (1024 * 1024)
        val totalMb = info.totalMem / (1024 * 1024)
        return Pair(freeMb, totalMb)
    }
}
