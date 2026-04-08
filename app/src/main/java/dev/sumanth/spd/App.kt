package dev.sumanth.spd

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sharedPref = getSharedPreferences("crash_log", MODE_PRIVATE)
            sharedPref.edit()
                .putString("error", throwable.message ?: "Unknown error")
                .putString("stack", throwable.stackTraceToString())
                .putLong("time", System.currentTimeMillis())
                .apply()

            // Call default handler to allow crash
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
    }
}