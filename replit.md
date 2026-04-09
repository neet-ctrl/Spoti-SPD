# SPD - Spotify Playlist Downloader

An advanced Android app that downloads Spotify playlists using NewPipe Extractor and FFmpeg for audio conversion.

## Build

This is an Android project built with Kotlin + Jetpack Compose. Actual APK build happens via GitHub Actions (not in Replit). The workflow here just shows project info.

## Architecture

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Key Components

### Screens
- `HomeScreen.kt` — Main download screen; Spotify-inspired gradient hero, song list with swipe gestures
- `HistoryScreen.kt` — Download history; card-based layout, tap-to-open folder in file manager, expandable songs list
- `LibraryScreen.kt` — Local music library; auto-scans download folder, animated stats header, search/sort, swipe-to-delete, favorites, connects to existing media player
- `PreferencesScreen.kt` — Settings; folder picker, auto-update toggle, about info

### Components
- `SpotifyDialog.kt` — WebView-based playlist scraper dialog
- `PermissionDialog.kt` — Storage permission request dialog (modern style)
- `UpdateDialog.kt` — App update dialog (modern style)
- `TopBar.kt` — App bar with SPD logo circle
- `BottomBar.kt` — Bottom nav with animated SpotifyGreen indicator (4 tabs: Home, History, Library, Settings)
- `FloatingMusicPlayer.kt` — Extracted shared floating player rendered at MainActivity level (persists across all tabs)

### Services
- `MusicPlayerService.kt` — Foreground service with MediaSession + MediaStyle notification (shuffle/prev/seek-back/play-pause/seek-fwd/next/repeat/favorite/close, ACTION_PLAY_SONG_INDEX for widget)
- `MusicPlayerWidgetProvider.kt` — Home screen widget provider; syncs with Library screen, song list ListView via RemoteViewsService, all controls, refresh
- `WidgetSongListService.kt` — RemoteViewsService powering the widget's embedded song ListView with playing state highlight

### ViewModels
- `HomeScreenViewModel.kt` — Manages all download state, scraping, media playback, notification bridge via BroadcastReceiver

### Utils
- `DownloadManager.kt` — NewPipe-based audio download, FFmpeg MP3 conversion, ZIP creation
- `SpotifyManager.kt` — JavaScript injection for scraping Spotify track data
- `DownloadHistoryManager.kt` — Persists download history via SharedPreferences JSON
- `SharedPref.kt` — Settings storage (download path, auto-update toggle)
- `PermissionUtils.kt` — Storage permission helpers

## Design System

- **Primary accent**: `#1DB954` (SpotifyGreen)
- **Dark background**: `#101410`
- **Light background**: `#F6FBF7`
- **Cards**: `RoundedCornerShape(20.dp)` throughout
- **Typography**: Material 3 type scale with custom weight overrides

## Features

- Scrape Spotify playlists/albums/tracks via embedded WebView
- Download individual songs or entire playlists
- ZIP archive creation for batch downloads
- MP3 conversion via FFmpeg
- Preview playback with full controls (seek, volume, shuffle, repeat, favorite)
- Notification-based player with background playback (foreground service)
- Download history with folder opening, song counts, success rates
- Crash recovery dialog with copy-to-clipboard stacktrace
- Auto-update checker

## Recent Changes (Widget & Notification Advanced Redesign)

- Full Spotify-inspired UI overhaul across all screens
- MusicPlayerService (foreground service) with MediaStyle notification
- BroadcastReceiver integration in ViewModel for notification button actions
- Floating bottom-sheet music player (collapsible, full controls)
- History screen: working tap-to-open folder (DocumentsContract), song counts, progress bars
- Preferences: section headers, modern settings cards
- All dialogs modernized (Permission, Update, Crash, SpotifyDialog)
- Android 13+ notification permission request in MainActivity
- stopForeground API compatibility fix for all Android versions

## Widget & Notification Overhaul (Latest)

### Widget (`widget_music_player.xml` + `MusicPlayerWidgetProvider.kt` + `WidgetSongListService.kt`)
- Deep dark gradient background (#1A1A2E → #0D0D1A) with rounded corners
- Custom Material Design vector icons for all controls (play, pause, skip, shuffle, repeat, favorite, refresh, etc.)
- Circular control button backgrounds (green for play, dark for secondary)
- Green accent tint for active shuffle/repeat states
- Album art area (music note icon with green circle background)
- Song title + artist row with Spotify-green artist label
- Dual time labels + slim progress bar (3dp green on dark)
- Full control row: Shuffle | Seek-10s | Prev | Play/Pause(circle) | Next | Seek+10s | Repeat
- Embedded ListView showing entire Library song list (scrollable)
- Currently playing song highlighted in green with border + green icon
- Tap any song item → plays that song (works even when app is closed)
- Refresh button syncs new songs from Library
- Widget size: min 280×300dp, resizable down to 280×200dp

### Notification (`notification_music_player.xml` + `MusicPlayerService.kt`)
- Deep dark background (#1A1A2E)
- Album art icon (48dp, green circle)
- Title (bold white) + Artist (Spotify green) + subheader row
- Dual time labels + slim green progress bar
- Full controls row: Shuffle | Seek-10s | Prev | Play/Pause(56dp circle) | Next | Seek+10s | Repeat
- Favorite + Close buttons on right side
- Footer status line ("Now Playing • Library" / "Paused • Library")
- MediaSession integration for lockscreen / quick-settings media card
- MediaStyle compact view shows Prev/Play-Pause/Next in notification shade
- Full custom big content view shown when expanded

### Data Sync
- `LibraryViewModel` saves full song list as JSON to SharedPreferences on every scan
- `WidgetSongListFactory` reads JSON, provides RemoteViews for each song
- `HomeScreenViewModel` saves current playing index to SharedPreferences for widget highlight
- Widget notifies data change on every player update
