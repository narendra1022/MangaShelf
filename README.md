## MangaShelf App

MangaShelf is an Android app that helps users explore and track their favorite manga with a clean and user-friendly experience.

### Demo Video
You can view the demo video and screenshots of the app [here ( Google Drive ink )](https://drive.google.com/drive/folders/1VR3VZFec9iVCg1Pv16_Zoc1PS5zHxcbq?usp=sharing).

### Features
-  **Manga List Page**: Displays manga with title, cover image, score, popularity, and publication year.
-  **Sorting & Filtering**: Grouped by publication year with horizontally scrollable tabs. Sorting by **score** and **popularity** (ascending/descending).
-  **Favorite Manga**: Mark/unmark favorites, persisted across sessions.
-  **Manga Details Page**: Displays manga information and allows favoriting & marking as read.
-  **Offline Support**: Fetches from local database if network request fails.
-  **Responsive UI**: Works across different screen sizes and orientations.
-  **Error Handling**: Displays messages and provides retry mechanisms

### Tech Stack
- **Kotlin** + **Jetpack Compose** for modern UI
- **Room Database** for local storage
- **Retrofit** for API calls
- **ViewModel + Flow** for state management
- **Hilt** for dependency injection
- **Coroutines** for background tasks and concurrency
- **Splash API** for implementing splash screen
- **Coil** for image loading and caching in Jetpack Compose

### Architecture
The app follows **MVVM (Model-View-ViewModel)** architecture, ensuring scalability and maintainability.

### Setup & Installation
1. Clone the repo:
   ```sh
   git clone https://github.com/narendra1022/MangaShelf.git
   ```
2. Open in **Android Studio**.
3. Sync Gradle and build the project.
4. Run the app on an emulator or physical device.

