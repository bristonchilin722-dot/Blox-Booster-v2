package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method

enum class ShizukuServiceStatus {
    NOT_INSTALLED,
    SERVICE_STOPPED,
    PERMISSION_REQUIRED,
    AUTHORIZED
}

data class ShizukuState(
    val status: ShizukuServiceStatus = ShizukuServiceStatus.NOT_INSTALLED,
    val isInstalled: Boolean = false,
    val isRunning: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val shizukuVersion: Int = 0,
    val lastCommandLog: String? = null,
    val lastError: String? = null
)

data class CommandExecutionResult(
    val success: Boolean,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val command: String
)

object ShizukuManager {
    const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    const val REQUEST_CODE_SHIZUKU = 7012

    private val _state = MutableStateFlow(ShizukuState())
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private var appContext: Context? = null
    private var isListenerRegistered = false

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        checkStatus()
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE_SHIZUKU) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            _state.value = _state.value.copy(
                isPermissionGranted = granted,
                status = if (granted) ShizukuServiceStatus.AUTHORIZED else ShizukuServiceStatus.PERMISSION_REQUIRED,
                lastCommandLog = if (granted) "Shizuku permission granted by user." else "Shizuku permission denied by user."
            )
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        if (!isListenerRegistered) {
            try {
                Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
                Shizuku.addBinderDeadListener(binderDeadListener)
                Shizuku.addRequestPermissionResultListener(permissionResultListener)
                isListenerRegistered = true
            } catch (e: Throwable) {
                _state.value = _state.value.copy(lastError = "Shizuku init error: ${e.message}")
            }
        }
        checkStatus(appContext)
    }

    fun checkStatus(context: Context? = null) {
        val ctx = context ?: appContext

        var installed = false
        if (ctx != null) {
            installed = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    ctx.packageManager.getPackageInfo(SHIZUKU_PACKAGE, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    ctx.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
                }
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }

        var running = false
        var version = 0
        try {
            running = Shizuku.pingBinder()
            if (running) {
                version = Shizuku.getVersion()
            }
        } catch (_: Throwable) {
            running = false
        }

        var granted = false
        if (running) {
            try {
                granted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } catch (_: Throwable) {
                granted = false
            }
        }

        val resolvedStatus = when {
            !installed -> ShizukuServiceStatus.NOT_INSTALLED
            !running -> ShizukuServiceStatus.SERVICE_STOPPED
            !granted -> ShizukuServiceStatus.PERMISSION_REQUIRED
            else -> ShizukuServiceStatus.AUTHORIZED
        }

        _state.value = _state.value.copy(
            status = resolvedStatus,
            isInstalled = installed,
            isRunning = running,
            isPermissionGranted = granted,
            shizukuVersion = version
        )
    }

    fun requestPermission(): Boolean {
        if (!_state.value.isRunning) {
            _state.value = _state.value.copy(lastError = "Cannot request permission: Shizuku service is not running.")
            return false
        }
        return try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _state.value = _state.value.copy(
                    isPermissionGranted = true,
                    status = ShizukuServiceStatus.AUTHORIZED,
                    lastCommandLog = "Permission already granted."
                )
                true
            } else {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
                true
            }
        } catch (e: Throwable) {
            _state.value = _state.value.copy(lastError = "Failed to request Shizuku permission: ${e.message}")
            false
        }
    }

    suspend fun executeCommand(command: String): CommandExecutionResult = withContext(Dispatchers.IO) {
        if (!_state.value.isRunning) {
            return@withContext CommandExecutionResult(
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = "Shizuku service is not running. Please start Shizuku first.",
                command = command
            )
        }
        if (!_state.value.isPermissionGranted) {
            return@withContext CommandExecutionResult(
                success = false,
                exitCode = -2,
                stdout = "",
                stderr = "Permission not granted. Please authorize Blox Booster inside Shizuku.",
                command = command
            )
        }

        try {
            val process = invokeShizukuProcess(arrayOf("sh", "-c", command))
                ?: return@withContext CommandExecutionResult(
                    success = false,
                    exitCode = -4,
                    stdout = "",
                    stderr = "Could not invoke Shizuku process executor.",
                    command = command
                )

            val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText().trim() }
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText().trim() }
            val exitCode = process.waitFor()

            val success = exitCode == 0
            val logMessage = if (success) {
                "Command executed successfully (exit 0): $command"
            } else {
                "Command failed (exit $exitCode): ${stderr.ifBlank { stdout }}"
            }

            _state.value = _state.value.copy(
                lastCommandLog = logMessage,
                lastError = if (!success) stderr.ifBlank { "Exit code $exitCode" } else null
            )

            CommandExecutionResult(
                success = success,
                exitCode = exitCode,
                stdout = stdout,
                stderr = stderr,
                command = command
            )
        } catch (e: Throwable) {
            val errMsg = "Execution failed: ${e.message}"
            _state.value = _state.value.copy(lastError = errMsg)
            CommandExecutionResult(
                success = false,
                exitCode = -3,
                stdout = "",
                stderr = errMsg,
                command = command
            )
        }
    }

    private fun invokeShizukuProcess(cmdArray: Array<String>): Process? {
        return try {
            val method: Method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(null, cmdArray, null, null) as? Process
        } catch (e: Throwable) {
            null
        }
    }

    suspend fun setScreenResolution(width: Int, height: Int, densityDpi: Int): CommandExecutionResult {
        val cmd = "wm size ${width}x${height} && wm density $densityDpi"
        return executeCommand(cmd)
    }

    suspend fun resetScreenResolution(): CommandExecutionResult {
        val cmd = "wm size reset && wm density reset"
        return executeCommand(cmd)
    }

    fun openShizukuApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            try {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$SHIZUKU_PACKAGE")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(storeIntent)
            } catch (_: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        }
    }
}
