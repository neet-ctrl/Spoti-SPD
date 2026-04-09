package dev.sumanth.spd.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import dev.sumanth.spd.App
import dev.sumanth.spd.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryRefreshService : Service() {

    companion object {
        const val EXTRA_SCAN_MODE = "extra_scan_mode"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val scanMode = intent?.getBooleanExtra(EXTRA_SCAN_MODE, false) ?: false
        
        // Create a ViewModelStoreOwner to get access to ViewModel
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }

        val libraryViewModel = ViewModelProvider(viewModelStoreOwner)[LibraryViewModel::class.java]

        // Set scan mode and trigger refresh in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                libraryViewModel.setScanWholeStorage(scanMode)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }
}