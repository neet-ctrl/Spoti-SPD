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
- `HomeScreen.kt` — Main download screen; Spotify-inspired gradient hero, song list with swipe gestures, floating bottom-sheet music player
- `HistoryScreen.kt` — Download history; card-based layout, tap-to-open folder in file manager, expandable songs list
- `PreferencesScreen.kt` — Settings; folder picker, auto-update toggle, about info

### Components
- `SpotifyDialog.kt` — WebView-based playlist scraper dialog
- `PermissionDialog.kt` — Storage permission request dialog (modern style)
- `UpdateDialog.kt` — App update dialog (modern style)
- `TopBar.kt` — App bar with SPD logo circle
- `BottomBar.kt` — Bottom nav with animated SpotifyGreen indicator

### Services
- `MusicPlayerService.kt` — Foreground service for music player notification (MediaStyle, prev/play-pause/next/close actions)

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

## Recent Changes (Advanced Redesign)

- Full Spotify-inspired UI overhaul across all screens
- MusicPlayerService (foreground service) with MediaStyle notification
- BroadcastReceiver integration in ViewModel for notification button actions
- Floating bottom-sheet music player (collapsible, full controls)
- History screen: working tap-to-open folder (DocumentsContract), song counts, progress bars
- Preferences: section headers, modern settings cards
- All dialogs modernized (Permission, Update, Crash, SpotifyDialog)
- Android 13+ notification permission request in MainActivity
- stopForeground API compatibility fix for all Android versions
