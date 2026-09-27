package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.engine.PotatoVisualEngine
import kotlin.math.roundToInt

class VisualOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isRunning = false
    private var lastFrameTime = 0L
    private var fpsCounter = 60
    private var frameDeltas = ArrayDeque<Long>(30)

    private var fpsTextView: TextView? = null
    private var statusTextView: TextView? = null

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return
            if (lastFrameTime != 0L) {
                val delta = frameTimeNanos - lastFrameTime
                if (delta > 0) {
                    if (frameDeltas.size >= 30) frameDeltas.removeFirst()
                    frameDeltas.addLast(delta)
                    val avgNanos = frameDeltas.average()
                    if (avgNanos > 0) {
                        val instFps = (1_000_000_000.0 / avgNanos).roundToInt().coerceIn(15, 144)
                        fpsCounter = instFps
                        updateOverlayText()
                    }
                }
            }
            lastFrameTime = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        if (Settings.canDrawOverlays(this)) {
            setupOverlay()
        }
        isRunning = true
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Blox Booster Gaming Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time FPS and Potato Visual status while gaming"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, VisualOverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Blox Booster Active")
            .setContentText("Potato Visual Mode & Frame Monitor running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Overlay", stopPIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 120
        }

        val pillLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 14, 24, 14)
            // Cyberpunk dark pill with neon purple border
            val shape = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#E60D0A1A"))
                setStroke(3, Color.parseColor("#A855F7"))
                cornerRadius = 32f
            }
            background = shape
            gravity = Gravity.CENTER_VERTICAL
        }

        // FPS text
        fpsTextView = TextView(this).apply {
            text = "60 FPS"
            setTextColor(Color.parseColor("#00F0FF")) // Neon Cyan
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 16, 0)
        }
        pillLayout.addView(fpsTextView)

        // Status text
        statusTextView = TextView(this).apply {
            text = "POTATO ON"
            setTextColor(Color.parseColor("#A855F7")) // Neon Purple
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
        }
        pillLayout.addView(statusTextView)

        // Touch listener for dragging
        pillLayout.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = false

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (kotlin.math.abs(dx) > 10 || kotlin.math.abs(dy) > 10) {
                            isClick = false
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(pillLayout, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            // Quick toggle potato mode on tap
                            PotatoVisualEngine.togglePotatoVisualMode()
                            updateOverlayText()
                        }
                        return true
                    }
                }
                return false
            }
        })

        overlayView = pillLayout
        try {
            windowManager?.addView(overlayView, params)
        } catch (_: Exception) {
            // Safely fail if overlay permission was revoked
        }
    }

    private fun updateOverlayText() {
        val isPotato = PotatoVisualEngine.config.value.potatoModeActive
        val fpsColor = when {
            fpsCounter >= 55 -> Color.parseColor("#00F0FF")
            fpsCounter >= 35 -> Color.parseColor("#FBBF24")
            else -> Color.parseColor("#EF4444")
        }
        fpsTextView?.post {
            fpsTextView?.text = "$fpsCounter FPS"
            fpsTextView?.setTextColor(fpsColor)
            statusTextView?.text = if (isPotato) "POTATO ON" else "NATIVE"
            statusTextView?.setTextColor(if (isPotato) Color.parseColor("#C084FC") else Color.parseColor("#94A3B8"))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (_: Exception) {}
            overlayView = null
        }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "blox_booster_hud_channel"
        const val NOTIFICATION_ID = 2026
        const val ACTION_STOP = "com.example.action.STOP_OVERLAY"

        fun start(context: Context) {
            val intent = Intent(context, VisualOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VisualOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
