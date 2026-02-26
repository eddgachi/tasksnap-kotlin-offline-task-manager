# 🎓 Building an Offline-First Task Manager in Kotlin: Complete Beginner's Guide

**Duration**: 20-30 hours of learning  
**Level**: Beginner to Intermediate Android Development  
**Prerequisites**: Basic Kotlin, Android fundamentals

---

## Table of Contents

1. [Introduction: What Are We Building?](#introduction-what-are-we-building)
2. [Core Concepts You Need to Know](#core-concepts-you-need-to-know)
3. [Understanding Offline-First Architecture](#understanding-offline-first-architecture)
4. [The Tools We're Using](#the-tools-were-using)
5. [Project Folder Structure Explained](#project-folder-structure-explained)
6. [Step 1: Domain Layer (The Blueprint)](#step-1-domain-layer-the-blueprint)
7. [Step 2: Data Layer (Local Storage & Sync)](#step-2-data-layer-local-storage--sync)
8. [Step 3: Presentation Layer (The UI)](#step-3-presentation-layer-the-ui)
9. [Step 4: Background Sync (The Magic)](#step-4-background-sync-the-magic)
10. [How Everything Works Together](#how-everything-works-together)
11. [Common Beginner Mistakes](#common-beginner-mistakes)
12. [Testing Your App](#testing-your-app)
13. [Next Steps](#next-steps)

---

## Introduction: What Are We Building?

### The App

A **Task Manager** app where users can:

- ✅ Create, edit, delete tasks
- 📁 Organize tasks into projects/categories
- ⚡ Mark tasks as complete
- 🔍 Filter and sort tasks
- 📱 Work completely **offline** without internet
- 🔄 Automatically sync when internet returns

### What Makes This Special?

Most apps work like this:

```
User taps button → App waits for internet → Gets response → Shows result
                   ⏳ (user waits, frustrated if slow)
```

Our app works like this:

```
User taps button → App saves locally → Shows result instantly
                   🚀 (instant feedback!)
                   ↓
              [Meanwhile, background sync happens]
                   ↓
              Verifies with server when possible
```

### Key Insight: Why Offline-First?

Imagine this scenario:

- You're on a train with spotty WiFi
- You create a task "Buy groceries"
- Traditional app: ❌ "Network error, try again"
- Our app: ✅ Task saved locally, shows immediately, syncs when connected

**That's the power of offline-first.**

---

## Core Concepts You Need to Know

### 1. What is a Database?

A **database** is like a digital filing cabinet where your app stores information.

**Traditional approach**:

```
App → Internet → Server's Database
(Need internet every time)
```

**Offline-first approach**:

```
App → Local Database (On Device)
      ↓
      [Syncs to] → Server's Database (When online)
```

### 2. What is Room?

**Room** is an Android library that lets you:

- Create a local database on the user's phone
- Store data like tasks, projects, etc.
- Query data (find, filter, sort)
- Update data (edit, delete)

Think of it as a mini-database built into your app.

```kotlin
// Without Room (manual SQLite):
val db = SQLiteDatabase.openDatabase("tasks.db")
val cursor = db.rawQuery("SELECT * FROM tasks WHERE id = ?", arrayOf(taskId))

// With Room (type-safe, automatic):
val task = taskDao.getTaskById(taskId)  // Much cleaner!
```

### 3. What is a DAO?

**DAO** = Data Access Object

It's an interface that says "here are all the ways you can talk to the database":

```kotlin
@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?
}
```

Translation:

- `@Insert`: Insert a task
- `@Query`: Custom SQL query
- `suspend`: Happens asynchronously (doesn't block UI)

### 4. What is a Flow?

**Flow** is a way to watch for changes in data.

Think of it like subscribing to a newspaper:

```
Newspaper → Publisher ← Subscriber (You)
                           ↓
                    Notified of updates
```

```kotlin
// Old way (manual updates):
fun getTasks(): List<Task> {
    return database.query(...)  // One-time fetch
}

// New way (Flow - reactive):
fun observeTasks(): Flow<List<Task>> {
    return database.observeTasks()  // Continuous updates
}

// In UI:
database.observeTasks().collect { tasks ->
    updateUI(tasks)  // Runs every time tasks change
}
```

**Benefits**:

- Automatic UI updates when data changes
- No manual refresh buttons needed
- Battery efficient (no polling)

### 5. What is a ViewModel?

**ViewModel** is a helper that:

- Holds data the UI needs
- Survives configuration changes (screen rotation)
- Handles user interactions
- Talks to the data layer

```kotlin
class TaskListViewModel {
    val tasks: StateFlow<List<Task>>  // Data

    fun deleteTask(id: String) {      // User action handler
        // Delete logic here
    }
}

// In UI:
val viewModel = hiltViewModel()
val tasks = viewModel.tasks.collectAsState()
Button(onClick = { viewModel.deleteTask(id) })
```

### 6. What is Coroutines?

**Coroutines** are a way to write asynchronous code that looks synchronous.

```kotlin
// Old way (callbacks):
database.getTask(id) { task ->
    println(task.title)
}

// New way (coroutines - suspend functions):
suspend fun getTask(id: String): Task {
    return database.getTask(id)  // Looks normal!
}

// Called:
viewModelScope.launch {
    val task = getTask(id)
    println(task.title)
}
```

**Key point**: `suspend` functions pause execution without blocking the thread.

### 7. What is Dependency Injection (Hilt)?

**DI** is a pattern where you don't create objects yourself; you ask for them to be created.

```kotlin
// ❌ Bad (manual creation):
class TaskRepository {
    private val database = TaskDatabase.getInstance()  // Hard to test
}

// ✅ Good (dependency injection):
class TaskRepository @Inject constructor(
    private val database: TaskDatabase  // Provided by Hilt
)
```

**Why?**

- **Testability**: Swap real database with fake database for testing
- **Flexibility**: Change implementations without changing code
- **Organization**: DI framework manages object creation

### 8. What is Compose?

**Jetpack Compose** is a modern way to build Android UIs using Kotlin functions instead of XML.

```kotlin
// Old way (XML):
<Button
    android:id="@+id/btn_add"
    android:text="Add Task"
    android:layout_width="match_parent" />

// New way (Compose):
@Composable
fun TaskListScreen() {
    Button(
        onClick = { /* add task */ },
        text = { Text("Add Task") }
    )
}
```

**Benefits**:

- Reactive (automatic UI updates)
- Less boilerplate code
- Type-safe (no XML parsing errors)
- Hot reload during development

### 9. What is Clean Architecture?

**Clean Architecture** separates your code into layers:

```
┌─────────────────────────────────┐
│   PRESENTATION LAYER            │  ← UI, Compose, ViewModels
├─────────────────────────────────┤
│   DOMAIN LAYER                  │  ← Business logic, Use Cases
├─────────────────────────────────┤
│   DATA LAYER                    │  ← Databases, APIs, Repositories
└─────────────────────────────────┘
```

**Why layers?**

- **Separation of Concerns**: Each layer has one job
- **Testability**: Test each layer independently
- **Reusability**: Swap implementations easily
- **Maintainability**: Changes in one layer don't break others

### 10. What is MVVM?

**MVVM** = Model-View-ViewModel pattern:

```
USER INTERACTION
    ↓
   VIEW (Compose UI)
    ↓ (calls methods)
VIEW MODEL (holds state, handles logic)
    ↓ (queries)
MODEL (data layer, repository)
    ↓ (returns data)
STATE FLOWS back to VIEW
    ↓
UI UPDATES automatically
```

**Example**:

```kotlin
// MODEL
data class Task(val id: String, val title: String, val completed: Boolean)

// VIEW MODEL
class TaskViewModel {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun completeTask(id: String) {
        // Mark task complete in database
    }
}

// VIEW (Compose)
@Composable
fun TaskListScreen(viewModel: TaskViewModel) {
    val tasks = viewModel.tasks.collectAsState()

    LazyColumn {
        items(tasks.value) { task ->
            TaskCard(
                task = task,
                onComplete = { viewModel.completeTask(task.id) }
            )
        }
    }
}
```

---

## Understanding Offline-First Architecture

### The Core Problem

Traditional apps assume the internet is always available:

```
✗ User is offline → App can't function
✗ Network is slow → App is slow
✗ App crashes → Data might be lost
✗ No internet at all → User can't create data
```

### The Offline-First Solution

Our app assumes the internet is **unreliable**:

```
✓ User is offline → App still works (reads from local DB)
✓ Network is slow → App is fast (local DB access is instant)
✓ App crashes → Data is safe (already in local DB)
✓ No internet → User can create data (it's stored locally)
✓ Internet returns → Data syncs automatically
```

### The Key Insight: Local DB is Source of Truth

**Traditional**:

```
Server DB ← Source of Truth
    ↓
Client Cache (might be stale)
```

**Offline-First**:

```
Local DB ← Source of Truth
    ↓
Server DB (copy for backup/sync)
```

### What is SyncStatus?

Since we have multiple copies of data (local + server), we need to track what's been synced:

```kotlin
enum class SyncStatus {
    PENDING_SYNC,   // Created/edited locally, not sent to server yet
    SYNCED,         // Safe on both local and server
    PENDING_DELETE, // User deleted, needs to notify server
}
```

**Example flow**:

```
1. User creates task offline
   Status: PENDING_SYNC (stored locally)

2. App shows task immediately
   User sees it (green checkmark would be wrong here)

3. Internet comes back, WorkManager syncs
   Sends task to server

4. Server confirms receipt
   Status changes to SYNCED

5. UI updates to show green checkmark
   User sees sync confirmation
```

### Why Soft Delete?

Instead of deleting immediately:

```kotlin
// ❌ Hard delete (immediate removal)
taskDao.deleteTask(taskId)
// What if app crashes before syncing deletion to server?
// Server still has the task!

// ✅ Soft delete (mark for deletion)
task.copy(syncStatus = SyncStatus.PENDING_DELETE)
taskDao.updateTask(updatedTask)
// Now we have a record that deletion was requested
// WorkManager will sync this to server
// Only delete locally after server confirms
```

**Benefit**: Never lose information. Even if app crashes, we know the user wanted to delete it.

---

## The Tools We're Using

### 1. Room Database

**What**: Local SQLite database library for Android

**Why**:

- Type-safe queries (compiler catches errors)
- Automatic migrations
- Supports reactive data with Flow
- Works offline

**How it works**:

```
Your Code (Kotlin)
    ↓
Room (abstraction)
    ↓
SQLite (actual database on device)
    ↓
Device Storage
```

**Example**:

```kotlin
// Define what table looks like
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val completed: Boolean,
)

// Define how to access it
@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Query("SELECT * FROM tasks")
    fun observeAllTasks(): Flow<List<TaskEntity>>
}

// Create database
@Database(entities = [TaskEntity::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
```

### 2. Retrofit

**What**: Library for making HTTP requests to APIs (we'll use this later)

**Why**:

- Clean, type-safe API calls
- Automatic JSON serialization
- Easy to mock for testing

**How it works**:

```
Your Code
    ↓
Retrofit (builds HTTP request)
    ↓
Internet
    ↓
Server
    ↓
Server responds (JSON)
    ↓
Retrofit (parses to Kotlin objects)
    ↓
Your Code gets typed object
```

**Future example** (we're mocking for now):

```kotlin
interface TaskApiService {
    @POST("/api/tasks")
    suspend fun createTask(@Body task: TaskDto): TaskDto

    @GET("/api/tasks")
    suspend fun getAllTasks(): List<TaskDto>
}
```

### 3. WorkManager

**What**: Schedules background tasks (even if app is closed)

**Why**:

- Sync happens automatically every 15 minutes
- Respects device constraints (battery, network, etc.)
- Survives app crashes and device reboots
- Efficient (batches work)

**How it works**:

```
App is open or closed
    ↓
WorkManager timer ticks (every 15 min)
    ↓
Checks constraints (is network available?)
    ↓
If yes: Start SyncWorker
    ↓
SyncWorker queries local DB for PENDING_SYNC tasks
    ↓
Sends to server (or mock API)
    ↓
Updates local DB to SYNCED
    ↓
If network fails: Retry with exponential backoff
    ↓
Next WorkManager trigger (15 min later)
```

**When would you use it**:

- Periodic syncs ✅ (we use this)
- One-time background work ✅
- Cleanup tasks
- Analytics

**When you wouldn't**:

- Real-time notifications (use Firebase Cloud Messaging instead)
- Immediate user response (do it in the app)

### 4. Hilt Dependency Injection

**What**: Framework that automatically creates and injects objects

**Why**:

- Reduces boilerplate code
- Makes testing easier (swap real with fake)
- Manages object lifecycle
- Handles complex dependencies

**How it works**:

```kotlin
// Step 1: Tell Hilt "here's how to create a TaskRepository"
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideTaskRepository(
        localDataSource: LocalTaskDataSource,
        remoteDataSource: RemoteTaskDataSource,
    ): TaskRepository {
        return TaskRepositoryImpl(localDataSource, remoteDataSource)
    }
}

// Step 2: Ask Hilt for the object (Hilt figures out dependencies)
class MyViewModel @Inject constructor(
    private val taskRepository: TaskRepository  // Hilt provides this
) : ViewModel() {
    // Can use taskRepository immediately
}
```

**Benefits**:

```
Without Hilt (❌ manual):
viewModel = TaskViewModel(
    taskRepository = TaskRepositoryImpl(
        localDataSource = LocalTaskDataSource(
            taskDao = TaskDatabase.getInstance(context).taskDao()
        ),
        remoteDataSource = MockRemoteTaskDataSource()
    )
)
// Lots of boilerplate!

With Hilt (✅ automatic):
viewModel = hiltViewModel()  // Hilt figures everything out!
```

### 5. Jetpack Compose

**What**: Modern declarative UI framework for Android

**Why**:

- Build UI with Kotlin functions
- Reactive (UI updates automatically)
- Less code than XML layouts
- Hot reload during development

**Key concepts**:

```kotlin
@Composable  // This function builds UI
fun TaskCard(task: Task) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(task.title)  // Text composable

        Checkbox(
            checked = task.completed,
            onCheckedChange = { /* handle change */ }
        )
    }
}

// Composables are reusable:
@Composable
fun TaskListScreen() {
    LazyColumn {
        items(taskList) { task ->
            TaskCard(task = task)  // Reuse TaskCard
        }
    }
}
```

**Recomposition**:

```
State changes
    ↓
Compose detects change
    ↓
Re-runs affected composables
    ↓
Only affected parts update (efficient!)
    ↓
UI shows new state
```

### 6. Coroutines

**What**: Kotlin feature for asynchronous, non-blocking code

**Why**:

- Write async code that looks sync
- Don't block UI thread
- Efficient (lightweight threads)

**The problem coroutines solve**:

```kotlin
// ❌ Without coroutines (blocks UI):
val task = database.getTask(id)  // Blocks entire UI thread!
println(task.title)

// UI freezes, feels slow!

// ✅ With coroutines (doesn't block):
viewModelScope.launch {
    val task = database.getTask(id)  // Pauses this coroutine, not UI
    println(task.title)
}
// UI stays responsive!
```

**Key function types**:

```kotlin
// Regular function
fun regularFunction() {
    println("Hello")  // Runs immediately
}

// Suspend function (can be paused)
suspend fun suspendFunction() {
    delay(1000)      // Pauses here, doesn't block thread
    println("Hello")
}

// Using suspend:
viewModelScope.launch {  // Start coroutine
    suspendFunction()    // Call suspend function
}
```

---

## Project Folder Structure Explained

Let's understand why we organize code the way we do:

```
app/src/main/kotlin/com/example/tasktodo/
│
├── di/                          ← Dependency Injection (How objects are created)
│   ├── DatabaseModule.kt        ← "Create a TaskDatabase instance"
│   ├── RepositoryModule.kt      ← "Create a TaskRepository instance"
│   └── WorkerModule.kt          ← "Create a WorkManager instance"
│
├── domain/                      ← WHAT the app does (business logic)
│   ├── model/
│   │   ├── Task.kt              ← Core task entity (framework-agnostic)
│   │   └── SyncStatus.kt        ← Sync state enum
│   │
│   ├── repository/
│   │   └── TaskRepository.kt    ← Interface (contract, not implementation)
│   │
│   └── usecase/
│       ├── CreateTaskUseCase.kt ← "How to create a task"
│       ├── DeleteTaskUseCase.kt ← "How to delete a task"
│       └── SyncTasksUseCase.kt  ← "How to sync tasks"
│
├── data/                        ← HOW we store/get data
│   ├── local/
│   │   ├── db/
│   │   │   ├── TaskDatabase.kt  ← Room database declaration
│   │   │   └── TaskDao.kt       ← How to query the database
│   │   │
│   │   ├── entity/
│   │   │   └── TaskEntity.kt    ← What a task looks like in database
│   │   │
│   │   └── datasource/
│   │       └── LocalTaskDataSource.kt ← Abstraction over Room
│   │
│   ├── remote/
│   │   └── datasource/
│   │       └── MockRemoteTaskDataSource.kt ← Simulates API (for testing)
│   │
│   └── repository/
│       └── TaskRepositoryImpl.kt ← Implements the TaskRepository interface
│
├── presentation/                ← What user SEES (UI layer)
│   ├── ui/
│   │   ├── screen/
│   │   │   └── TaskListScreen.kt ← Full screen UI
│   │   │
│   │   └── component/
│   │       └── TaskCard.kt      ← Reusable component
│   │
│   ├── viewmodel/
│   │   └── TaskListViewModel.kt ← Holds state, handles UI logic
│   │
│   └── nav/
│       └── NavGraph.kt          ← Screen navigation
│
├── workers/                     ← Background tasks
│   └── SyncWorker.kt            ← Periodic sync worker
│
├── util/                        ← Helper utilities
│   ├── ConnectivityObserver.kt  ← Detect online/offline
│   └── DateUtils.kt             ← Date formatting
│
├── TaskManagerApplication.kt    ← App entry point
└── MainActivity.kt              ← Activity with Compose
```

### Why Three Layers? (Domain, Data, Presentation)

**Analogy: A Restaurant**

```
PRESENTATION LAYER (Dining Room)
    ↑ (waiters take orders)
    │
DOMAIN LAYER (Chef's Instructions)
    ↑ (chef prepares food)
    │
DATA LAYER (Kitchen/Suppliers)
    ↓ (ingredients, storage)
```

**Benefits of separation**:

1. **Domain Layer** (independent):
   - "A task has a title and due date"
   - Doesn't care if data is in Room, Datastore, or API
   - Doesn't care if UI is Compose, XML, or web

2. **Data Layer** (implementation):
   - "We store tasks in Room database"
   - Can change to Datastore without affecting domain/presentation
   - Can add new API endpoints without affecting domain/presentation

3. **Presentation Layer** (UI):
   - "Show tasks in a list"
   - Can change from Compose to XML without affecting domain/data
   - Doesn't know if data comes from Room or API

**Testing benefit**:

```
Test domain logic ← Can mock data layer
    ↓
Test data layer ← Can mock Room
    ↓
Test UI ← Can mock ViewModel
```

---

## Step 1: Domain Layer (The Blueprint)

### Purpose

The domain layer defines **what the app does**, independent of **how** it does it.

**Key principle**: This layer has NO Android imports (except in UI). It's pure Kotlin business logic.

### Core Models

#### Task Model

```kotlin
data class Task(
    val id: String,              // Unique ID (UUID generated locally)
    val projectId: String,       // Which project (category) it belongs to
    val title: String,           // "Buy groceries"
    val description: String,     // "Get milk, eggs, bread"
    val dueDate: LocalDateTime?, // "2024-02-23 at 5 PM" (nullable)
    val priority: Priority,      // Enum: LOW, MEDIUM, HIGH
    val isCompleted: Boolean,    // Done? true/false
    val createdAt: LocalDateTime,  // When created (UTC)
    val updatedAt: LocalDateTime,  // Last modified (UTC)
    val syncStatus: SyncStatus,  // PENDING_SYNC / SYNCED / PENDING_DELETE
)
```

**Why each field?**

| Field                 | Why                                        |
| --------------------- | ------------------------------------------ |
| `id`                  | Uniquely identify task across syncs        |
| `projectId`           | Group related tasks (Work, Personal, etc.) |
| `title`               | What the task is                           |
| `dueDate`             | Nullable because not all tasks have dates  |
| `priority`            | Filter/sort by importance                  |
| `syncStatus`          | CRITICAL: Track what needs syncing         |
| `createdAt/updatedAt` | Timestamps help resolve conflicts          |
| `LocalDateTime`       | Readable, timezone-aware (not just `Long`) |

#### Status Enums

```kotlin
enum class Priority {
    LOW,      // Not urgent
    MEDIUM,   // Standard
    HIGH,     // Urgent
}

enum class SyncStatus {
    PENDING_SYNC,   // Created locally, not sent to server yet
    SYNCED,         // Safe on both local and server
    PENDING_DELETE, // User deleted, needs to notify server
}
```

**Why enums?**

- Type-safe: Compiler prevents typos (can't set `priority = "SUPER_HIGH"`)
- Self-documenting: Clear what values are valid
- Exhaustive when statements: Compiler warns if you miss a case

### Repository Interface

The repository is a **contract** that says "here's what the app can do with tasks":

```kotlin
interface TaskRepository {

    // ===== READ OPERATIONS =====
    // These return Flow for reactive updates

    /**
     * Get all tasks in a project.
     * Flow means: subscribe once, get updates whenever DB changes
     */
    fun observeTasksByProject(projectId: String): Flow<List<Task>>

    /**
     * Get all tasks with optional filtering.
     * filterCompleted: null = all, true = only completed, false = only pending
     */
    fun observeAllTasks(
        filterCompleted: Boolean? = null,
        sortBy: TaskSort = TaskSort.CREATED_DESC
    ): Flow<List<Task>>

    /**
     * Get one task (not a stream, just once).
     * Used for detail screens where we need just one task.
     */
    suspend fun getTaskById(id: String): Task?


    // ===== WRITE OPERATIONS =====
    // These mark PENDING_SYNC and return immediately

    /**
     * Create a new task.
     * Saves locally as PENDING_SYNC.
     * Returns immediately (doesn't wait for server).
     * WorkManager syncs to server later.
     */
    suspend fun createTask(task: Task): Task

    /**
     * Update existing task.
     * Marks as PENDING_SYNC.
     * Returns immediately.
     */
    suspend fun updateTask(task: Task)

    /**
     * Delete a task (soft delete).
     * Marks as PENDING_DELETE (doesn't actually delete yet).
     * WorkManager will sync deletion to server.
     * Only deletes locally after server confirms.
     */
    suspend fun deleteTask(id: String)

    /**
     * Hard delete (internal only, after sync).
     * Called only after server confirms deletion.
     */
    suspend fun hardDeleteTask(id: String)


    // ===== SYNC =====

    /**
     * Sync all pending changes with server.
     * Called by WorkManager every 15 minutes.
     * Returns: how many tasks were synced
     */
    suspend fun syncPendingChanges(): Int
}
```

**Why an interface, not a class?**

```kotlin
// ❌ Without interface (tightly coupled):
val repo = TaskRepositoryImpl(...)  // Must use specific implementation
// Hard to test (can't swap with fake)

// ✅ With interface (loose coupling):
val repo: TaskRepository = TaskRepositoryImpl(...)  // Type is interface
// Can swap: TaskRepository = FakeTaskRepository(...)  // For testing
```

### Use Cases

Use cases are **business logic flows**. They:

- Validate input
- Handle specific workflows
- Are reusable across screens
- Are testable independently

**Example: CreateTaskUseCase**

```kotlin
class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Create a new task with validation.
     *
     * This is a "use case" because it represents a business flow:
     * 1. Validate the title (can't be empty)
     * 2. Generate a unique ID
     * 3. Set current timestamps
     * 4. Mark as PENDING_SYNC
     * 5. Save to database
     */
    suspend operator fun invoke(
        projectId: String,
        title: String,
        description: String = "",
        dueDate: LocalDateTime? = null,
        priority: Priority = Priority.MEDIUM,
    ): Task {
        // Step 1: Validate
        require(title.isNotBlank()) { "Task title cannot be empty" }

        // Step 2: Generate ID locally (offline-first)
        val localId = UUID.randomUUID().toString()
        val now = LocalDateTime.now()

        // Step 3: Create task with PENDING_SYNC status
        val newTask = Task(
            id = localId,
            projectId = projectId,
            title = title.trim(),
            description = description.trim(),
            dueDate = dueDate,
            priority = priority,
            isCompleted = false,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC,  // Key: will be synced by WorkManager
        )

        // Step 4: Save to repository
        return taskRepository.createTask(newTask)
    }
}
```

**Key point**: Use cases abstract business logic. If requirements change ("title must be 10+ chars"), you change it in one place.

### Why Domain Layer Doesn't Know About Android?

```kotlin
// ❌ WRONG (domain depends on Android):
data class Task(
    val title: String,
    val bitmap: android.graphics.Bitmap,  // ❌ Android dependency
)

// ✅ CORRECT (domain is pure):
data class Task(
    val title: String,
    val imageUrl: String,  // ✅ Pure data
)
```

**Benefits**:

- Domain can be tested without Android emulator
- Domain can be reused on different platforms (iOS, web)
- Domain is framework-agnostic

---

## Step 2: Data Layer (Local Storage & Sync)

### Purpose

The data layer handles:

- **Storage**: Local database (Room)
- **Remote**: API communication (Retrofit, mocked)
- **Sync**: Coordinating local ↔ remote
- **Conversion**: Domain models ↔ database entities

### Understanding Entities vs Models

**Problem**: Domain model and database schema don't always match.

```kotlin
// Domain model (what app thinks about):
data class Task(
    val id: String,
    val title: String,
    val syncStatus: SyncStatus,  // Enum
)

// Database entity (how it's stored):
@Entity
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val syncStatus: String,  // Stored as string ("PENDING_SYNC")
)
```

**Why separate?**

- Database needs strings (JSON serialization)
- Domain prefers types (enums, custom classes)
- Can change schema without changing business logic

**Conversion**:

```kotlin
// Domain → Entity (when saving to DB)
fun Task.toEntity(): TaskEntity = TaskEntity(
    id = this.id,
    title = this.title,
    syncStatus = this.syncStatus.name,  // PENDING_SYNC → "PENDING_SYNC"
)

// Entity → Domain (when reading from DB)
fun TaskEntity.toDomain(): Task = Task(
    id = this.id,
    title = this.title,
    syncStatus = SyncStatus.valueOf(this.syncStatus),  // "PENDING_SYNC" → PENDING_SYNC
)
```

### Room: The Local Database

**What is Room?** It's a library that makes SQLite (the database engine) easy to use in Android.

#### Step 1: Define the Schema

```kotlin
@Entity(
    tableName = "tasks",  // Database table name
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,  // Delete tasks if project deleted
        )
    ],
    indices = [
        Index(value = ["projectId"]),  // Speed up: WHERE projectId = X
        Index(value = ["syncStatus"]),  // Speed up: WHERE syncStatus = "PENDING_SYNC"
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,              // Unique identifier
    val projectId: String,       // Foreign key to projects
    val title: String,
    val description: String,
    val dueDate: LocalDateTime?, // Can be null
    val priority: String,        // Stored as "LOW", "MEDIUM", "HIGH"
    val isCompleted: Boolean,
    val createdAt: LocalDateTime,  // UTC time
    val updatedAt: LocalDateTime,  // UTC time
    val syncStatus: String,      // Stored as "PENDING_SYNC", "SYNCED", etc.
)
```

**Key concepts**:

| Concept        | Meaning                                      |
| -------------- | -------------------------------------------- |
| `@Entity`      | This class represents a database table       |
| `@PrimaryKey`  | Unique identifier for each row               |
| `tableName`    | What to call the table in the database       |
| `foreignKeys`  | Links to other tables (data integrity)       |
| `indices`      | Speeds up queries (like bookmarks in a book) |
| `?` (nullable) | Can be null (e.g., tasks without due dates)  |

**Indices explained**:

```kotlin
// Without index: Search entire table
// SELECT * FROM tasks WHERE syncStatus = "PENDING_SYNC"
// ❌ Check every single row (slow with 1000 tasks)

// With index: Direct lookup
// ✅ Database has a quick lookup table (fast even with 1000 tasks)
```

#### Step 2: Define Data Access (DAO)

```kotlin
@Dao  // Data Access Object
interface TaskDao {

    // ===== READ OPERATIONS =====

    /**
     * Observe all tasks in a project.
     * Flow means: Every time tasks change, this emits the new list.
     */
    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun observeTasksByProject(projectId: String): Flow<List<TaskEntity>>

    /**
     * Observe all tasks with optional filtering.
     * :filterCompleted is a parameter (null = ignore filter)
     */
    @Query(
        """
        SELECT * FROM tasks
        WHERE :filterCompleted IS NULL OR isCompleted = :filterCompleted
        ORDER BY updatedAt DESC
        """
    )
    fun observeAllTasks(filterCompleted: Boolean?): Flow<List<TaskEntity>>

    /**
     * Get one task.
     * suspend = non-blocking, doesn't freeze UI
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    /**
     * Get all tasks with a specific sync status.
     * Used by SyncWorker to find what needs syncing.
     */
    @Query("SELECT * FROM tasks WHERE syncStatus = :syncStatus")
    suspend fun getTasksBySyncStatus(syncStatus: String): List<TaskEntity>

    /**
     * Count how many tasks are pending sync.
     * Used in UI to show "Pending sync: 3"
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE syncStatus IN ('PENDING_SYNC', 'PENDING_DELETE')")
    fun observePendingSyncCount(): Flow<Int>


    // ===== WRITE OPERATIONS =====

    /**
     * Insert a new task.
     * suspend = async (doesn't block UI)
     */
    @Insert
    suspend fun insertTask(task: TaskEntity)

    /**
     * Update an existing task.
     * Must have matching @PrimaryKey id.
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Upsert = Update or Insert.
     * If task exists (by id), update it. Otherwise, insert it.
     * Used during sync when merging server data.
     */
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    /**
     * Delete a task.
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * Delete by ID (convenience method).
     */
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    /**
     * Update just the sync status.
     * Used after successful sync to mark SYNCED.
     */
    @Query("UPDATE tasks SET syncStatus = :syncStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSyncStatus(
        id: String,
        syncStatus: String,
        updatedAt: LocalDateTime
    )
}
```

**SQL queries explained**:

```sql
-- Get all tasks in a project
SELECT * FROM tasks WHERE projectId = :projectId
-- :projectId is replaced with actual value (e.g., "proj123")

-- Conditional filtering
WHERE :filterCompleted IS NULL OR isCompleted = :filterCompleted
-- If filterCompleted is null, show all tasks
-- If filterCompleted is true, show only completed tasks
-- If filterCompleted is false, show only pending tasks

-- Update specific column
UPDATE tasks SET syncStatus = :syncStatus WHERE id = :id
-- Only update syncStatus, leave other columns unchanged
```

#### Step 3: Create the Database

```kotlin
@Database(
    entities = [TaskEntity::class, ProjectEntity::class],
    version = 1,  // Schema version (increment when schema changes)
    exportSchema = true  // Export schemas for version tracking
)
@TypeConverters(LocalDateTimeConverter::class)  // Tell Room how to store LocalDateTime
abstract class TaskDatabase : RoomDatabase() {

    // Provide access to DAOs
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao

    companion object {
        private const val DB_NAME = "task_manager.db"

        @Volatile
        private var instance: TaskDatabase? = null

        /**
         * Singleton pattern: Only one database instance per app.
         * @Volatile ensures visibility across threads.
         */
        fun getInstance(context: Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): TaskDatabase {
            return Room.databaseBuilder(
                context,
                TaskDatabase::class.java,
                DB_NAME
            ).build()
        }
    }
}
```

**Singleton pattern explained**:

```kotlin
// Problem: Create multiple database connections
val db1 = TaskDatabase.getInstance(context)
val db2 = TaskDatabase.getInstance(context)
// ❌ Two separate connections (wastes memory, data inconsistency)

// Solution: Singleton (one instance)
val db = TaskDatabase.getInstance(context)  // First call: creates instance
val db2 = TaskDatabase.getInstance(context)  // Second call: returns same instance
// ✅ One connection (efficient, consistent)
```

### Local Data Source (Abstraction)

The local data source wraps Room DAOs:

```kotlin
class LocalTaskDataSource @Inject constructor(
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao,
) {

    /**
     * Observe all tasks in a project.
     *
     * This function:
     * 1. Calls DAO to get Flow<List<TaskEntity>>
     * 2. Maps each TaskEntity to Task (conversion)
     * 3. Returns Flow<List<Task>> to repository
     */
    fun observeTasksByProject(projectId: String): Flow<List<Task>> =
        taskDao.observeTasksByProject(projectId)
            .map { entities ->
                entities.map { it.toDomain() }  // Convert each entity to domain model
            }

    /**
     * Observe all tasks with optional filtering.
     */
    fun observeAllTasks(filterCompleted: Boolean?): Flow<List<Task>> =
        taskDao.observeAllTasks(filterCompleted)
            .map { entities -> entities.map { it.toDomain() } }

    /**
     * Get one task (not a flow).
     */
    suspend fun getTaskById(id: String): Task? =
        taskDao.getTaskById(id)?.toDomain()

    /**
     * Get all tasks with a specific sync status.
     * Used by sync worker.
     */
    suspend fun getTasksBySyncStatus(syncStatus: String): List<Task> =
        taskDao.getTasksBySyncStatus(syncStatus)
            .map { it.toDomain() }

    /**
     * Observe pending sync count.
     */
    fun observePendingSyncCount(): Flow<Int> =
        taskDao.observePendingSyncCount()

    // ===== WRITE OPERATIONS =====

    /**
     * Insert a task (convert domain → entity).
     */
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    /**
     * Update a task (convert domain → entity).
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    /**
     * Update just the sync status.
     */
    suspend fun updateTaskSyncStatus(id: String, syncStatus: String) {
        taskDao.updateSyncStatus(id, syncStatus, LocalDateTime.now())
    }

    /**
     * Delete a task permanently.
     */
    suspend fun deleteTaskById(id: String) {
        taskDao.deleteTaskById(id)
    }
}
```

**Why this abstraction?**

```kotlin
// ❌ Direct Room usage (tight coupling):
class Repository(private val dao: TaskDao) {
    fun getTasks() = dao.observeAllTasks().map { ... }
}
// If we swap Room for Datastore, entire repository changes

// ✅ Abstracted (loose coupling):
class Repository(private val localDataSource: LocalTaskDataSource) {
    fun getTasks() = localDataSource.observeAllTasks()
}
// If we swap Room for Datastore, only LocalTaskDataSource changes
```

### Mock Remote Data Source

For now, we simulate the server API:

```kotlin
class MockRemoteTaskDataSource @Inject constructor() {

    /**
     * Simulate network delay.
     * Makes it feel like a real API.
     */
    private suspend fun simulateNetworkDelay() {
        delay(500)  // Half second delay
    }

    /**
     * Simulate uploading tasks to server.
     * In reality: POST /api/tasks with JSON
     * Here: just pretend it succeeded
     */
    suspend fun uploadTasks(tasks: List<Task>): List<Task> {
        simulateNetworkDelay()

        // Simulate server accepting and returning tasks
        return tasks.map { it.copy(syncStatus = SyncStatus.SYNCED) }
    }

    /**
     * Simulate deleting a task on server.
     * In reality: DELETE /api/tasks/{id}
     * Here: just pretend it succeeded
     */
    suspend fun deleteTaskRemote(taskId: String) {
        simulateNetworkDelay()
        // Server confirmed deletion
    }
}
```

**Why mock?**

- Test sync logic without running a real server
- Build UI while backend is in development
- Later, swap with real Retrofit calls (same interface)

### TaskRepositoryImpl (The Orchestrator)

This is where offline-first magic happens:

```kotlin
class TaskRepositoryImpl @Inject constructor(
    private val localDataSource: LocalTaskDataSource,
    private val remoteDataSource: MockRemoteTaskDataSource,
) : TaskRepository {

    // ===== READS: Always from local DB =====

    /**
     * Observe tasks by project.
     * Always reads from local DB (offline-first principle).
     */
    override fun observeTasksByProject(projectId: String): Flow<List<Task>> =
        localDataSource.observeTasksByProject(projectId)


    // ===== WRITES: Immediate local, async sync =====

    /**
     * Create a task.
     *
     * Offline-first flow:
     * 1. Save to local DB immediately (PENDING_SYNC)
     * 2. Return to user instantly (no network wait)
     * 3. WorkManager syncs to server asynchronously
     */
    override suspend fun createTask(task: Task): Task {
        // Verify status is PENDING_SYNC (business rule)
        require(task.syncStatus == SyncStatus.PENDING_SYNC) {
            "New tasks must have PENDING_SYNC status"
        }

        // Save to local DB immediately
        localDataSource.insertTask(task)

        // Return instantly (user gets immediate feedback)
        return task

        // Note: We DON'T send to server here!
        // WorkManager will pick it up later
    }

    /**
     * Update a task.
     * Same offline-first pattern.
     */
    override suspend fun updateTask(task: Task) {
        require(task.syncStatus == SyncStatus.PENDING_SYNC) {
            "Updated tasks must have PENDING_SYNC status"
        }

        localDataSource.updateTask(task)
        // Return immediately, sync later
    }

    /**
     * Delete a task (soft delete).
     *
     * Why soft delete?
     * App could crash before syncing deletion to server.
     * With PENDING_DELETE, we never lose the deletion intent.
     */
    override suspend fun deleteTask(id: String) {
        val task = localDataSource.getTaskById(id) ?: return

        // Mark as PENDING_DELETE (not actually deleted yet)
        val updatedTask = task.copy(
            syncStatus = SyncStatus.PENDING_DELETE,
            updatedAt = LocalDateTime.now(),
        )

        // Save the deletion intent
        localDataSource.updateTask(updatedTask)

        // WorkManager will sync this deletion to server
    }

    /**
     * Hard delete (internal only, after sync succeeds).
     */
    override suspend fun hardDeleteTask(id: String) {
        localDataSource.deleteTaskById(id)
    }


    // ===== SYNC: Called by WorkManager every 15 minutes =====

    /**
     * Sync pending changes with server.
     *
     * Algorithm:
     * 1. Find all PENDING_SYNC tasks
     * 2. Send to server (mock or real API)
     * 3. Mark as SYNCED if successful
     * 4. Find all PENDING_DELETE tasks
     * 5. Send deletes to server
     * 6. Hard-delete locally if successful
     *
     * If anything fails: Leave as PENDING_*, retry next time
     */
    override suspend fun syncPendingChanges(): Int {
        var syncedCount = 0

        try {
            // ===== SYNC CREATES/UPDATES =====

            // Find all tasks waiting to be synced
            val pendingSyncTasks = localDataSource.getTasksBySyncStatus(
                SyncStatus.PENDING_SYNC.name
            )

            if (pendingSyncTasks.isNotEmpty()) {
                try {
                    // Send to server (or mock server)
                    val syncedTasks = remoteDataSource.uploadTasks(pendingSyncTasks)

                    // Mark as SYNCED locally
                    syncedTasks.forEach { synced ->
                        localDataSource.updateTaskSyncStatus(
                            synced.id,
                            SyncStatus.SYNCED.name
                        )
                    }

                    syncedCount += syncedTasks.size
                } catch (e: Exception) {
                    // Network failed - leave as PENDING_SYNC
                    // WorkManager will retry
                    throw e
                }
            }

            // ===== SYNC DELETES =====

            val pendingDeleteTasks = localDataSource.getTasksBySyncStatus(
                SyncStatus.PENDING_DELETE.name
            )

            pendingDeleteTasks.forEach { toDelete ->
                try {
                    // Tell server to delete
                    remoteDataSource.deleteTaskRemote(toDelete.id)

                    // Server confirmed - hard delete locally
                    hardDeleteTask(toDelete.id)

                    syncedCount++
                } catch (e: Exception) {
                    // Leave as PENDING_DELETE, retry next time
                    // Important: Don't hard-delete if server failed
                }
            }

        } catch (e: Exception) {
            // If uploads all failed, bubble exception to WorkManager
            // WorkManager will retry with backoff
            if (syncedCount == 0) throw e
        }

        return syncedCount
    }
}
```

**Key offline-first principles**:

1. **Local DB is source of truth**
   - UI reads from local DB
   - Server is a backup

2. **Writes are instant**
   - Save locally immediately
   - Sync to server asynchronously
   - User gets instant feedback

3. **Network failures don't corrupt data**
   - PENDING_SYNC/DELETE track intent
   - App can crash, data is safe
   - Retry seamlessly

---

## Step 3: Presentation Layer (The UI)

### Purpose

The presentation layer:

- Shows data to the user (Compose)
- Responds to user actions
- Holds UI state (ViewModel)
- Navigates between screens

### Understanding ViewModel

**Problem**: Losing data on configuration changes

```kotlin
// ❌ WITHOUT ViewModel:
var tasks = listOf<Task>()  // Activity variable

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    tasks = loadTasks()  // Load from database
}

// User rotates screen
// onCreate called again
// tasks lost, reload from DB
// ❌ Inefficient, data lost temporarily
```

**Solution**: ViewModel survives screen rotation

```kotlin
// ✅ WITH ViewModel:
class TaskViewModel : ViewModel() {
    var tasks = listOf<Task>()  // ViewModel member (survives rotation)
}

// User rotates screen
// ViewModel NOT recreated
// tasks still in memory
// ✅ Data preserved!
```

### UiState Pattern

**Single source of truth**: One state object holds all screen data

```kotlin
/**
 * All data the TaskListScreen needs.
 * Immutable (can't be changed directly).
 * Updated through copy() with new values.
 */
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),       // The actual tasks
    val isLoading: Boolean = false,            // Loading spinner?
    val error: String? = null,                 // Error message?
    val selectedProjectId: String? = null,     // Which project?
    val filterCompleted: Boolean? = null,      // null=all, true=done, false=pending
    val sortBy: TaskSort = TaskSort.CREATED_DESC,  // How to sort
    val pendingSyncCount: Int = 0,             // How many await sync?
    val isSyncing: Boolean = false,            // Sync in progress?
    val isOnline: Boolean = true,              // Internet available?
)
```

**Why immutable?**

```kotlin
// ❌ Mutable (problematic):
state.tasks.add(newTask)  // Direct mutation
// Hard to track when state changed
// UI might not update (no notification)

// ✅ Immutable (good):
state = state.copy(tasks = state.tasks + newTask)  // Create new state
// Clear when state changed
// Easy to notify UI (just compare object reference)
```

### Events Pattern

**User interactions** are represented as sealed class events:

```kotlin
/**
 * All possible user actions on TaskListScreen.
 * Sealed class = compiler ensures all cases handled.
 */
sealed class TaskListUiEvent {
    data class FilterByCompletion(val completed: Boolean?) : TaskListUiEvent()
    data class SortBy(val sort: TaskSort) : TaskListUiEvent()
    data class DeleteTask(val taskId: String) : TaskListUiEvent()
    data class CompleteTask(val taskId: String) : TaskListUiEvent()
    object ClearError : TaskListUiEvent()
    object ManualSync : TaskListUiEvent()
}
```

**Why sealed?**

```kotlin
// ❌ Regular class:
viewModel.handleEvent("delete_task", mapOf("id" to "123"))
// String is error-prone, no type checking

// ✅ Sealed class:
viewModel.handleEvent(TaskListUiEvent.DeleteTask("123"))
// Type-safe, IDE auto-complete, compiler checks
```

### ViewModel Implementation

```kotlin
@HiltViewModel  // Hilt creates this, injects dependencies
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val taskRepository: TaskRepository,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    // ===== STATE =====

    /**
     * Private mutable version (ViewModel only).
     * UI can't modify it.
     */
    private val _uiState = MutableStateFlow(TaskListUiState())

    /**
     * Public read-only version (exposed to UI).
     * UI can observe but not modify.
     */
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()


    // ===== INITIALIZATION =====

    /**
     * init block runs when ViewModel created.
     * Set up all observers.
     */
    init {
        loadTasks()
        observePendingSyncCount()
        observeConnectivity()
    }


    // ===== EVENT HANDLING =====

    /**
     * Handle user actions.
     * Called from UI: viewModel.onEvent(TaskListUiEvent.DeleteTask(id))
     */
    fun onEvent(event: TaskListUiEvent) {
        when (event) {
            is TaskListUiEvent.DeleteTask -> deleteTask(event.taskId)
            is TaskListUiEvent.CompleteTask -> completeTask(event.taskId)
            is TaskListUiEvent.FilterByCompletion -> filterByCompletion(event.completed)
            is TaskListUiEvent.SortBy -> sortBy(event.sort)
            is TaskListUiEvent.ClearError -> clearError()
            is TaskListUiEvent.ManualSync -> manualSync()
        }
    }


    // ===== PRIVATE IMPLEMENTATIONS =====

    /**
     * Load tasks from repository and observe changes.
     */
    private fun loadTasks() {
        viewModelScope.launch {
            // Show loading spinner
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Get use case that handles filtering/sorting
                getTasksUseCase(
                    filterCompleted = _uiState.value.filterCompleted,
                    sortBy = _uiState.value.sortBy,
                )
                    // If error occurs, update state with error
                    .catch { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Unknown error"
                            )
                        }
                    }
                    // For each emission from use case, update state
                    .collect { tasks ->
                        _uiState.update {
                            it.copy(
                                tasks = tasks,
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load tasks"
                    )
                }
            }
        }
    }

    /**
     * Delete a task.
     * User sees it disappear immediately (optimistic update).
     */
    private fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // Call use case (marks PENDING_DELETE)
                deleteTaskUseCase(taskId)

                // loadTasks() observer will see the change
                // UI updates automatically
            } catch (e: Exception) {
                // Show error
                _uiState.update {
                    it.copy(error = "Failed to delete: ${e.message}")
                }
            }
        }
    }

    /**
     * Mark a task complete/incomplete.
     */
    private fun completeTask(taskId: String) {
        viewModelScope.launch {
            try {
                // Find the task in current state
                val task = _uiState.value.tasks.find { it.id == taskId }
                    ?: return@launch

                // Call use case to toggle completion
                updateTaskUseCase(
                    id = taskId,
                    isCompleted = !task.isCompleted,
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update: ${e.message}")
                }
            }
        }
    }

    /**
     * Observe pending sync count.
     * Displays "Pending sync: 3" in UI.
     */
    private fun observePendingSyncCount() {
        viewModelScope.launch {
            taskRepository.observePendingSyncCount()
                .catch { error -> Timber.e(error) }  // Log but don't show error
                .collect { count ->
                    _uiState.update { it.copy(pendingSyncCount = count) }
                }
        }
    }

    /**
     * Observe device online/offline state.
     * Shows "You are offline" banner.
     */
    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.observeIsOnline()
                .collect { isOnline ->
                    _uiState.update { it.copy(isOnline = isOnline) }
                }
        }
    }

    /**
     * Clear error message (user taps X on snackbar).
     */
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * User taps "sync now" button.
     */
    private fun manualSync() {
        // TODO: Trigger manual sync
    }
}
```

**Key ViewModel concepts**:

| Concept                 | Meaning                                        |
| ----------------------- | ---------------------------------------------- |
| `viewModelScope.launch` | Start coroutine that cancels when VM destroyed |
| `_uiState.update()`     | Type-safe state update                         |
| `.asStateFlow()`        | Make mutable flow read-only for UI             |
| `init { }`              | Set up observers when VM created               |

### Compose Screens

#### Simple Components

**SyncStatusIndicator.kt**:

```kotlin
/**
 * Shows sync status as a colored circle with icon.
 * - Green ✓: SYNCED
 * - Blue ☁: PENDING_SYNC (uploading)
 * - Red !: PENDING_DELETE
 */
@Composable
fun SyncStatusIndicator(syncStatus: SyncStatus) {
    val (color, icon) = when (syncStatus) {
        SyncStatus.SYNCED -> Color.Green to Icons.Default.Check
        SyncStatus.PENDING_SYNC -> Color.Blue to Icons.Default.CloudUpload
        SyncStatus.PENDING_DELETE -> Color.Red to Icons.Default.Warning
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .background(color, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Sync status",
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
    }
}
```

**TaskCard.kt** (Reusable component):

```kotlin
/**
 * Displays one task as a card.
 * Shows: title, priority, due date, sync status, delete button.
 */
@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: (String) -> Unit,  // User taps checkbox
    onDelete: (String) -> Unit,          // User taps delete
    onClick: (String) -> Unit,           // User taps card
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .clickable { onClick(task.id) }  // Navigate to detail
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Checkbox
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggleComplete(task.id) }
        )

        // Content (title, priority, due date)
        Column(modifier = Modifier.weight(1f)) {
            // Title (strikethrough if completed)
            Text(
                text = task.title,
                textDecoration = if (task.isCompleted)
                    TextDecoration.LineThrough else TextDecoration.None,
            )

            // Priority & due date
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriorityBadge(task.priority)  // Color badge

                // Due date (if set)
                task.dueDate?.let { dueDate ->
                    Text(
                        text = "Due: ${formatDate(dueDate)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Sync status icon
        SyncStatusIndicator(task.syncStatus)

        // Delete button
        IconButton(onClick = { onDelete(task.id) }) {
            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
        }
    }
}
```

#### Full Screen

**TaskListScreen.kt**:

```kotlin
/**
 * Full task list screen.
 *
 * Data flow:
 * 1. Hilt provides ViewModel
 * 2. collectAsStateWithLifecycle converts Flow → Compose State
 * 3. State change triggers recomposition
 * 4. UI calls viewModel.onEvent() on user action
 */
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),  // Hilt injection
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToCreateTask: () -> Unit,
) {
    // ===== CONVERT FLOW TO COMPOSE STATE =====

    /**
     * collectAsStateWithLifecycle():
     * - Converts Flow<TaskListUiState> to Compose State<TaskListUiState>
     * - Lifecycle-aware (stops collecting when screen hidden)
     * - Prevents memory leaks
     */
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    // For showing snackbars (error messages)
    val snackbarHostState = remember { SnackbarHostState() }


    // ===== SIDE EFFECTS =====

    /**
     * LaunchedEffect: Run code once when something changes.
     * Here: Show snackbar when error appears.
     */
    LaunchedEffect(uiState.value.error) {
        uiState.value.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
        }
    }


    // ===== UI =====

    Scaffold(  // Basic layout structure
        topBar = {
            TopAppBar(
                title = { Text("Tasks") },
                actions = {
                    // Sync button
                    IconButton(
                        onClick = { viewModel.onEvent(TaskListUiEvent.ManualSync) }
                    ) {
                        Icon(Icons.Default.Sync, "Sync")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToCreateTask() },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("New Task") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // Main content
        Box(modifier = Modifier.padding(paddingValues)) {

            // Show different content based on state
            when {
                // Loading state
                uiState.value.isLoading && uiState.value.tasks.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Empty state
                uiState.value.tasks.isEmpty() -> {
                    Text(
                        text = "No tasks yet. Create one to get started!",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Tasks list
                else -> {
                    Column {
                        // Offline banner
                        if (!uiState.value.isOnline) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Red.copy(alpha = 0.8f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📡 You are offline. Changes will sync when online.",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Filter & sort controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            IconButton(onClick = { /* TODO: Filter menu */ }) {
                                Icon(Icons.Default.FilterList, "Filter")
                            }

                            // Pending sync indicator
                            Text(
                                text = "Pending sync: ${uiState.value.pendingSyncCount}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        // Task list
                        LazyColumn(
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = uiState.value.tasks,
                                key = { it.id }  // Efficient recomposition
                            ) { task ->
                                TaskCard(
                                    task = task,
                                    onToggleComplete = { taskId ->
                                        viewModel.onEvent(
                                            TaskListUiEvent.CompleteTask(taskId)
                                        )
                                    },
                                    onDelete = { taskId ->
                                        viewModel.onEvent(
                                            TaskListUiEvent.DeleteTask(taskId)
                                        )
                                    },
                                    onClick = { taskId ->
                                        onNavigateToTaskDetail(taskId)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Sync overlay (when syncing in progress)
            if (uiState.value.isSyncing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
```

**Compose state management flow**:

```
ViewModel holds: StateFlow<TaskListUiState>
    ↓
Compose observes: .collectAsStateWithLifecycle()
    ↓
State changes, Compose detects
    ↓
Affected composables re-run (recomposition)
    ↓
UI displays new state
    ↓
User interacts: Button click
    ↓
Calls viewModel.onEvent(event)
    ↓
ViewModel updates state
    ↓
Flow emits new state
    ↓
Compose recomposes
    ↓
(Cycle repeats)
```

### Navigation

**NavGraph.kt**:

```kotlin
/**
 * Defines all screens and how to navigate between them.
 * NavHost = FragmentManager for Compose
 * composable = one screen
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "task_list"  // First screen shown
    ) {
        // Task list screen
        composable("task_list") {
            TaskListScreen(
                onNavigateToTaskDetail = { taskId ->
                    navController.navigate("task_detail/$taskId")
                },
                onNavigateToCreateTask = {
                    navController.navigate("task_detail/new")  // "new" = create mode
                }
            )
        }

        // Task detail/edit screen
        composable("task_detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: "new"

            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }  // Go back
            )
        }
    }
}
```

**MainActivity.kt**:

```kotlin
@AndroidEntryPoint  // Enable Hilt injection
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TaskManagerTheme {
                Surface {
                    val navController = rememberNavController()
                    NavGraph(navController)
                }
            }
        }
    }
}
```

---

## Step 4: Background Sync (The Magic)

### Understanding WorkManager

**Problem**: App is killed, sync never happens.

```kotlin
// ❌ Without WorkManager:
button.setOnClickListener {
    sync()  // Only runs if app is open
}
// User closes app → sync never runs

// ✅ With WorkManager:
// Sync runs every 15 min, even if app closed
```

### SyncWorker

```kotlin
/**
 * Worker = unit of background work.
 * CoroutineWorker = uses coroutines (non-blocking).
 * WorkManager calls doWork() on a background thread.
 */
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncTasksUseCase: SyncTasksUseCase,
) : CoroutineWorker(context, params) {

    /**
     * This function runs in the background (WorkManager schedule).
     * It's called periodically (e.g., every 15 minutes).
     */
    override suspend fun doWork(): Result = try {
        Timber.d("SyncWorker: Starting sync...")

        // Call use case to sync tasks
        val syncedCount = syncTasksUseCase()

        Timber.d("SyncWorker: Successfully synced $syncedCount tasks")

        // Sync successful, don't retry
        Result.success()

    } catch (e: Exception) {
        // Sync failed - classify the error
        when (e) {
            is NetworkException -> {
                // Transient error (network timeout, no internet, etc.)
                // Try again with exponential backoff
                Timber.w("SyncWorker: Network error, will retry", e)
                Result.retry()
            }
            is AuthException -> {
                // Permanent error (401, 403 - user not authenticated)
                // Don't retry without user action
                Timber.e("SyncWorker: Auth error, won't retry", e)
                Result.failure()
            }
            else -> {
                // Unknown error - retry a few times
                if (runAttemptCount < 3) {
                    Timber.e("SyncWorker: Unknown error (attempt ${runAttemptCount}), will retry", e)
                    Result.retry()
                } else {
                    Timber.e("SyncWorker: Too many attempts, giving up", e)
                    Result.failure()
                }
            }
        }
    }
}

// Custom exceptions for error classification
class NetworkException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

class AuthException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
```

**Result types**:

| Result      | Meaning     | Action                                     |
| ----------- | ----------- | ------------------------------------------ |
| `success()` | Sync worked | Done, next run in 15 min                   |
| `retry()`   | Try again   | Exponential backoff (1 min, 5 min, 30 min) |
| `failure()` | Give up     | Mark work failed                           |

### Scheduling

**WorkerModule.kt**:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    /**
     * Schedule the periodic sync work.
     * Runs every 15 minutes, even if app closed.
     */
    @Provides
    @Singleton
    fun scheduleSyncWork(workManager: WorkManager): WorkManager {

        // Build the work request
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,  // Every 15 minutes
            TimeUnit.MINUTES,
            flexInterval = 5,  // Can run between 10-15 min (battery optimization)
            TimeUnit.MINUTES,
        )
            // Exponential backoff for failures
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                initialDelay = 1,   // First retry after 1 min
                timeUnit = TimeUnit.MINUTES,
            )
            // Only sync if network available (save battery)
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // Enqueue (schedule) the work
        workManager.enqueueUniquePeriodicWork(
            "sync_tasks_work",
            ExistingPeriodicWorkPolicy.KEEP,  // Don't schedule duplicate
            syncWorkRequest
        )

        Timber.d("Sync work scheduled: every 15 min")
        return workManager
    }
}
```

**How scheduling works**:

```
App starts
    ↓
WorkManager initialized
    ↓
WorkManager schedules SyncWorker to run every 15 minutes
    ↓
15 minutes pass (or immediately if time elapsed)
    ↓
Device online AND network available?
    ↓
YES: Start SyncWorker
    ↓
SyncWorker.doWork() runs
    ↓
Queries local DB for PENDING_SYNC tasks
    ↓
Sends to API
    ↓
Updates local DB to SYNCED
    ↓
Returns Result.success()
    ↓
WorkManager completes work
    ↓
Schedule next run (15 min later)
```

### Handling Offline

**ConnectivityObserver.kt**:

```kotlin
/**
 * Watches device online/offline state.
 * Emits true when online, false when offline.
 */
class ConnectivityObserver @Inject constructor(
    private val context: Context
) {

    /**
     * Observe connectivity state.
     * Returns Flow<Boolean> that emits whenever network state changes.
     */
    fun observeIsOnline(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
            ?: run {
                trySend(false)  // No connectivity manager = offline
                close()
                return@callbackFlow
            }

        // Register callback for network changes
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network came online
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Network went offline
                val isOnline = connectivityManager.activeNetwork != null
                trySend(isOnline)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        // Emit current state
        val isOnline = connectivityManager.activeNetwork != null
        trySend(isOnline)

        // Cleanup when Flow is cancelled
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
```

**In ViewModel**:

```kotlin
private fun observeConnectivity() {
    viewModelScope.launch {
        connectivityObserver.observeIsOnline()
            .collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
            }
    }
}
```

**In UI**:

```kotlin
// Show offline banner when offline
if (!uiState.value.isOnline) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red.copy(alpha = 0.8f))
            .padding(12.dp)
    ) {
        Text(
            text = "📡 You are offline. Changes will sync when online.",
            color = Color.White
        )
    }
}
```

---

## How Everything Works Together

### Complete Data Flow Example

**Scenario**: User is offline, creates a task, then goes online.

```
1. USER CREATES TASK (OFFLINE)
   └─ UI: TaskDetailScreen
      └─ User enters title, taps Save
         └─ onSaveTask() called
            └─ viewModel.onEvent(SaveTask(...))

2. VIEWMODEL HANDLES EVENT
   └─ viewModel.onEvent() receives SaveTask event
      └─ Calls createTaskUseCase.invoke()
         └─ Validates title (not empty)
            └─ Generates UUID locally
               └─ Creates Task with PENDING_SYNC status
                  └─ Calls taskRepository.createTask()

3. REPOSITORY SAVES LOCALLY (INSTANT)
   └─ taskRepository.createTask(task)
      └─ localDataSource.insertTask(task)
         └─ taskDao.insertTask(taskEntity)
            └─ Room inserts into "tasks" table
               └─ taskEntity.syncStatus = "PENDING_SYNC"
                  └─ Database emits new list via Flow

4. UI UPDATES IMMEDIATELY
   └─ Room emits List<TaskEntity> via Flow
      └─ LocalDataSource maps to List<Task>
         └─ Repository Flow emits
            └─ ViewModel collector receives
               └─ _uiState.update { it.copy(tasks = newTasks) }
                  └─ StateFlow emits new state
                     └─ collectAsStateWithLifecycle() updates Compose State
                        └─ UI recomposes
                           └─ Task appears in list with 🔵 blue cloud (PENDING_SYNC)
                              └─ ✅ INSTANT feedback (no network wait!)

5. USER GOES ONLINE
   └─ ConnectivityObserver detects network available
      └─ observeIsOnline() Flow emits true
         └─ ViewModel updates isOnline state
            └─ UI removes "offline" banner

6. WORKMANAGER PERIODIC TRIGGER (or next 15 min)
   └─ WorkManager timer ticks
      └─ Checks constraints: isOnline? Yes ✓
         └─ Starts SyncWorker.doWork()

7. SYNCWORKER SYNCS
   └─ SyncWorker.doWork() called
      └─ Calls syncTasksUseCase()
         └─ taskRepository.syncPendingChanges()
            └─ Finds all PENDING_SYNC tasks from local DB
               └─ Calls remoteDataSource.uploadTasks(tasks)
                  └─ Mock API simulates upload (500ms delay)
                     └─ Returns tasks with SYNCED status

8. LOCAL DB UPDATED
   └─ Repository updates task status to SYNCED
      └─ taskDao.updateSyncStatus(id, "SYNCED")
         └─ Room updates database
            └─ Emits new list via Flow

9. UI UPDATES AGAIN
   └─ (Same flow as step 4)
      └─ Task appears with ✅ green checkmark (SYNCED)
         └─ User sees sync confirmation

✅ TASK FULLY SYNCED (WORK complete)
```

### State at Each Point

```
Initial state:
{
  tasks: [],
  isLoading: false,
  error: null,
  isOnline: false,      // Offline
  pendingSyncCount: 0,
  isSyncing: false,
}

After creating task (offline):
{
  tasks: [Task(id="123", title="Buy milk", syncStatus=PENDING_SYNC)],
  isLoading: false,
  error: null,
  isOnline: false,      // Still offline
  pendingSyncCount: 1,  // 1 task pending
  isSyncing: false,
}

After going online:
{
  tasks: [Task(id="123", title="Buy milk", syncStatus=PENDING_SYNC)],
  isLoading: false,
  error: null,
  isOnline: true,       // Now online
  pendingSyncCount: 1,
  isSyncing: false,
}

During sync:
{
  tasks: [Task(id="123", title="Buy milk", syncStatus=PENDING_SYNC)],
  isLoading: false,
  error: null,
  isOnline: true,
  pendingSyncCount: 1,
  isSyncing: true,      // Sync in progress
}

After sync completes:
{
  tasks: [Task(id="123", title="Buy milk", syncStatus=SYNCED)],
  isLoading: false,
  error: null,
  isOnline: true,
  pendingSyncCount: 0,  // 0 tasks pending
  isSyncing: false,
}
```

---

## Common Beginner Mistakes

### 1. Not Understanding Flow vs suspend

**❌ Wrong**:

```kotlin
// Thinking Flow is like suspend (one-time fetch)
fun getTasks(): Flow<List<Task>> {
    return listOf(task1, task2).asFlow()  // Returns immediately
}

// Later:
val task = getTasks().first()  // Gets stale data
```

**✅ Correct**:

```kotlin
// Flow is reactive (continuous updates)
fun observeTasks(): Flow<List<Task>> {
    return database.observeAllTasks()  // Emits on every DB change
}

// Collect all updates:
observeTasks().collect { tasks ->
    updateUI(tasks)  // Runs every time DB changes
}
```

### 2. Not Understanding Offline-First

**❌ Wrong**:

```kotlin
// Try to sync immediately
suspend fun createTask(task: Task) {
    val response = api.createTask(task)  // Network call first!
    database.insertTask(response)  // Then save
}
// Problem: If network fails, task lost
```

**✅ Correct**:

```kotlin
// Save locally first
suspend fun createTask(task: Task) {
    task = task.copy(syncStatus = PENDING_SYNC)
    database.insertTask(task)  // Immediate, safe
    // WorkManager syncs later
}
```

### 3. Modifying State Directly

**❌ Wrong**:

```kotlin
// Direct mutation
_uiState.value.tasks.add(newTask)  // Don't do this!
// UI might not update (Flow doesn't know it changed)
```

**✅ Correct**:

```kotlin
// Immutable update
_uiState.update { currentState ->
    currentState.copy(
        tasks = currentState.tasks + newTask
    )
}
// Flow knows state changed, notifies UI
```

### 4. Not Handling Errors in Coroutines

**❌ Wrong**:

```kotlin
viewModelScope.launch {
    val tasks = getTasks()  // What if this crashes?
    updateUI(tasks)
}
// No error handling, user sees nothing
```

**✅ Correct**:

```kotlin
viewModelScope.launch {
    try {
        val tasks = getTasks()
        updateUI(tasks)
    } catch (e: Exception) {
        _uiState.update { it.copy(error = e.message) }
    }
}
```

### 5. Not Understanding Dependency Injection

**❌ Wrong**:

```kotlin
class ViewModel {
    private val repository = TaskRepository()  // Hard-coded!
    // Can't test (always uses real repository)
    // Can't swap for mock
}
```

**✅ Correct**:

```kotlin
class ViewModel @Inject constructor(
    private val repository: TaskRepository  // Injected
) {
    // Can be tested (swap with mock)
    // Can be changed (swap for real/mock implementation)
}
```

### 6. Not Using Keys in LazyColumn

**❌ Wrong**:

```kotlin
LazyColumn {
    items(tasks) { task ->
        TaskCard(task = task)
    }
}
// Compose doesn't know which item is which
// Recomposes all items even if only one changed
```

**✅ Correct**:

```kotlin
LazyColumn {
    items(
        items = tasks,
        key = { it.id }  // Tell Compose how to identify items
    ) { task ->
        TaskCard(task = task)
    }
}
// Only changed items recompose (efficient)
```

### 7. Not Understanding SyncStatus

**❌ Wrong**:

```kotlin
// Immediately synced to server
fun deleteTask(id: String) {
    database.deleteTask(id)  // Hard delete
}
// What if server says "no"? Data lost.
```

**✅ Correct**:

```kotlin
// Soft delete with tracking
fun deleteTask(id: String) {
    val task = database.getTask(id)
    task = task.copy(syncStatus = PENDING_DELETE)
    database.updateTask(task)  // Mark for deletion
    // WorkManager syncs later
    // Only hard-delete after server confirms
}
```

---

## Testing Your App

### Unit Testing (Test Business Logic)

```kotlin
// Test use case in isolation
class CreateTaskUseCaseTest {

    @Test
    fun `createTask validates title not empty`() {
        val useCase = CreateTaskUseCase(mockRepository)

        // Should throw if title is empty
        assertThrows<IllegalArgumentException> {
            runBlocking {
                useCase.invoke(
                    projectId = "proj1",
                    title = "",  // Empty!
                    description = "Test"
                )
            }
        }
    }

    @Test
    fun `createTask marks task as PENDING_SYNC`() {
        val mockRepository = mockk<TaskRepository>()
        val useCase = CreateTaskUseCase(mockRepository)

        runBlocking {
            useCase.invoke(
                projectId = "proj1",
                title = "Buy milk",
                description = ""
            )
        }

        // Verify repository was called with PENDING_SYNC
        coVerify {
            mockRepository.createTask(
                match {
                    it.syncStatus == SyncStatus.PENDING_SYNC
                }
            )
        }
    }
}
```

### ViewModel Testing

```kotlin
class TaskListViewModelTest {

    @Test
    fun `loadTasks updates ui state with tasks`() {
        val mockUseCase = mockk<GetTasksUseCase>()
        val tasks = listOf(Task(...), Task(...))

        // Mock use case to return tasks
        coEvery { mockUseCase() } returns flowOf(tasks)

        val viewModel = TaskListViewModel(mockUseCase)

        // Assert state was updated
        assertEquals(
            viewModel.uiState.value.tasks,
            tasks
        )
    }
}
```

### Testing Flow/Coroutines

Use **Turbine** library for testing Flows:

```kotlin
@Test
fun `observeAllTasks emits updates`() = runTest {
    val repository = mockk<TaskRepository>()
    val task1 = Task(id="1", title="First")
    val task2 = Task(id="2", title="Second")

    coEvery { repository.observeAllTasks(...) } returns flowOf(
        listOf(task1),
        listOf(task1, task2)
    )

    repository.observeAllTasks().test {
        // First emission
        assertEquals(listOf(task1), awaitItem())

        // Second emission
        assertEquals(listOf(task1, task2), awaitItem())

        // No more emissions
        awaitComplete()
    }
}
```

---

## Next Steps

### What to Build Next

1. **TaskDetailScreen**
   - Edit existing task
   - Create new task
   - Date picker for due dates
   - Priority selector

2. **ProjectsScreen**
   - List all projects
   - Create/edit projects
   - Delete projects

3. **Push Notifications**
   - Remind when task due
   - Notify when task synced
   - Notification channels

4. **Real API Integration**
   - Replace MockRemoteTaskDataSource with Retrofit
   - Handle API errors properly
   - Implement conflict resolution

5. **Testing**
   - Unit tests for use cases
   - ViewModel tests
   - Integration tests with Room
   - UI tests with Compose

### Learning Resources

- **Room**: https://developer.android.com/training/data-storage/room
- **Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html
- **Jetpack Compose**: https://developer.android.com/compose
- **WorkManager**: https://developer.android.com/topic/libraries/architecture/workmanager
- **Hilt**: https://developer.android.com/training/dependency-injection/hilt-android
- **MVVM**: https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel

### Advanced Topics (After Mastering Basics)

1. **Database Migrations**
   - Evolving schema (adding columns, changing types)
   - Data migrations (transforming old data)

2. **Conflict Resolution**
   - Multiple devices editing same task
   - Last-write-wins strategy
   - User-prompted resolution

3. **Offline Queue**
   - Queue operations while offline
   - Replay queue when online
   - Handle operation failures

4. **Push Notifications**
   - Firebase Cloud Messaging
   - Notification channels
   - Deep linking to tasks

5. **Performance**
   - Database query optimization
   - Compose recomposition analysis
   - Memory profiling

---

## Summary

### What You've Learned

✅ **Architecture Patterns**

- Clean Architecture (3 layers)
- MVVM (Model-View-ViewModel)
- Offline-first (local DB is source of truth)

✅ **Tools & Libraries**

- Room (local database)
- Coroutines (async programming)
- Flow (reactive data)
- Jetpack Compose (modern UI)
- WorkManager (background sync)
- Hilt (dependency injection)

✅ **Key Concepts**

- SyncStatus (PENDING_SYNC, SYNCED, PENDING_DELETE)
- Soft deletes (never lose user intent)
- Reactive UI (automatic updates)
- Type-safe code (compile-time errors prevented)
- Testability (mock dependencies)

✅ **Real-World Skills**

- Building apps that work offline
- Syncing data with servers
- Handling network failures gracefully
- Writing maintainable, testable code
- Creating responsive user interfaces

### The Big Picture

You've built an app that:

- 📱 Works completely offline
- 🚀 Responds instantly to user actions
- 🔄 Syncs automatically in the background
- 🛡️ Never loses user data
- 🧪 Is highly testable
- 📈 Can scale to thousands of tasks

All using industry-standard Android patterns and tools!

### Next Steps

1. **Build TaskDetailScreen** to put your ViewModel skills to practice
2. **Add real Retrofit API** integration
3. **Implement push notifications** for due date reminders
4. **Write comprehensive tests** for your code
5. **Deploy to Google Play** and get real users!

---

## Glossary

**API**: Application Programming Interface; a way for apps to talk to servers

**Async/Asynchronous**: Code that doesn't block (doesn't freeze UI)

**Clean Architecture**: Separating code into layers (Domain, Data, Presentation)

**Compose**: Jetpack Compose; modern way to build Android UIs

**Coroutines**: Kotlin feature for writing async code that looks sync

**DAO**: Data Access Object; interface for database queries

**Dependency Injection**: Pattern where objects are provided rather than created

**Entity**: Database representation of data (different from domain model)

**Flow**: Reactive stream that emits values over time

**Hilt**: Framework for managing dependency injection

**MVVM**: Model-View-ViewModel architectural pattern

**Offline-First**: Design where local DB is source of truth, not server

**Repository**: Layer that abstracts where data comes from (DB, API, etc.)

**Room**: Android library for local SQLite databases

**StateFlow**: A Flow that holds and emits state

**SyncStatus**: Enum tracking if task is PENDING_SYNC, SYNCED, or PENDING_DELETE

**Suspend Function**: Coroutine function that can be paused without blocking

**ViewModel**: Class that survives configuration changes and holds UI state

**WorkManager**: Framework for scheduling background tasks
