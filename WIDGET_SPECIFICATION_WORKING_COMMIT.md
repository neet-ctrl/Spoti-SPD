# WIDGET SPECIFICATION - WORKING COMMIT 55cf1a7
# Exact implementation that worked without "can't load" error

## WIDGET OVERVIEW
- **Type:** AppWidgetProvider with RemoteViews
- **Layout:** LinearLayout with vertical orientation
- **Background:** Dark theme (#121212)
- **Padding:** 12dp
- **Size:** Match parent width/height

## UI COMPONENTS & LAYOUT STRUCTURE

### 1. HEADER SECTION (Horizontal LinearLayout)
- **Height:** wrap_content
- **Padding Bottom:** 8dp
- **Gravity:** center_vertical

#### Player Icon (ImageView)
- **ID:** N/A (static icon)
- **Size:** 32dp x 32dp
- **Source:** @mipmap/ic_launcher
- **Content Description:** "Player icon"

#### Header Text (TextView)
- **ID:** widget_header
- **Width:** 0dp (weight=1)
- **Text:** "Library Player"
- **Color:** #1DB954 (Spotify green)
- **Size:** 14sp
- **Style:** bold
- **Padding Start:** 10dp

#### Refresh Button (ImageView)
- **ID:** widget_refresh
- **Size:** 32dp x 32dp
- **Padding:** 6dp
- **Source:** @android:drawable/ic_popup_sync
- **Tint:** #FFFFFF
- **Content Description:** "Refresh"

### 2. SONG INFO SECTION

#### Title (TextView)
- **ID:** widget_title
- **Width:** match_parent
- **Text:** "No song selected" (default)
- **Color:** #FFFFFF
- **Size:** 15sp
- **Style:** bold
- **Max Lines:** 1
- **Ellipsize:** end

#### Artist (TextView)
- **ID:** widget_artist
- **Width:** match_parent
- **Text:** "Artist" (default)
- **Color:** #B3B3B3
- **Size:** 13sp
- **Max Lines:** 1
- **Ellipsize:** end
- **Padding Bottom:** 12dp

### 3. TIME DISPLAY SECTION (Horizontal LinearLayout)
- **Width:** match_parent
- **Height:** wrap_content
- **Orientation:** horizontal
- **Gravity:** center_vertical
- **Padding Bottom:** 4dp

#### Current Time (TextView)
- **ID:** widget_current_time
- **Width:** wrap_content
- **Text:** "0:00"
- **Color:** #B3B3B3
- **Size:** 12sp

#### Total Duration (TextView)
- **ID:** widget_total_duration
- **Width:** wrap_content
- **Text:** "0:00"
- **Color:** #B3B3B3
- **Size:** 12sp

### 4. PROGRESS BAR
- **ID:** widget_progress
- **Style:** @android:style/Widget.ProgressBar.Horizontal
- **Width:** match_parent
- **Height:** 4dp
- **Progress:** 0 (default)
- **Max:** 100
- **Indeterminate:** false
- **Progress Tint:** #1DB954
- **Background Tint:** #3E3E3E
- **Padding Bottom:** 12dp

### 5. CONTROL BUTTONS SECTION (Horizontal LinearLayout)
- **Width:** match_parent
- **Height:** wrap_content
- **Orientation:** horizontal
- **Gravity:** center
- **Padding Bottom:** 10dp

#### Shuffle Button (ImageView)
- **ID:** widget_shuffle
- **Size:** 36dp x 36dp
- **Padding:** 8dp
- **Source:** @android:drawable/ic_menu_rotate
- **Tint:** #FFFFFF
- **Content Description:** "Shuffle"

#### Previous Button (ImageView)
- **ID:** widget_prev
- **Size:** 36dp x 36dp
- **Padding:** 8dp
- **Source:** @android:drawable/ic_media_previous
- **Tint:** #FFFFFF
- **Content Description:** "Previous"

#### Play/Pause Button (ImageView)
- **ID:** widget_play_pause
- **Size:** 50dp x 50dp
- **Padding:** 10dp
- **Source:** @android:drawable/ic_media_play (default)
- **Tint:** #1DB954
- **Content Description:** "Play/Pause"

#### Next Button (ImageView)
- **ID:** widget_next
- **Size:** 36dp x 36dp
- **Padding:** 8dp
- **Source:** @android:drawable/ic_media_next
- **Tint:** #FFFFFF
- **Content Description:** "Next"

#### Repeat Button (ImageView)
- **ID:** widget_repeat
- **Size:** 36dp x 36dp
- **Padding:** 8dp
- **Source:** @android:drawable/ic_menu_rotate
- **Tint:** #FFFFFF
- **Content Description:** "Repeat"

### 6. BOTTOM SECTION (Horizontal LinearLayout)
- **Width:** match_parent
- **Height:** wrap_content
- **Orientation:** horizontal
- **Gravity:** center_vertical

#### Favorite Button (ImageView)
- **ID:** widget_favorite
- **Size:** 28dp x 28dp
- **Padding:** 4dp
- **Source:** @android:drawable/btn_star_big_off (default)
- **Tint:** #FFFFFF
- **Content Description:** "Favorite"

#### Status Text (TextView)
- **ID:** widget_status
- **Width:** 0dp (weight=1)
- **Text:** "Library mode" (default)
- **Color:** #808080
- **Size:** 11sp
- **Padding Start:** 10dp
- **Max Lines:** 1
- **Ellipsize:** end

## FUNCTIONALITY & BEHAVIOR

### Widget Provider Class: MusicPlayerWidgetProvider

#### Companion Object Constants
```kotlin
private const val PREFS_NAME = "player_widget_prefs"
private const val KEY_TITLE = "title"
private const val KEY_ARTIST = "artist"
private const val KEY_CURRENT_TIME = "current_time"
private const val KEY_DURATION = "duration"
private const val KEY_IS_PLAYING = "is_playing"
private const val KEY_IS_LOADING = "is_loading"
private const val KEY_IS_SHUFFLE = "is_shuffle"
private const val KEY_REPEAT_MODE = "repeat_mode"
private const val KEY_IS_FAVORITE = "is_favorite"

const val ACTION_REFRESH_LIBRARY = "dev.sumanth.spd.ACTION_REFRESH_LIBRARY"
const val ACTION_OPEN_LIBRARY = "dev.sumanth.spd.ACTION_OPEN_LIBRARY"
const val EXTRA_REFRESH_LIBRARY = "extra_refresh_library"
```

#### Static Methods
- `updateAllWidgets(context: Context)`: Updates all widget instances
- `updateAppWidget(context, appWidgetManager, appWidgetId)`: Updates single widget

#### Instance Methods
- `onUpdate()`: Called when widget needs update
- `onReceive()`: Handles broadcast intents

### Intent Building Functions

#### buildControlIntent(context, action)
- **Type:** PendingIntent.getService()
- **Target:** MusicPlayerService.class
- **Action:** Various control actions
- **Flags:** FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
- **Request Code:** action.hashCode()

#### buildRefreshIntent(context)
- **Type:** PendingIntent.getBroadcast()
- **Target:** MusicPlayerWidgetProvider.class
- **Action:** ACTION_REFRESH_LIBRARY
- **Flags:** FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE
- **Request Code:** 0

#### buildOpenLibraryIntent(context, refresh)
- **Type:** PendingIntent.getActivity()
- **Target:** MainActivity.class
- **Action:** ACTION_OPEN_LIBRARY
- **Extras:** EXTRA_REFRESH_LIBRARY = refresh
- **Flags:** FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_NEW_TASK
- **Request Code:** 1

### Service Communication
- **Method:** Direct service intents (PendingIntent.getService)
- **Service Response:** sendBroadcast() (not LocalBroadcastManager)
- **Actions Handled:**
  - ACTION_PLAY_PAUSE
  - ACTION_NEXT
  - ACTION_PREV
  - ACTION_SHUFFLE
  - ACTION_REPEAT
  - ACTION_TOGGLE_FAVORITE

### Visual States

#### Play/Pause Button
- **Playing:** ic_media_pause
- **Paused:** ic_media_play

#### Shuffle Button
- **Enabled:** ic_menu_rotate (different visual treatment)
- **Disabled:** ic_media_rew

#### Repeat Button
- **One:** ic_popup_sync
- **All:** ic_menu_rotate
- **Off:** ic_menu_rotate (different visual treatment)

#### Favorite Button
- **Favorited:** btn_star_big_on
- **Not Favorited:** btn_star_big_off

### Data Storage
- **Method:** SharedPreferences
- **File:** "player_widget_prefs"
- **Keys:** See constants above
- **Types:** String, Float, Boolean, Int

### Click Handlers
- All buttons use setOnClickPendingIntent()
- Root layout opens library (buildOpenLibraryIntent)
- Refresh button triggers library refresh
- Control buttons send service intents

## DEPENDENCIES & IMPORTS
```kotlin
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.sumanth.spd.MainActivity
import dev.sumanth.spd.R
import dev.sumanth.spd.service.MusicPlayerService
```

## MANIFEST REQUIREMENTS
```xml
<receiver
    android:name=".service.MusicPlayerWidgetProvider"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        <action android:name="dev.sumanth.spd.ACTION_REFRESH_LIBRARY" />
        <action android:name="dev.sumanth.spd.ACTION_OPEN_LIBRARY" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/music_player_widget_info" />
</receiver>
```

## WIDGET INFO XML (music_player_widget_info.xml)
- **minWidth:** 250dp
- **minHeight:** 110dp
- **updatePeriodMillis:** 0 (manual updates only)
- **initialLayout:** @layout/widget_music_player
- **configure:** (none)
- **resizeMode:** horizontal|vertical
- **widgetCategory:** home_screen

## TESTING CHECKLIST
- [ ] Widget loads without "can't load" error
- [ ] All buttons are clickable and responsive
- [ ] Play/pause button changes icon correctly
- [ ] Progress bar updates with song progress
- [ ] Time displays update correctly
- [ ] Shuffle/repeat/favorite states update visually
- [ ] Refresh button triggers library refresh
- [ ] Root tap opens main activity
- [ ] Widget survives device rotation
- [ ] Multiple widget instances work independently</content>
<parameter name="filePath">/workspaces/spoti/WIDGET_SPECIFICATION_WORKING_COMMIT.md