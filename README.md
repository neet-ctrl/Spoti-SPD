# 🎵 SPD - Spotify Playlist Downloader

<div align="center">

![Version](https://img.shields.io/badge/version-1.10.3-blue.svg)
![Android](https://img.shields.io/badge/Android-7.0+-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)
![Material 3](https://img.shields.io/badge/Material-3-orange.svg)
![License](https://img.shields.io/badge/License-MIT-red.svg)

*A modern Android application for downloading Spotify playlists, albums, and tracks with beautiful Material 3 design and built-in music player.*

[📱 Download APK](#installation) • [📖 Features](#features) • [🚀 Quick Start](#quick-start) • [⚙️ Settings](#settings)

</div>

---

## ✨ Features

### 🎵 Core Functionality

- **Spotify Links Support**: Download playlists, albums, and individual tracks from Spotify
- **URL Processing**: Handles Spotify web links, deep links, and shared content
- **Metadata Extraction**: Automatically scrapes song titles, artists, and albums
- **Smart Scraping**: Visual progress indicator during extraction process
- **Batch Processing**: Efficiently handles large playlists

### 🎨 User Interface

- **Beautiful Material 3 Design**: Modern, clean interface with smooth animations
- **Dark/Light Mode**: Automatic theme switching based on system preferences
- **Responsive Layout**: Optimized for all screen sizes
- **Intuitive Navigation**: Easy-to-use home, history, and settings screens
- **Visual Feedback**: Real-time progress indicators and status updates

### 📥 Download Features

- **Individual Downloads**: Quick download of single songs
- **Batch Downloads**: Select multiple songs for combined download
- **ZIP Archive Downloads**: Download selected songs as a single compressed archive
- **MP3 Conversion**: Optional MP3 format conversion for compatibility
- **Progress Tracking**: Real-time download progress with file sizes
- **Custom Download Location**: Choose where files are saved
- **Retry Failed Downloads**: Automatically retry failed downloads

### 🎵 Built-in Music Player

- **Integrated Player**: Listen to downloaded songs within the app
- **Floating Player**: Collapsible music player panel with full controls
- **Playback Controls**: Play, pause, next, previous navigation
- **Shuffle Mode**: Randomize track order
- **Repeat Modes**: None, One, or All repeat options
- **Volume Control**: Adjustable volume with slider
- **Seek Bar**: Skip to any position in the track
- **Progress Display**: Current time and total duration
- **Favorite Toggle**: Mark songs as favorites

### 📚 Additional Features

- **Download History**: View all previous downloads with metadata
- **File Management**: Quick access to downloaded files
- **Auto-Update Check**: Optional automatic update notifications
- **Storage Permissions**: Automatic permission handling
- **Error Recovery**: Robust handling of network issues and failures
- **Selection Mode**: Multi-select songs for batch operations
- **Swipe Gestures**: Swipe left/right on songs for quick actions

---

## 🚀 Quick Start

### Getting Started

1. **Launch the App** → Press the app icon
2. **Paste Spotify Link** → Enter or paste a Spotify URL
3. **Tap Scrape** → Extract metadata from the link
4. **Choose Action**:
   - Download individual songs
   - Select multiple for batch download
   - Play songs using the built-in player
5. **Access Downloads** → View files in your chosen download location or check History

### Basic Workflow

```
Spotify Link → Paste → Scrape → Download → Enjoy
```

### Tips
- Use the History tab to view all previous downloads
- Change download location in Settings
- Enable auto-update checks for notifications on new versions
- Toggle MP3 conversion for compatibility
- Use selection mode to download multiple songs at once
- Swipe on songs for quick play or download actions

---

## 📱 User Interface

### Main Screen
- **Hero Banner**: App branding with gradient background
- **URL Input Field**: Paste Spotify links with paste button
- **MP3 Toggle**: Switch to convert downloads to MP3 format
- **Scrape Button**: Extract songs from the link
- **Download All Button**: Download all scraped songs
- **Song List**: View all extracted songs with details and controls
- **Selection Mode**: Multi-select songs for batch operations
- **Floating Player**: Collapsible music player at bottom

### History Screen
- **Download History**: View all previous downloads
- **File Details**: See file names, sizes, and timestamps
- **Direct Access**: Tap files to open them
- **Clear History**: Remove entries as needed
- **Error Handling**: View failed downloads and retry options

### Settings Screen
- **Storage Settings**:
  - Download Location: Choose where files are saved
  - Browse and select custom folders
- **Update Settings**:
  - Auto-check for Updates: Get notified of new versions
- **About Section**:
  - View current app version
  - Developer information
  - GitHub repository link

---

## ⚙️ Settings

### Storage Configuration
- **Download Location**: Set your preferred download folder
- **Default Path**: `/storage/emulated/0/Download/Spotify Downloads/`
- **Change Location**: Tap the folder option to browse and select a new path

### Update Settings
- **Auto-check for Updates**: Toggle to enable automatic update checks
- **When Enabled**: App checks for new versions on launch
- **Notifications**: Get notified when updates are available

### About
- **App Version**: View current installed version
- **Developer**: Shakti Kumar (Developer)
- **GitHub**: Visit the repository for source code and updates
- **License**: MIT License (see LICENSE file)

---

## 📱 Installation

### Requirements
- **Android Version**: 7.0 (API 24) or higher
- **Storage**: 100MB free space
- **Internet**: Stable connection for downloads

### Installation Methods

#### Option 1: Download APK
1. Go to [GitHub Releases](https://github.com/Shakti-ctrl/spoti/releases)
2. Download the latest APK file
3. Enable "Install from Unknown Sources" in Android settings
4. Install and launch the app

#### Option 2: GitHub Actions (Latest Build)
1. Visit [Actions](https://github.com/Shakti-ctrl/spoti/actions)
2. Select latest successful build
3. Download APK from artifacts
4. Install on device

#### Option 3: Build from Source
```bash
# Clone the repository
git clone https://github.com/Shakti-ctrl/spoti.git
cd spoti

# Build with Gradle
./gradlew build

# Run on device
./gradlew installDebug
```

---

## 🏗️ Architecture

### Technology Stack
- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Async Processing**: Kotlin Coroutines
- **HTTP Client**: OkHttp3
- **Audio Processing**: FFmpeg Kit
- **Storage**: SharedPreferences, JSON serialization
- **Build System**: Gradle Kotlin DSL

### Project Structure
```
app/src/main/java/dev/sumanth/spd/
├── MainActivity.kt              # Entry point
├── model/                       # Data models
├── service/                     # Background services
├── ui/
│   ├── component/               # Reusable components
│   ├── screen/                  # UI screens
│   ├── theme/                   # Material 3 theme
│   └── viewmodel/               # Business logic
└── utils/                       # Utilities
```

### Key Components
- **HomeScreenViewModel**: Main logic for downloads and playback
- **MusicPlayerService**: Background music playback
- **Downloader**: Handles file downloads
- **Spotify**: Metadata extraction and scraping

---

## 📄 License & Legal

### MIT License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Important Notice
- **Educational Use**: This app is intended for personal, educational use only
- **Copyright**: Users are responsible for respecting music copyright and laws
- **Spotify Terms**: Users must comply with Spotify's Terms of Service
- **No Liability**: Developers are not responsible for misuse of this application
- **File Details**: See file names, sizes, and timestamps
- **Direct Access**: Tap files to open them
- **Clear History**: Remove entries as needed

### Settings Screen
- **Storage Settings**:
  - Download Location: Choose where files are saved
  - Browse and select custom folders
- **Update Settings**:
  - Auto-check for Updates: Get notified of new versions
- **About Section**:
  - View current app version
  - Developer information
  - GitHub repository link

---

## ⚙️ Settings

### Storage Configuration
- **Download Location**: Set your preferred download folder
- **Default Path**: `/storage/emulated/0/Download/Spotify Downloads/`
- **Change Location**: Tap the folder option to browse and select a new path

### Update Settings
- **Auto-check for Updates**: Toggle to enable automatic update checks
- **When Enabled**: App checks for new versions on launch
- **Notifications**: Get notified when updates are available

### About
- **App Version**: View current installed version
- **Developer**: Shakti Kumar (Developer)
- **GitHub**: Visit the repository for source code and updates
- **License**: MIT License (see LICENSE file)

---

## 📱 Installation

### Requirements
- **Android Version**: 7.0 (API 24) or higher
- **Storage**: 100MB free space
- **Internet**: Stable connection for downloads

### Installation Methods

#### Option 1: Download APK
1. Go to [GitHub Releases](https://github.com/Shakti-ctrl/spoti/releases)
2. Download the latest APK file
3. Enable "Install from Unknown Sources" in Android settings
4. Install and launch the app

#### Option 2: GitHub Actions (Latest Build)
1. Visit [Actions](https://github.com/Shakti-ctrl/spoti/actions)
2. Select latest successful build
3. Download APK from artifacts
4. Install on device

#### Option 3: Build from Source
```bash
# Clone the repository
git clone https://github.com/Shakti-ctrl/spoti.git
cd spoti

# Build with Gradle
./gradlew build

# Run on device
./gradlew installDebug
```

---

## 🏗️ Architecture

### Technology Stack
- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Async Processing**: Kotlin Coroutines
- **HTTP Client**: OkHttp3
- **Audio Processing**: FFmpeg Kit
- **Storage**: SharedPreferences, JSON serialization
- **Build System**: Gradle Kotlin DSL

### Project Structure
```
app/src/main/java/dev/sumanth/spd/
├── MainActivity.kt              # Entry point
├── model/                       # Data models
├── service/                     # Background services
├── ui/
│   ├── component/               # Reusable components
│   ├── screen/                  # UI screens
│   ├── theme/                   # Material 3 theme
│   └── viewmodel/               # Business logic
└── utils/                       # Utilities
```

### Key Components
- **HomeScreenViewModel**: Main logic for downloads and playback
- **MusicPlayerService**: Background music playback
- **Downloader**: Handles file downloads
- **Spotify**: Metadata extraction and scraping

---

## 📄 License & Legal

### MIT License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Important Notice
- **Educational Use**: This app is intended for personal, educational use only
- **Copyright**: Users are responsible for respecting music copyright and laws
- **Spotify Terms**: Users must comply with Spotify's Terms of Service
- **No Liability**: Developers are not responsible for misuse of this application

### Technology Stack
- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Async Processing**: Kotlin Coroutines
- **HTTP Client**: OkHttp3
- **Audio Processing**: FFmpeg Kit
- **Storage**: SharedPreferences, JSON serialization
- **Build System**: Gradle Kotlin DSL

### Project Structure
```
app/src/main/java/dev/sumanth/spd/
├── MainActivity.kt              # Entry point
├── model/                       # Data models
├── service/                     # Background services
├── ui/
│   ├── component/               # Reusable components
│   ├── screen/                  # UI screens
│   ├── theme/                   # Material 3 theme
│   └── viewmodel/               # Business logic
└── utils/                       # Utilities
```

### Key Components
- **HomeScreenViewModel**: Main logic for downloads and playback
- **MusicPlayerService**: Background music playback
- **Downloader**: Handles file downloads
- **Spotify**: Metadata extraction and scraping

---

## �‍💻 Contributing

### How to Contribute
1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature-name`
3. **Make** your changes
4. **Test** thoroughly
5. **Commit**: `git commit -m "Add feature"`
6. **Push**: `git push origin feature-name`
7. **Submit** a Pull Request

### Code Guidelines
- Follow Kotlin style conventions
- Use Material 3 components and design
- Include proper error handling
- Add meaningful comments for complex logic
- Test on multiple Android versions

---

## 📄 License & Legal

### MIT License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Important Notice
- **Educational Use**: This app is intended for personal, educational use only
- **Copyright**: Users are responsible for respecting music copyright and laws
- **Spotify Terms**: Users must comply with Spotify's Terms of Service
- **No Liability**: Developers are not responsible for misuse of this application
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
**Developed by**: Shakti Kumar

