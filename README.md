# Nope-Remote 📱

Nope-Remote is an open-source Android application that turns your smartphone into a powerful, universal IR remote control. Built with modern Android technologies, it offers a sleek Material You interface and advanced features like Custom Flows to automate your home entertainment system.

<img src="https://via.placeholder.com/800x400.png?text=Nope-Remote+Banner" alt="Nope-Remote Banner" width="100%">

## ✨ Features

-   **Universal Compatibility**: Control TVs, Set-Top Boxes, ACs, and more using your phone's built-in IR blaster.
-   **Custom Flows (Macros)**: Create "Flows" to chain multiple commands together. Turn on your TV, Set-Top Box, and Soundbar with a single tap!
-   **Material You Design**: A beautiful, modern UI that adapts to your wallpaper's colors (on supported devices).
-   **Dark/Light Mode**: Fully supports system-wide dark and light themes.
-   **Database Support**: Stores your remotes and flows locally using Room Database.
-   **Haptic Feedback**: Get tactile confirmation for every button press.

## 🛠️ Tech Stack

-   **Language**: [Kotlin](https://kotlinlang.org/)
-   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
-   **Architecture**: MVVM (Model-View-ViewModel)
-   **Dependency Injection**: [Dagger Hilt](https://dagger.dev/hilt/)
-   **Local Database**: [Room](https://developer.android.com/training/data-storage/room)
-   **Networking**: [Retrofit](https://square.github.io/retrofit/)
-   **Asynchronous Processing**: Coroutines & Flow

## 🚀 Getting Started

### Prerequisites

-   Use an Android device with a built-in **IR Blaster**.
-   Android SDK 33 installed.
-   Minimum Android version: 6.0 (API Level 23).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/monuk7735/NopeRemote.git
    ```
2.  **Open in Android Studio:**
    -   Launch Android Studio.
    -   Select "Open" and navigate to the cloned directory.
3.  **Build and Run:**
    -   Let Gradle sync the dependencies.
    -   Connect your Android device or start an emulator (Note: Emulators do not support IR transmission).
    -   Click the **Run** button (green arrow).

## 📖 Usage

### Adding a Remote
1.  Tap the **"+"** button on the home screen.
2.  Select your device type and brand.
3.  Test the buttons to find the working configuration.
4.  Save the remote.

### Creating a Flow (Macro)
1.  Go to the **Flows** tab.
2.  Tap **"Create New Flow"**.
3.  Add steps by selecting buttons from your saved remotes.
4.  Add delays if necessary between commands.
5.  Save and execute the flow with one tap.

## 🤝 Contributing

Contributions are welcome! Please feel free to invoke `Issue` or `Pull Request`.

## 📄 License

This project is licensed under the [MIT License](LICENSE).
