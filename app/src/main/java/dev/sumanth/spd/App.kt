package dev.sumanth.spd

import android.app.Application
import dev.sumanth.spd.utils.WidgetLogger

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val logger = WidgetLogger(this)
        logger.logInfo("App onCreate called")

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logger.logError("Uncaught exception in thread ${thread.name}", throwable)

            val sharedPref = getSharedPreferences("crash_log", MODE_PRIVATE)
            sharedPref.edit()
                .putString("error", throwable.message ?: "Unknown error")
                .putString("stack", throwable.stackTraceToString())
                .putLong("time", System.currentTimeMillis())
                .apply()

            // Call default handler to allow crash
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }

        logger.logDebug("App initialization completed")
    }
}