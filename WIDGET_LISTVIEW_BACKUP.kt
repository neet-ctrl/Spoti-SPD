// BACKUP: ListView RemoteViewsService code for widget song list
// This code causes "can't load widget" error - disabled temporarily
// Restore this when fixing the RemoteViewsService/RemoteViewsFactory binding

// === REMOVE FROM widget_music_player.xml layout ===
// Remove this ListView from the main widget layout:
/*
<ListView
    android:id="@+id/widget_song_list"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1"
    android:divider="@null"
    android:dividerHeight="0dp"
    android:scrollbars="none"
    android:background="@android:color/transparent" />
*/

// === CODE TO RESTORE IN MusicPlayerWidgetProvider ===
/*
// Remove Uri import if not used elsewhere
import android.net.Uri

// In companion object updateAppWidget(), add this after volumePercent calculation:
val adapterIntent = Intent(context, WidgetSongListService::class.java).apply {
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
}

val itemClickFlags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
} else {
    PendingIntent.FLAG_UPDATE_CURRENT
}
val itemClickTemplate = PendingIntent.getService(
    context,
    appWidgetId * 1000,
    Intent(context, MusicPlayerService::class.java).apply {
        action = MusicPlayerService.ACTION_PLAY_SONG_INDEX
    },
    itemClickFlags
)

// In remoteViews.apply block, add these lines:
setRemoteAdapter(R.id.widget_song_list, adapterIntent)
setPendingIntentTemplate(R.id.widget_song_list, itemClickTemplate)

// After appWidgetManager.updateAppWidget(), add:
appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_song_list)

// Add this helper function in companion object:
private fun buildCopyLogIntent(context: Context): PendingIntent {
    val intent = Intent(context, WidgetCopyLogReceiver::class.java).apply {
        action = WidgetCopyLogReceiver.ACTION_COPY_LOG
    }
    return PendingIntent.getBroadcast(
        context, 2, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

// In remoteViews.apply block, add:
setOnClickPendingIntent(R.id.widget_copy_log, buildCopyLogIntent(context))
*/

// === ASSOCIATED FILES ===
// WidgetSongListService.kt - handles the RemoteViewsService binding
// WidgetSongListFactory.kt - factory for creating RemoteViews for each list item
// These can be kept as-is but are not called until the ListView binding is restored
