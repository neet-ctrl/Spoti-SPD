package dev.sumanth.spd

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.sumanth.spd.model.NavigationItem
import dev.sumanth.spd.ui.component.Background
import dev.sumanth.spd.ui.component.BottomBar
import dev.sumanth.spd.ui.component.PermissionDialog
import dev.sumanth.spd.ui.component.TopBar
import dev.sumanth.spd.ui.component.UpdateDialog
import dev.sumanth.spd.ui.screen.HomeScreen
import dev.sumanth.spd.ui.screen.HistoryScreen
import dev.sumanth.spd.ui.screen.PreferencesScreen
import dev.sumanth.spd.ui.viewmodel.UpdaterViewModel
import dev.sumanth.spd.utils.NewPipeDownloader
import dev.sumanth.spd.utils.SharedPref
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : ComponentActivity() {

    private val navigationItems = listOf(
        NavigationItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem("History", Icons.Filled.History, Icons.Outlined.History),
        NavigationItem("Preferences", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    private val updateViewModel: UpdaterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = SharedPref(this)
        if (sharedPref.getAutoUpdateCheck()) {
            updateViewModel.checkForUpdate(this)
        }

        NewPipe.init(NewPipeDownloader.getInstance())

        // Check for crash log
        val crashSharedPref = getSharedPreferences("crash_log", MODE_PRIVATE)
        val crashError = crashSharedPref.getString("error", null)
        val crashStack = crashSharedPref.getString("stack", "") ?: ""
        val hasCrash = crashError != null
        if (hasCrash) {
            crashSharedPref.edit().clear().apply()
        }

        setContent {

            val pagerState = rememberPagerState(pageCount = { navigationItems.size })
            val title by remember {
                derivedStateOf { navigationItems[pagerState.currentPage].title }
            }

            var showCrashDialog by remember { mutableStateOf(hasCrash) }

            Background {
                Scaffold(
                    topBar = { TopBar(title) },
                    bottomBar = { BottomBar(navigationItems, pagerState) }
                ) { innerPadding ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen()
                            1 -> HistoryScreen()
                            2 -> PreferencesScreen()
                        }
                    }

                    PermissionDialog(this)
                    UpdateDialog(updateViewModel)
                    if (showCrashDialog) {
                        CrashDialog(
                            errorMessage = crashError ?: "Unknown error",
                            stackTrace = crashStack,
                            onDismiss = { showCrashDialog = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CrashDialog(
    errorMessage: String,
    stackTrace: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Crashed") },
        text = {
            Column {
                Text("An unexpected error occurred:")
                Text(errorMessage, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Stack Trace:")
                OutlinedTextField(
                    value = stackTrace,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.height(200.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Crash Log", "$errorMessage\n\n$stackTrace")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Crash log copied to clipboard", Toast.LENGTH_SHORT).show()
            }) {
                Text("Copy")
            }
        }
    )
}
