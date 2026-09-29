Smart Pantry Manager is a Java Android application developed to help users manage the ingredients available in their pantry and reduce food waste.
The application allows users to add, view, edit and delete pantry ingredients. It also checks the ingredients and quantities available in the pantry against a collection of stored recipes. A recipe is only suggested when all of its required ingredients and quantities are available in the user's pantry.
The application includes pantry management, recipe suggestions, recipe details, search functionality, expiring-soon items and a settings/profile screen.

Database:
The application uses **SQLite** as its database.
SQLite was chosen because Smart Pantry Manager is a local Android application and does not require an internet connection or a remote database server. SQLite is built into Android and allows pantry data to be stored directly on the device.
The database stores pantry items, recipes and recipe ingredients. SQLite also allows the application data to persist after the application is closed and reopened.

 Main Features:
* Add pantry ingredients
* View pantry ingredients
* Edit pantry ingredients
* Delete pantry ingredients
* Search pantry items
* View expiring-soon ingredients
* Strict recipe matching
* Recipe suggestions based on available ingredients and quantities
* Recipe details with ingredients and preparation instructions
* Persistent local data storage using SQLite
* RecyclerView with custom adapters
* Activity navigation using Intents

Requirements:
* Android Studio
* Java
* Android SDK
* Android emulator or Android device

Setup and Run Instructions:
1. Clone or download this repository.
2. Open the project in Android Studio.
3. Allow Android Studio to complete the Gradle sync.
4. Connect an Android device or start an Android emulator.
5. Build the project using Android Studio.
6. Run the application on the selected device or emulator.
7. The application will create and initialise its local SQLite database when it is first run.

Package Name:
`com.example.smartpantrymanager`

Database:
SQLite. The database is created and managed locally using the application's `DatabaseHelper` class.

Module: Mobile App Development 700
