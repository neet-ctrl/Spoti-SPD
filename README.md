# 🎵 Spotify Playlist Downloader

<div align="center">

![Version](https://img.shields.io/badge/version-1.10.3-blue.svg)
![Android](https://img.shields.io/badge/Android-7.0+-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)
![Material 3](https://img.shields.io/badge/Material-3-orange.svg)
![License](https://img.shields.io/badge/License-MIT-red.svg)

*A powerful, modern Android application for downloading Spotify playlists, albums, and tracks with advanced interactive features and beautiful Material 3 design.*

[📱 Download APK](#installation) • [📖 Features](#features) • [🚀 Quick Start](#quick-start) • [📚 Documentation](#documentation)

</div>

---

## ✨ Features

### 🎵 Core Functionality

#### 🔗 Universal Link Support
- **Spotify Playlists**: Download entire playlists with all tracks
- **Spotify Albums**: Extract and download complete albums
- **Individual Tracks**: Download single songs
- **Smart URL Handling**: Automatically processes Spotify deep links and web URLs
- **Intent Integration**: Handles links shared from Spotify app

#### 📊 Metadata Extraction
- **Real-time Scraping**: Visual progress indicator during extraction
- **Complete Metadata**: Song titles, artists, albums, duration
- **Batch Processing**: Handles large playlists efficiently
- **Error Recovery**: Robust handling of network issues and invalid links

### 🎨 Advanced UI/UX

#### 🎯 Interactive Song Management
- **Song List Display**: Beautiful card-based layout with metadata
- **Selection Mode**: Long-press any song to enter multi-select mode
- **Select All**: One-tap selection of all songs in playlist
- **Swipe Gestures**:
  - **Swipe Left**: Instantly download individual song
  - **Swipe Right**: Select song for batch operations
- **Play Buttons**: Mini play button next to each song for preview

#### 🎼 Floating Music Player
- **Popup Player**: Full-screen floating music player
- **Rich Controls**:
  - Play/Pause with large floating action button
  - Next/Previous track navigation
  - Shuffle mode toggle
  - Playlist continuation
- **Song Metadata**: Displays current song title, artist, album
- **Floatable Panel**: Can be collapsed to screen edge for multitasking
- **Persistent Playback**: Continues playing while navigating app

#### 📥 Smart Download System

##### Individual Downloads
- **One-tap Download**: Swipe left or tap download button
- **Progress Tracking**: Real-time download progress with file size
- **Location Links**: Clickable destination path opens file location
- **Format Options**: MP3 conversion for compatibility

##### Batch Downloads
- **Multi-select Interface**: Select multiple songs with checkboxes
- **Download Dialog**: Advanced popup with options:
  - **Default Location**: Shows current download folder
  - **Change Location**: System file picker for custom paths
  - **Song List**: Collapsible list with serial numbers
  - **Count Display**: Shows selected song count
  - **Dual Options**:
    - **Download Each**: Individual files with progress tracking
    - **Download as ZIP**: Single compressed archive

#### 📊 Progress & History

##### Real-time Progress Bars
- **Individual File Progress**: Current download with percentage
- **Overall Progress**: Batch completion status
- **File Location Links**: Tap to open download folder in file manager
- **Visual Indicators**: Material 3 progress bars with animations

##### Download History
- **Persistent History**: All downloads saved with metadata
- **Success Tracking**: Success/failure status for each download
- **Location Links**: Direct access to downloaded files
- **File Management**: Delete history entries, view file details

### 🛠️ Technical Excellence

#### 🎨 Material 3 Design
- **Dynamic Theming**: Automatic light/dark mode switching
- **Consistent Components**: Cards, buttons, dialogs following M3 guidelines
- **Smooth Animations**: Fade-in/out, expand/collapse, swipe animations
- **Responsive Layout**: Optimized for phones and tablets

#### ⚡ Performance Features
- **Background Processing**: Non-blocking downloads with cancellation
- **Memory Efficient**: Lazy loading for large playlists
- **Coroutine-based**: Modern async programming with Kotlin Flow
- **Error Handling**: Comprehensive error recovery and retry mechanisms

#### 🔧 Customization
- **Download Location**: Customizable storage paths
- **Format Preferences**: MP3 conversion toggle
- **Clipboard Integration**: Direct paste from Spotify app
- **Permission Management**: Automatic storage permission handling

---

## 🚀 Quick Start

### Basic Usage

1. **Launch App** → Paste Spotify link or use clipboard
2. **Tap Scrape** → View extracted songs with metadata
3. **Choose Download Method**:
   - **Quick Download**: Swipe left on songs
   - **Batch Download**: Long-press → Select → Download options
4. **Monitor Progress** → Tap location links to access files
5. **Enjoy Music** → Use play buttons for instant preview

### Advanced Workflow

```
Spotify Link → Scrape → Song List → Select/Play/Swipe → Download → Access Files
```

---

## 📱 Screenshots & UI Guide

### Main Interface
- **Header Card**: App branding with feature summary
- **Input Section**: URL field with paste button and MP3 toggle
- **Action Buttons**: Scrape and Download All buttons
- **Song List**: Expandable cards with metadata and controls

### Interactive Features
- **Selection Mode**: Long-press activates multi-select with checkboxes
- **Swipe Actions**: Left = Download, Right = Select
- **Play Controls**: Mini play buttons for each song
- **Progress Cards**: Real-time download tracking with location links

### Dialogs & Popups
- **Download Dialog**: Location picker, song list, ZIP options
- **Music Player**: Floating controls with shuffle and navigation
- **Permission Dialogs**: Storage and network access prompts

---

## 📚 Documentation

### 🎵 Music Player Features

#### Floating Player Controls
```
┌─────────────────┐
│  ♪ Song Title   │
│  Artist Name    │
├─────────────────┤
│  [⏮] [▶] [⏭]   │
│                 │
│  ⏯️ Shuffle      │
│                 │
│  [Close Player] │
└─────────────────┘
```

#### Playback Modes
- **Normal Playback**: Sequential playlist playback
- **Shuffle Mode**: Random song selection
- **Loop Options**: Single song or playlist repeat
- **Background Play**: Continues when app minimized

### 📥 Download System

#### File Organization
```
Download Location/
├── Song Title - Artist.mp3
├── Another Song - Artist.mp3
└── Playlist Name.zip (optional)
```

#### Progress Tracking
- **File Progress**: Individual download percentage
- **Batch Progress**: Overall completion status
- **Speed Indicators**: Download speed and ETA
- **Error Handling**: Failed downloads with retry options

### 🎯 Gesture Guide

#### Song List Gestures
- **Tap**: Select in selection mode
- **Long Press**: Enter/exit selection mode
- **Swipe Left**: Download song immediately
- **Swipe Right**: Select song for batch
- **Play Button**: Open floating music player

#### Player Gestures
- **Tap Controls**: Play/pause, next/previous
- **Swipe Down**: Collapse player to mini mode
- **Swipe Up**: Expand to full player
- **Drag**: Move floating player position

---

## 🛠️ Installation

### Prerequisites
- **Android Version**: 7.0 (API 24) or higher
- **Storage**: 100MB free space for app + downloads
- **Network**: Stable internet for downloads
- **Permissions**: Storage and internet access

### Download Options

#### Option 1: Pre-built APK (Recommended)
1. Go to [GitHub Releases](https://github.com/supersu-man/spotify-playlist-downloader/releases)
2. Download `app-debug.apk` (latest version)
3. Enable "Install from Unknown Sources" in Android settings
4. Install and launch the app

#### Option 2: GitHub Actions Artifact
1. Visit [Actions Tab](https://github.com/supersu-man/spotify-playlist-downloader/actions)
2. Select latest successful workflow
3. Download debug APK from artifacts
4. Install on your device

#### Option 3: Build from Source
```bash
# Clone repository
git clone https://github.com/supersu-man/spotify-playlist-downloader.git
cd spotify-playlist-downloader

# Open in Android Studio
# Build → Make Project
# Run → Run 'app'
```

### API Configuration (Optional)
For enhanced YouTube Music integration:
1. Visit [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Create new app → Get Client ID & Secret
3. Add to `app/src/main/res/values/secrets.xml`:
```xml
<resources>
    <string name="CLIENT_ID">your_client_id</string>
    <string name="CLIENT_SECRET">your_client_secret</string>
</resources>
```

---

## 📖 Detailed Usage Guide

### Step-by-Step Workflow

#### 1. Getting Spotify Links
- **From Spotify App**: Share → Copy Link
- **From Web Player**: Copy URL from browser
- **Deep Links**: `spotify:playlist:xxx` or `spotify:album:xxx`
- **Web URLs**: `https://open.spotify.com/playlist/xxx`

#### 2. Scraping Process
- Paste link in input field
- Toggle MP3 conversion if desired
- Tap "Scrape" button
- Wait for metadata extraction
- View song list with titles and artists

#### 3. Song Management
- **Quick Actions**:
  - Swipe left on song → Instant download
  - Tap play button → Preview in floating player
- **Batch Operations**:
  - Long-press song → Enter selection mode
  - Tap "Select All" → Select entire playlist
  - Tap "Download" → Open download dialog

#### 4. Download Options
- **Individual Downloads**: Progress shown per file
- **ZIP Downloads**: All songs in single archive
- **Location Selection**: Choose custom download folder
- **Progress Monitoring**: Real-time updates with location links

#### 5. File Access
- **Progress Cards**: Tap location link → Open file manager
- **History Tab**: View all downloads with file paths
- **Direct Access**: Files saved to selected location

### Advanced Features Guide

#### Music Player Usage
1. Tap play button next to any song
2. Floating player appears with controls
3. Use navigation buttons for playlist control
4. Toggle shuffle for random playback
5. Close player when done

#### Selection Mode Guide
1. Long-press any song to activate
2. Checkboxes appear next to all songs
3. Tap songs to select/deselect
4. Use "Select All" for entire list
5. Tap "Download" for batch options
6. Tap "Cancel" to exit selection mode

#### Download Dialog Features
- **Location Display**: Shows current download path
- **Change Button**: Opens system file picker
- **Song Count**: Shows number of selected songs
- **Expandable List**: View all selected songs with numbers
- **Download Options**:
  - **Download Each**: Individual files
  - **Download as ZIP**: Single compressed file

---

## 🏗️ Architecture & Technical Details

### Tech Stack
```
├── Language: Kotlin 1.9+
├── UI: Jetpack Compose + Material 3
├── Architecture: MVVM Pattern
├── Async: Kotlin Coroutines + Flow
├── HTTP: OkHttp3
├── Audio: FFmpeg Kit
├── Storage: SharedPreferences + JSON
├── Build: Gradle Kotlin DSL
└── CI/CD: GitHub Actions
```

### Project Structure
```
spotify-playlist-downloader/
├── app/
│   ├── build.gradle.kts          # App configuration
│   ├── proguard-rules.pro        # Obfuscation rules
│   └── src/main/
│       ├── AndroidManifest.xml   # App permissions
│       ├── java/dev/sumanth/spd/
│       │   ├── MainActivity.kt   # App entry point
│       │   ├── model/            # Data models
│       │   ├── ui/
│       │   │   ├── component/    # Reusable components
│       │   │   ├── screen/       # Screen composables
│       │   │   ├── theme/        # Material 3 theming
│       │   │   └── viewmodel/    # Business logic
│       │   └── utils/            # Helper utilities
│       └── res/                  # Resources & assets
├── build.gradle.kts              # Project build config
├── settings.gradle.kts           # Project settings
└── gradle/                       # Build tools
```

### Key Components

#### ViewModels
- **HomeScreenViewModel**: Main UI state management
- **UpdaterViewModel**: App update checking
- State management for downloads, selection, player

#### Utilities
- **Downloader**: Core download logic with FFmpeg
- **Spotify**: Web scraping and metadata extraction
- **SharedPreference**: Persistent storage management
- **Permission**: Android permission handling

#### UI Components
- **SpotifyDialog**: Scraping progress dialog
- **DownloadDialog**: Batch download options
- **MusicPlayerDialog**: Floating audio player
- **Progress Cards**: Download tracking components

---

## 🔧 Configuration & Customization

### Download Settings
- **Default Location**: `/storage/emulated/0/Download/Spotify Downloads/`
- **Format Options**: MP3 conversion toggle
- **Quality Settings**: Highest available bitrate
- **Concurrent Downloads**: Single file at a time

### App Preferences
- **Theme Mode**: System/Auto light/dark
- **Notification Settings**: Download completion alerts
- **Storage Permissions**: Automatic permission requests
- **Cache Management**: Temporary file cleanup

### Advanced Configuration
```kotlin
// Custom download location
sharedPref.setDownloadPath("/custom/path/")

// MP3 conversion settings
viewModel.convertToMp3 = true

// Player preferences
viewModel.isShuffleMode = false
```

---

## 🐛 Troubleshooting

### Common Issues & Solutions

#### "Webpage not available" Error
- **Cause**: Invalid Spotify link or network issues
- **Solution**: Verify link is accessible, check internet connection
- **Alternative**: Try different Spotify link format

#### Download Failures
- **Cause**: Storage permissions or network timeout
- **Solution**:
  - Grant storage permissions in app settings
  - Check available storage space
  - Retry download or use different network

#### App Crashes
- **Cause**: Memory issues or corrupted data
- **Solution**:
  - Clear app cache and data
  - Restart device
  - Reinstall app from clean APK

#### Player Issues
- **Cause**: Audio codec or file format problems
- **Solution**: Ensure MP3 conversion is enabled
- **Alternative**: Check file integrity in download location

### Debug Information
- **Version**: Check app version in settings
- **Logs**: Enable debug logging for detailed error info
- **Device Info**: Android version and device model
- **Network**: Connection type and speed

### Recovery Steps
1. **Soft Reset**: Clear app cache
2. **Hard Reset**: Clear app data (loses history)
3. **Clean Install**: Uninstall → Restart → Reinstall
4. **System Check**: Verify Android version compatibility

---

## 🤝 Contributing

### Development Setup
1. **Fork Repository**: Create your fork on GitHub
2. **Clone Locally**: `git clone your-fork-url`
3. **Open in Android Studio**: Import Gradle project
4. **Create Branch**: `git checkout -b feature-name`
5. **Make Changes**: Follow coding guidelines
6. **Test Thoroughly**: Multiple devices and scenarios
7. **Commit Changes**: `git commit -am "Add feature"`
8. **Push Branch**: `git push origin feature-name`
9. **Create PR**: Submit pull request with description

### Coding Guidelines
- **Kotlin Style**: Follow official Kotlin conventions
- **Material 3**: Use M3 components and theming
- **Error Handling**: Comprehensive try-catch blocks
- **Documentation**: KDoc comments for public APIs
- **Testing**: Unit tests for business logic

### Feature Requests
- **Issue Template**: Use GitHub issue templates
- **Detailed Description**: Include screenshots/mockups
- **Use Cases**: Explain real-world scenarios
- **Priority Level**: Low/Medium/High/Critical

---

## 📄 License & Legal

### MIT License
```
Copyright (c) 2024 Sumanth

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

### Disclaimer
- **Educational Purpose**: This app is for learning and personal use only
- **Copyright Respect**: Users must respect music copyright laws
- **Spotify Terms**: Comply with Spotify's Terms of Service
- **Legal Responsibility**: Developers not liable for misuse
- **DMCA Compliance**: Remove content upon takedown requests

---

## 🙏 Acknowledgments

### Core Dependencies
- **[NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor)**: YouTube Music integration
- **[FFmpeg Kit](https://github.com/arthenica/ffmpeg-kit)**: Audio processing and conversion
- **[OkHttp](https://github.com/square/okhttp)**: HTTP client for API calls
- **[Material Components](https://material.io)**: Google's design system

### Development Tools
- **Android Studio**: Official IDE for Android development
- **Gradle**: Build automation and dependency management
- **GitHub Actions**: CI/CD pipeline for automated builds
- **Kotlin**: Modern programming language

### Community & Inspiration
- **Open Source Community**: Contributors to core libraries
- **Android Developers**: Documentation and best practices
- **Material Design**: Design system and guidelines
- **Spotify API**: Music metadata and integration

---

## 📞 Support & Contact

### Getting Help
- **GitHub Issues**: Bug reports and feature requests
- **Discussions**: General questions and community support
- **Documentation**: Comprehensive usage guides
- **Troubleshooting**: Self-service problem resolution

### Contact Information
- **Developer**: Sumanth
- **GitHub**: [@supersu-man](https://github.com/supersu-man)
- **Repository**: [spotify-playlist-downloader](https://github.com/supersu-man/spotify-playlist-downloader)
- **Issues**: [GitHub Issues](https://github.com/supersu-man/spotify-playlist-downloader/issues)

### Version Information
- **Current Version**: 1.10.3
- **Last Updated**: April 2026
- **Min Android**: API 24 (Android 7.0)
- **Target Android**: API 36 (Android 16)
- **Architecture**: ARM64, ARM32, x86_64

---

<div align="center">

**Made with ❤️ for Android users who love Spotify**

*Download • Scrape • Enjoy*

</div>

### Advanced Features

#### Music Player
- Tap the play button next to any song
- Floating player appears with controls:
  - Play/Pause
  - Next/Previous track
  - Shuffle mode
  - Playlist continuation
  - Collapsible panel

#### Download History
- View all previous downloads
- Tap file location links to open in file manager
- See success rates and metadata
- Delete individual history entries

#### Custom Download Location
- Go to Settings > Download Location
- Tap to change folder using system file picker
- App remembers your preference

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Async Processing**: Kotlin Coroutines
- **HTTP Client**: OkHttp3
- **Audio Processing**: FFmpeg Kit
- **Data Persistence**: SharedPreferences + JSON serialization

### Project Structure
```
app/src/main/java/dev/sumanth/spd/
├── MainActivity.kt              # Main activity and navigation
├── model/                       # Data models
│   ├── DownloadHistory.kt       # Download history models
│   └── Navigation.kt            # Navigation state
├── ui/
│   ├── component/               # Reusable UI components
│   │   ├── Background.kt        # Background elements
│   │   ├── BottomBar.kt         # Navigation bar
│   │   ├── PermissionDialog.kt  # Permission prompts
│   │   ├── SpotifyDialog.kt     # Scraping dialog
│   │   └── TopBar.kt            # Top app bar
│   ├── screen/                  # Screen composables
│   │   ├── HomeScreen.kt        # Main download screen
│   │   ├── HistoryScreen.kt     # Download history
│   │   └── PreferencesScreen.kt # Settings screen
│   ├── theme/                   # Theming
│   │   ├── Color.kt             # Color palette
│   │   └── Theme.kt             # Theme configuration
│   └── viewmodel/               # ViewModels
│       ├── HomeScreenViewModel.kt
│       └── UpdaterViewModel.kt
└── utils/                       # Utilities
    ├── Downloader.kt            # Download logic
    ├── Permission.kt            # Permission handling
    ├── SharedPreference.kt      # Preferences management
    └── Spotify.kt               # Spotify integration
```

## API Keys Setup

The app requires Spotify API credentials for YouTube Music search functionality:

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Create a new app
3. Copy Client ID and Client Secret
4. Add them to `secrets.xml` as shown above

## Permissions

The app requires the following permissions:
- `INTERNET`: For downloading songs and API calls
- `READ_EXTERNAL_STORAGE`: For accessing downloaded files
- `WRITE_EXTERNAL_STORAGE`: For saving downloaded files
- `ACCESS_NETWORK_STATE`: For network connectivity checks

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and test thoroughly
4. Commit your changes: `git commit -am 'Add feature'`
5. Push to the branch: `git push origin feature-name`
6. Submit a pull request

### Development Guidelines
- Follow Kotlin coding conventions
- Use meaningful commit messages
- Test on multiple Android versions
- Ensure Material 3 compliance
- Handle edge cases and errors gracefully

## Troubleshooting

### Common Issues

**"Webpage not available" error:**
- Ensure you have a stable internet connection
- Check that the Spotify link is valid and accessible
- Try refreshing the page or using a different link

**Download fails:**
- Verify storage permissions are granted
- Check available storage space
- Ensure the download location is writable

**App crashes:**
- Clear app data and cache
- Reinstall the app
- Check device compatibility (Android 7.0+)

### Debug Mode
Enable debug logging by setting `BuildConfig.DEBUG` to true in build.gradle.kts.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

This app is for educational purposes only. Please respect copyright laws and Spotify's terms of service. The developers are not responsible for any misuse of this application.

## Acknowledgments

- [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) for YouTube integration
- [FFmpeg Kit](https://github.com/arthenica/ffmpeg-kit) for audio processing
- Material 3 design system by Google
- Spotify for providing the music platform

---

**Version**: 1.10.3  
**Last Updated**: April 2026  
**Developed by**: Sumanth

