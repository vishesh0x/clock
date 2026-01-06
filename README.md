# Clock

A modern, responsive Android Clock application built entirely with Jetpack Compose. This app combines premium UI interactions with robust functionality, offering a complete time management suite including Alarm, Timer, and Stopwatch tools.

## Features

### ⏰ Smart Alarm
- **Full Control:** Create, edit, and delete alarms with ease.
- **Customization:** Assign custom labels to alarms (e.g., "Work", "Workout").
- **Toggle Options:** Individually enable or disable Vibration and Snooze for each alarm.
- **Analog Interface:** Features a premium Analog Clock interface for setting time.
- **Reliable Scheduling:** Uses Android's AlarmManager to ensure alarms ring even if the app is closed.

### ⏳ Advanced Timer
- **Quick Input:** Numpad-based input system for fast and precise time setting.
- **Visual Progress:** Circular progress indicator visualizes remaining time.
- **Background Persistence:** Timer state is saved automatically; rotating the screen or switching apps does not reset the timer.
- **Landscape Support:** Layout splits into a side-by-side view on landscape orientation or tablets for better usability.

### ⏱️ Stopwatch
- **Precision Timing:** Millisecond-level accuracy for tracking duration.
- **Lap Recording:** Record unlimited laps, which are displayed in a scrollable list.
- **Rotation Support:** The stopwatch continues running seamlessly even when rotating the device.
- **Smart Layout:** The lap list automatically adjusts padding in portrait mode to ensure the last item floats above the navigation bar.

### 🎨 Premium UI & Theming
- **Liquid Navigation:** A custom physics-based navigation bar that morphs fluidly between tabs.
- **Responsive Design:** automatically detects screen size and orientation:
  - **Portrait:** Shows a floating bottom navigation bar.
  - **Landscape/Tablet:** Switches to a vertical navigation rail to maximize vertical screen space.
- **Dynamic Theming:**
  - **Material You:** Pulls colors directly from your wallpaper (Android 12+).
  - **AMOLED Mode:** A dedicated "Pure Black" mode for OLED screens to save battery.
  - **Custom Accents:** Option to manually override system colors with custom presets.

## Tech Stack

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Navigation:** Navigation Compose
- **Database:** Room Database (SQLite) for Alarm persistence
- **State Management:** `rememberSaveable` & Runtime State
- **Storage:** SharedPreferences for App Settings

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- Android SDK API 36
- JDK 17+.

### Installation

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/vishesh0x/clock.git](https://github.com/vishesh0x/clock.git)
    ```

2.  **Open in Android Studio**
    - Open Android Studio and select **Open**.
    - Navigate to the `clock` folder and click OK.
    - Allow Gradle to sync dependencies.

3.  **Run the App**
    - Connect an Android device or start an emulator.
    - Click the **Run** (▶️) button.

## Author

**Vishesh Kumar**
- GitHub: [@vishesh0x](https://github.com/vishesh0x)
- Website: [visheshraghuvanshi.in](https://visheshraghuvanshi.in)

## License

This project is open source and available under the [MIT License](LICENSE).