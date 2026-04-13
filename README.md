# Parking Management System

A robust Android application designed to streamline parking operations, including vehicle entry, exit, ticket generation, and automated billing.

## 🚀 Features

*   **Vehicle Entry & Exit:** Efficiently manage the flow of vehicles with digital ticket generation.
*   **QR Code Integration:** Quick scanning for ticket identification and processing using CameraX.
*   **Dynamic Rate Calculation:** Automated billing based on configurable rates, supporting:
    *   Price per hour.
    *   Half-hour billing logic.
    *   Exceeding limit grace periods (e.g., first few minutes free).
*   **Actual Duration Tracking:** Precise tracking of stay duration shown in a user-friendly "X hour Y min" format.
*   **Receipt Printing:** Integration with thermal printers for professional parking receipts.
*   **Offline Support:** Local data persistence using Room database for reliable operation.
*   **Cloud Sync:** Real-time synchronization with a central API for data management and reporting.

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Architecture:** Clean Architecture with MVVM
*   **Dependency Injection:** Hilt
*   **Database:** Room (SQLite)
*   **Networking:** Retrofit & OkHttp
*   **Concurrency:** Kotlin Coroutines & Flow
*   **Scanning:** CameraX
*   **Image Loading:** Coil (if applicable)

## 📦 Project Structure

```text
com.logisparktech.parkingmanagementsystem
├── core                # Common utilities and base classes
├── data                # Repositories, DAOs, Entities, and Remote DTOs
├── di                  # Dependency Injection modules (Hilt)
├── domain              # Use Cases and Repository interfaces
└── presentation        # UI Layer (Screens, ViewModels, States)
```

## ⚙️ Configuration

### API Setup
The app connects to a central server. You can configure the base URL in:
`app/src/main/java/com/logisparktech/parkingmanagementsystem/di/NetworkModule.kt`

### Logging
Network logs are enabled via `HttpLoggingInterceptor`. Filter by `OkHttp` in Logcat to view API requests and responses.

## 🛠 Installation

1. Clone the repository.
2. Open the project in **Android Studio Hedgehog** (or later).
3. Sync the project with Gradle files.
4. Connect an Android device or emulator (API level 23+).
5. Build and Run the app.

## 📄 License
Copyright © 2024 Logispark Tech. All rights reserved.
