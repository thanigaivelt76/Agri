# Agri Project - Modernizing Agriculture

Agri Project is a professional Android application designed to bridge the gap between technology and traditional farming. Built with Jetpack Compose and powered by advanced integrations, it provides a comprehensive ecosystem for farmers, machinery owners, and agricultural workers.

## 🚀 Key Features

*   **Smart Marketplace:** A direct platform for farmers to list and sell crops, ensuring better price transparency.
*   **Machinery Hub:** Integrated Google Maps support to locate, view, and book agricultural machinery in real-time.
*   **AI & Voice Assistant:** On-device machine learning (TensorFlow Lite) and Voice Assistant to provide instant agricultural advice and hands-free operation.
*   **Weather Intelligence:** Real-time weather forecasting and agricultural alerts to help plan farming activities effectively.
*   **Worker Management:** A dedicated module for worker registration and hiring, simplifying seasonal labor management.
*   **Secure Transactions:** Fully integrated Razorpay payment gateway for safe and seamless financial transactions.
*   **Real-time Notifications:** Firebase Cloud Messaging (FCM) integration for order updates, weather alerts, and news.

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Backend:** Firebase (Authentication, Firestore, Cloud Storage)
*   **ML:** TensorFlow Lite
*   **Networking:** Retrofit, OkHttp, Kotlinx Serialization
*   **Maps:** Google Maps SDK for Android, Maps Compose
*   **Payments:** Razorpay SDK
*   **Image Loading:** Coil

## 📦 Prerequisites

Before running the project, ensure you have:
*   Android Studio Ladybug (or newer)
*   JDK 17+
*   A `google-services.json` file in the `app/` directory (Firebase)
*   Google Maps API Key (configured in `AndroidManifest.xml`)

## 🔧 Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/thanigaivelt76/Agri.git
    ```
2.  **Open in Android Studio:**
    Select the root folder and wait for Gradle sync to complete.
3.  **Configure API Keys:**
    Update the `YOUR_GOOGLE_MAPS_API_KEY_HERE` in `app/src/main/AndroidManifest.xml`.
4.  **Run the App:**
    Click the 'Run' button in Android Studio or use:
    ```bash
    ./gradlew assembleDebug
    ```

## 🤝 Contributing

Contributions are welcome! If you'd like to improve Agri Project, please:
1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/NewFeature`).
3.  Commit your changes (`git commit -m 'Add NewFeature'`).
4.  Push to the branch (`git push origin feature/NewFeature`).
5.  Open a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
