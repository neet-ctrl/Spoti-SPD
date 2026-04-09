package dev.sumanth.spd.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import dev.sumanth.spd.SpdApplication
import dev.sumanth.spd.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryRefreshService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create a ViewModelStoreOwner to get access to ViewModel
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }

        val factory = (application as SpdApplication).viewModelFactory
        val libraryViewModel = ViewModelProvider(viewModelStoreOwner, factory)[LibraryViewModel::class.java]

        // Trigger refresh in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                libraryViewModel.refresh()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }
}