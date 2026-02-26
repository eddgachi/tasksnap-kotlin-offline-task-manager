# 📱 Offline-First Task Manager in Kotlin

A production-grade, educational project demonstrating **offline-first architecture** with Clean Architecture, MVVM, Jetpack Compose, Room Database, and WorkManager.

## 🎯 Project Overview

This is a **comprehensive learning project** that teaches advanced Android development concepts through building a real, usable app. The app prioritizes **offline functionality** while maintaining seamless background sync.

### What Makes This Special

- ✅ **Offline-First**: App works perfectly offline; sync happens in background
- ✅ **Clean Architecture**: Domain, Data, and Presentation layers clearly separated
- ✅ **MVVM Pattern**: State-driven UI with reactive data flows
- ✅ **Production-Ready**: Error handling, retry logic, edge case management
- ✅ **Highly Testable**: Unit, integration, and UI testing at every layer
- ✅ **Educational**: Every component explained, not just copy-paste code

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│           PRESENTATION LAYER                        │
│  (Jetpack Compose, ViewModels, StateFlow)           │
├─────────────────────────────────────────────────────┤
│            DOMAIN LAYER                             │
│  (Use Cases, Repositories, Business Logic)          │
├─────────────────────────────────────────────────────┤
│            DATA LAYER                               │
│  (Room DB, Mock/Real API, Local + Remote Sync)      │
├─────────────────────────────────────────────────────┤
│       BACKGROUND & UTILITIES                        │
│  (WorkManager, ConnectivityObserver, DI)            │
└─────────────────────────────────────────────────────┘
```

### Data Flow

```
User Action
    ↓
ViewModel.onEvent()
    ↓
UseCase (validation, logic)
    ↓
Repository (local write, mark PENDING_SYNC)
    ↓
Local DB (instant update)
    ↓
UI recomposes (immediate feedback)
    ↓
[Async] WorkManager syncs periodically
    ↓
Remote API (or mock)
    ↓
Local DB updated to SYNCED
    ↓
UI updates (shows sync status)
```

## 🚀 Features

### Core Features

- ✅ **Create Tasks**: Title, description, due date, priority, project
- ✅ **Edit Tasks**: Update any field, changes auto-sync
- ✅ **Delete Tasks**: Soft delete with PENDING_DELETE state
- ✅ **Complete Tasks**: Mark done/pending with instant UI feedback
- ✅ **Organize**: Tasks grouped by projects/categories
- ✅ **Filter & Sort**: By completion, priority, due date, alphabetical
- ✅ **Offline Support**: Full functionality without internet

### Sync Features

- ✅ **Automatic Sync**: Every 15 minutes via WorkManager
- ✅ **Smart Retry**: Exponential backoff (1m → 5m → 30m)
- ✅ **Conflict Handling**: Last-write-wins using timestamps
- ✅ **Sync Indicators**: Visual feedback (blue cloud = pending, green = synced)
- ✅ **Offline Banner**: Shows when device is offline
- ✅ **Pending Counter**: "Pending sync: 3" in UI

### Technical Features

- ✅ **Local Persistence**: Room database with indices
- ✅ **Reactive Updates**: Flow-based state management
- ✅ **Type Safety**: Enums for Priority, SyncStatus, TaskSort
- ✅ **Error Classification**: Network vs. Auth vs. Unknown errors
- ✅ **Logging**: Timber for debugging
- ✅ **Dependency Injection**: Hilt for loose coupling

## 📋 Tech Stack

| Layer            | Technology             | Purpose                      |
| ---------------- | ---------------------- | ---------------------------- |
| **UI**           | Jetpack Compose        | Modern declarative UI        |
| **State**        | StateFlow, Flow        | Reactive state management    |
| **ViewModel**    | Android Architecture   | Lifecycle-aware state holder |
| **Local DB**     | Room                   | Type-safe SQLite access      |
| **Remote API**   | Retrofit (TODO)        | Backend communication        |
| **DI**           | Hilt                   | Dependency injection         |
| **Background**   | WorkManager            | Periodic sync tasks          |
| **Connectivity** | ConnectivityManager    | Network state monitoring     |
| **Testing**      | JUnit5, MockK, Turbine | Comprehensive testing        |
| **Logging**      | Timber                 | Debug logging                |

## 📦 Project Structure

```
app/src/main/kotlin/com/example/tasktodo/
├── di/                              # Dependency Injection
│   ├── DatabaseModule.kt            # Room setup
│   ├── RepositoryModule.kt          # Repository bindings
│   ├── DataSourceModule.kt          # DataSource setup
│   └── WorkerModule.kt              # WorkManager scheduling
│
├── data/                            # DATA LAYER
│   ├── local/
│   │   ├── db/
│   │   │   ├── TaskDatabase.kt      # @Database class
│   │   │   ├── TaskDao.kt           # Type-safe queries
│   │   │   └── ProjectDao.kt
│   │   ├── entity/
│   │   │   ├── TaskEntity.kt        # Room schema
│   │   │   └── ProjectEntity.kt
│   │   └── datasource/
│   │       └── LocalTaskDataSource.kt   # Abstraction over Room
│   │
│   ├── remote/
│   │   ├── api/
│   │   │   └── TaskApiService.kt    # Retrofit (TODO)
│   │   └── datasource/
│   │       └── MockRemoteTaskDataSource.kt  # Simulated API
│   │
│   └── repository/
│       └── TaskRepositoryImpl.kt     # Offline-first orchestration
│
├── domain/                          # DOMAIN LAYER
│   ├── model/
│   │   ├── Task.kt                  # Core entity
│   │   ├── Project.kt               # Category
│   │   └── SyncStatus.kt            # Enum
│   │
│   ├── repository/
│   │   └── TaskRepository.kt        # Interface
│   │
│   └── usecase/
│       ├── GetTasksUseCase.kt       # Fetch
│       ├── CreateTaskUseCase.kt     # Create + validate
│       ├── UpdateTaskUseCase.kt     # Update + validate
│       ├── DeleteTaskUseCase.kt     # Soft delete
│       └── SyncTasksUseCase.kt      # Sync orchestration
│
├── presentation/                    # PRESENTATION LAYER
│   ├── ui/
│   │   ├── screen/
│   │   │   ├── TaskListScreen.kt        # Main list
│   │   │   ├── TaskDetailScreen.kt      # Detail/edit
│   │   │   └── ProjectScreen.kt         # Projects
│   │   │
│   │   ├── component/
│   │   │   ├── TaskCard.kt              # Reusable card
│   │   │   ├── PriorityBadge.kt         # Priority display
│   │   │   ├── SyncStatusIndicator.kt   # Sync icon
│   │   │   └── OfflineBanner.kt         # Offline indicator
│   │   │
│   │   └── theme/
│   │       ├── Theme.kt                 # Compose theme
│   │       └── Color.kt                 # Colors
│   │
│   ├── viewmodel/
│   │   ├── TaskListViewModel.kt         # List state
│   │   └── TaskDetailViewModel.kt       # Detail state
│   │
│   └── nav/
│       └── NavGraph.kt                  # Navigation routing
│
├── workers/                         # BACKGROUND WORK
│   ├── SyncWorker.kt                # Periodic sync
│   └── NotificationWorker.kt        # Reminders (TODO)
│
├── util/                            # UTILITIES
│   ├── ConnectivityObserver.kt      # Network monitoring
│   ├── LocalDateTimeConverter.kt    # Room type converter
│   ├── Extensions.kt                # Kotlin extensions
│   └── DateUtils.kt                 # Date formatting
│
├── TaskManagerApplication.kt        # Application class
└── MainActivity.kt                  # Activity entry point
```

## 🔄 Offline-First Data Flow

### Scenario: User Creates Task While Offline

```
1. User taps "New Task"
   ↓
2. CreateTaskUseCase validates input
   ↓
3. Task created with syncStatus = PENDING_SYNC
   ↓
4. Saved to local Room DB immediately
   ↓
5. UI updates instantly (blue cloud icon = PENDING_SYNC)
   ↓
6. [User goes about their day, app working perfectly offline]
   ↓
7. Later, user goes online (WiFi or data)
   ↓
8. WorkManager's next periodic run triggers SyncWorker
   ↓
9. SyncWorker finds all PENDING_SYNC tasks
   ↓
10. Sends to remote API (mocked for now)
   ↓
11. If successful, marks as SYNCED in local DB
   ↓
12. UI automatically updates: blue cloud → green checkmark
   ↓
13. ✅ Task fully synced
```

### Scenario: Network Timeout During Sync

```
1. SyncWorker attempts sync
   ↓
2. Network timeout → NetworkException thrown
   ↓
3. SyncWorker returns Result.retry()
   ↓
4. WorkManager applies exponential backoff:
   - 1st retry: Wait 1 minute
   - 2nd retry: Wait 5 minutes
   - 3rd retry: Wait 30 minutes
   ↓
5. Network recovers during one of these retries
   ↓
6. Sync succeeds
   ↓
7. ✅ Transient error recovered automatically
```

## 🛠️ Setup & Installation

### Prerequisites

- Android Studio Flamingo or later
- Kotlin 1.9+
- Android API 26+
- Gradle 8.0+

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/yourusername/task-manager.git
cd task-manager

# Build the app
./gradlew build

# Run on emulator/device
./gradlew installDebug
```

### Project Configuration

Update `app/build.gradle.kts`:

```gradle
android {
    namespace = "com.example.tasktodo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tasktodo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    // Hilt
    implementation "com.google.dagger:hilt-android:2.48"
    kapt "com.google.dagger:hilt-compiler:2.48"

    // Room
    implementation "androidx.room:room-runtime:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"

    // Jetpack Compose
    implementation "androidx.compose.ui:ui:1.6.0"
    implementation "androidx.compose.material3:material3:1.1.1"
    implementation "androidx.activity:activity-compose:1.8.0"

    // Coroutines & Flow
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1"

    // WorkManager
    implementation "androidx.work:work-runtime-ktx:2.8.1"

    // Navigation
    implementation "androidx.navigation:navigation-compose:2.7.5"

    // Timber logging
    implementation "com.jakewharton.timber:timber:5.0.1"

    // Testing
    testImplementation "junit:junit:4.13.2"
    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1"
    testImplementation "io.mockk:mockk:1.13.8"
    testImplementation "app.cash.turbine:turbine:1.0.0"
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
}
```

## 🧪 Testing

### Run All Tests

```bash
./gradlew test              # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
./gradlew testDebug         # Debug build tests
```

### Test Examples

**Unit Test (Domain Layer)**:

```kotlin
@Test
fun createTask_validInput_returnsPendingSyncStatus() = runTest {
    val useCase = CreateTaskUseCase(fakeRepository)

    val task = useCase(
        projectId = "proj1",
        title = "Buy milk",
        priority = Priority.HIGH
    )

    assertEquals(SyncStatus.PENDING_SYNC, task.syncStatus)
    assertTrue(task.id.isNotEmpty())
}
```

**Integration Test (Data Layer)**:

```kotlin
@RunWith(AndroidJUnit4::class)
class TaskRepositoryTest {
    @Test
    fun createTask_saved_retrievable() = runBlocking {
        val task = Task(...)
        repository.createTask(task)

        val saved = repository.getTaskById(task.id)
        assertEquals(task.title, saved?.title)
    }
}
```

**UI Test (Compose)**:

```kotlin
@RunWith(AndroidJUnit4::class)
class TaskListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysTasks() {
        composeTestRule.setContent {
            TaskListScreen(viewModel = fakeViewModel)
        }

        composeTestRule.onNodeWithText("Buy milk").assertIsDisplayed()
    }
}
```

## 📚 Key Concepts Explained

### 1. Offline-First

**Principle**: Local DB is source of truth, not the server.

**Implementation**:

- All writes go to local DB first (instant feedback)
- Network sync happens asynchronously in background
- If offline, app works perfectly with cached data
- When online, WorkManager automatically syncs changes

**Benefits**:

- ⚡ Instant UI response (no network wait)
- 📱 Works offline
- 🔒 Data safety (never lost locally)
- 🔋 Battery efficiency (batched syncs)

### 2. Clean Architecture

**Three Layers**:

**Domain Layer** (Business Logic)

- Framework-agnostic
- Pure Kotlin (no Android imports)
- Defines _what_ the app does
- Most stable, changes least

**Data Layer** (Persistence)

- Implements domain interfaces
- Handles local DB (Room) and remote API (Retrofit)
- Converts between domain models and storage formats
- Could swap Room for Datastore, Domain Layer unchanged

**Presentation Layer** (UI)

- Jetpack Compose screens
- ViewModels for state management
- Depends on domain layer only
- No business logic here

**Benefits**:

- 🧪 Testable at each layer
- 🔄 Reusable (domain layer independent)
- 🛠️ Maintainable (clear separation)

### 3. MVVM Pattern

**Model** → Domain layer (repositories, use cases)
**View** → Jetpack Compose screens
**ViewModel** → State holder + event handler

**Unidirectional Flow**:

```
User Action → ViewModel.onEvent() → State Update → StateFlow → Recomposition → UI
```

### 4. Repository Pattern

**Purpose**: Abstraction that hides data source complexity.

**Benefits**:

- UI doesn't care if data comes from Room, API, or mock
- Easy to test (use FakeRepository)
- Easy to swap implementations

**Key Method**:

```kotlin
// Always reads from local DB
override fun observeAllTasks(): Flow<List<Task>> =
    localDataSource.observeAllTasks()
```

### 5. StateFlow for Reactive UI

**Flow**: Asynchronous data stream (cold)
**StateFlow**: Flow with current value (hot), UI-friendly

**Usage**:

```kotlin
private val _uiState = MutableStateFlow(TaskListUiState())
val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

// In Compose
val state = viewModel.uiState.collectAsStateWithLifecycle()
// Recomposes when state changes
```

### 6. WorkManager for Periodic Sync

**Why not just a service?**

- Services can be killed
- WorkManager persists and reschedules
- Respects constraints (network, battery, device idle)
- Handles retries with exponential backoff

**Usage**:

```kotlin
val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
    repeatInterval = 15,
    TimeUnit.MINUTES
)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()

workManager.enqueueUniquePeriodicWork(
    "sync_tasks",
    ExistingPeriodicWorkPolicy.KEEP,
    syncWorkRequest
)
```

## 🐛 Common Issues & Troubleshooting

### Issue: Tasks Not Syncing

**Check**:

1. Is WorkManager scheduled? (`WorkerModule.scheduleSyncWork()`)
2. Is device online? (Check `ConnectivityObserver`)
3. Are tasks marked `PENDING_SYNC`? (Check Room DB)
4. Check Logcat for errors (`Timber.e()` logs)

**Debug**:

```bash
adb logcat | grep -i "sync"
```

### Issue: Stale Data in UI

**Check**:

1. Are you using `Flow` or just calling methods?
2. Is ViewModel collecting the Flow in `viewModelScope`?
3. Are you using `collectAsStateWithLifecycle()`?

**Solution**:

```kotlin
// ✅ Reactive
repository.observeAllTasks()
    .collect { tasks ->
        _uiState.update { it.copy(tasks = tasks) }
    }

// ❌ Stale
val tasks = repository.getTasksSync()  // One-time fetch
```

### Issue: Memory Leak

**Check**:

1. Using `viewModelScope` for coroutines?
2. Using `collectAsStateWithLifecycle()` for Flow?
3. Unsubscribing from listeners?

**Solution**:

```kotlin
// ✅ Safe (auto-cancels)
viewModelScope.launch { ... }

// ❌ Leak risk
GlobalScope.launch { ... }
```

### Issue: Room Crash - No Type Converter

**Check**:

1. Are you storing `LocalDateTime`?
2. Is `LocalDateTimeConverter` in `@TypeConverters`?

**Solution**:

```kotlin
@Database(...)
@TypeConverters(LocalDateTimeConverter::class)  // Add this!
abstract class TaskDatabase : RoomDatabase()
```

## 📖 Learning Resources

### Architecture

- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android Architecture Guides](https://developer.android.com/jetpack/guide)
- [MVVM Pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)

### Android Libraries

- [Jetpack Compose Docs](https://developer.android.com/compose)
- [Room Database Tutorial](https://developer.android.com/training/data-storage/room)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Hilt DI](https://dagger.dev/hilt/)

### Kotlin

- [Coroutines & Flow](https://kotlinlang.org/docs/flow.html)
- [Sealed Classes](https://kotlinlang.org/docs/sealed-classes.html)
- [Scope Functions](https://kotlinlang.org/docs/scope-functions.html)

### Testing

- [JUnit5](https://junit.org/junit5/)
- [MockK](https://mockk.io/)
- [Turbine](https://github.com/cashapp/turbine)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)

## 🚀 Future Enhancements

### Short-term

- [ ] Real Retrofit API integration
- [ ] Push notifications for due dates
- [ ] Subtasks support
- [ ] Enhanced sync conflict resolution

### Medium-term

- [ ] Recurring tasks
- [ ] Task reminders with Alarms
- [ ] Share tasks with other users
- [ ] Rich text descriptions (Markdown)

### Long-term

- [ ] Collaborative editing (CRDT)
- [ ] Cloud backup
- [ ] Cross-device sync
- [ ] Custom recurring patterns

## 📝 License

This project is licensed under the MIT License - see LICENSE file for details.

## 👨‍🎓 Learning Path

**Recommended learning order**:

1. **Step 1: Domain Layer** (4 hours)
   - Understand models, enums, repository interface, use cases
   - Read: `domain/`

2. **Step 2: Data Layer** (6 hours)
   - Room entities, DAOs, type converters
   - LocalDataSource, MockRemoteDataSource
   - TaskRepositoryImpl (offline-first logic)
   - Read: `data/`

3. **Step 3: Presentation Layer** (6 hours)
   - UiState and UiEvent patterns
   - ViewModel with StateFlow
   - Compose screens and components
   - Navigation
   - Read: `presentation/`

4. **Step 4: Background Sync** (4 hours)
   - SyncWorker and Result types
   - WorkManager scheduling and constraints
   - ConnectivityObserver
   - Error classification and retry logic
   - Read: `workers/`

5. **Step 5: Testing** (4 hours)
   - Unit tests (domain layer)
   - Integration tests (data layer)
   - UI tests (Compose)
   - WorkManager tests
   - Read: Test files

**Total**: ~24 hours hands-on learning

## ❓ FAQ

**Q: Why offline-first?**
A: Real-world networks are unreliable. Offline-first prioritizes data safety and user experience.

**Q: Can I use this in production?**
A: Yes! This architecture is production-ready. Add real Retrofit API, proper error handling, and analytics.

**Q: How do I add a new feature?**
A:

1. Update domain model/repository
2. Implement in repository
3. Create use case
4. Update ViewModel
5. Build Compose UI
6. Write tests

**Q: What about multi-user sync?**
A: Use CRDT (Conflict-free Replicated Data Types) or Vector Clocks for conflict resolution. This requires more advanced sync logic.

**Q: How often should WorkManager sync?**
A: 15 minutes is reasonable default. Adjust based on battery and network constraints.

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Write tests
4. Submit a pull request

## 📞 Support

Have questions? Open an issue with:

- Error message / stack trace
- Steps to reproduce
- Expected vs. actual behavior
- Device/Android version

---

**Happy coding!** 🚀 This project demonstrates enterprise-grade Android architecture. Use it as a template for your own apps.
