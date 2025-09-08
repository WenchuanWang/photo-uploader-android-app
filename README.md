# Photo Uploader App

A modular Android app demonstrating photo selection, local persistence, and background upload to Firebase Storage. The project is structured for reuse and testability, with a clean separation of concerns across modules.

## Screenshots
| Empty Photo List | Uploading | Upload Success | Upload Partial Failure | Upload Queued |
| --- | --- | --- | --- | --- |
| <img height=400 src=https://github.com/user-attachments/assets/89139263-8f31-4490-b57c-010984926222 /> | <img height=400 src=https://github.com/user-attachments/assets/3865abce-8cfd-4df5-af81-300fa720012c /> | <img height=400 src=https://github.com/user-attachments/assets/d9b75280-13c3-45e5-a31c-748842c5581b/> | <img height=400 src=https://github.com/user-attachments/assets/5c524ced-4cfa-42a9-a41b-68a3e51ea860 /> | <img height=400 src=https://github.com/user-attachments/assets/c3216d6d-89c8-48f5-813f-be74b9bb7ffd /> |


Uploads will show as Failed if the network drops, but they’ll automatically resume once the connection is back.

https://github.com/user-attachments/assets/cad999be-3a2d-4753-a8cf-f9c66e474b3f


Firebase Storage
<img width="1330" height="496" alt="Screenshot 2025-09-08 at 3 30 32 pm" src="https://github.com/user-attachments/assets/420f857b-5086-4942-9127-c5ed22d47f63" />


## Key Features

- Select photos and show upload progress via Compose
- Persist photos locally in Room with status tracking
- Background upload via WorkManager and Firebase Storage
- Hilt-based DI across modules
- Use Kotlin Coroutines and Flow to handle background tasks, database operations, and asynchronous UI updates
- Comprehensive unit tests across modules
- Simple UI tests for a key user flow

## UI

- Compose UI with light/dark mode support
- Select up to 6 photos from the device Gallery at once
- Global top loading bar indicating overall upload activity
- Status-driven colors/icons based on `UploadStatus` (Queued/Uploading/Success/Failed)

## Functionality

- On selection, photos are copied into app storage and inserted into Room
- Uploads are enqueued when:
  - photos are picked from the Gallery
  - device is connected to the Internet (WorkManager constraint)
  - app is in foreground or background (WorkManager runs jobs in background)
- Deleting a photo removes its record from the local database

## Architecture Overview

- UI (app): `PhotoViewModel` exposes `PhotoUiState`; `PhotoSelectorScreen` renders state and triggers actions.
- Domain (core):
  - Models: `Photo`, `UploadStatus`
  - Use cases: `GetAllPhotosUseCase`, `InsertPhotosUseCase`, `DeletePhotosUseCase`, `UploadPhotosUseCase`
  - Mapper: `Photo.toUploadEnqueueData()` produces `UploadEnqueueData`
- Data (data):
  - Room: `AppDb`, `PhotoDao`, `PhotoEntity`, `Converters`
  - Repository: `PhotoRepositoryImpl` (implements `PhotoRepository` from core)
  - Listener: `UploadResultListenerImpl` updates DB on worker callbacks
- Uploader (uploader):
  - `WorkManagerUploadScheduler` implements `UploadScheduler`
  - `UploadPhotoWorker` performs upload with Firebase Storage
  - Idempotency: stable key `users/<uid>/<dedupKey>.<ext>` and existence check avoid duplicates
- Contracts (domain-api): `UploadEnqueueData`, `UploadScheduler`, `UploadResultListener`

## Modules

- app: UI, navigation, Hilt setup, WorkManager initialization (Configuration.Provider + HiltWorkerFactory)
- core: Domain models, repository interface, use cases, and utility mappers
- data: Room database, DAO/entities, repository implementation, and data-side listeners
- uploader: Upload orchestration (WorkManager scheduler + Hilt worker) and DI for Firebase
- domain-api: Small public contracts shared across modules (UploadScheduler, UploadEnqueueData, UploadResultListener)

The photo upload functionality (queuing, status management, network handling) is built as a reusable module (`uploader`) and consumed via stable contracts in `domain-api`.

## Firebase Setup

- Place `google-services.json` in `app/`.
- Anonymous auth is used automatically if the user is not signed in.
- Firebase Storage is accessed via Hilt-provided `FirebaseStorage` (in uploader module).

### Firebase Storage Rules (basic)

Use restrictive rules so users can only access their own files under `users/{uid}`:

```bash
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {
    match /users/{uid}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

## Testing Strategy

- core:
  - `MimeExtTest` validates MIME-to-extension mapping
  - `UploadEnqueueMappersTest` validates mapping from `Photo` to `UploadEnqueueData`
- data:
  - `PhotoRepositoryImplTest` covers file copy and entity creation
  - `PhotoMapperTest` validates entity→model mapping
  - `UploadResultListenerImplTest` validates status transitions
- uploader:
  - `WorkManagerUploadSchedulerTest` checks enqueue behavior (Robolectric)
  - `UploadPhotoWorkerTest` exercises basic success/failure paths
- app:
  - `PhotoViewModelTest` covers UI state transformations and actions

## Build

```bash
./gradlew assembleDebug
```

## Run Unit Tests

```bash
# Run everything
./gradlew test

# Or per module
./gradlew :core:test :data:test :uploader:test :app:test
```

## Run UI Tests

Instrumentation (Compose UI) tests run on a device/emulator from the app module.

1) Start an Android emulator (or connect a device with USB debugging enabled).
2) Run:

```bash
./gradlew :app:connectedAndroidTest
```

Notes:
- Ensure an emulator is running; otherwise Gradle will report “No connected devices”.

## AI-Assisted Development Summary

This project incorporated focused assistance from AI tools to accelerate learning and delivery:

- ChatGPT: provided guidance on Room setup.
- ChatGPT: offered pointers and examples for implementing the Firebase Storage upload flow (I hadn’t used Firebase recently).
- Cursor: assisted in scaffolding and iterating on unit tests across modules.
- Cursor + ChatGPT: helped shape the modularization approach.

## Future Improvements

- Delete files from Firebase Storage when removing locally (not implemented)
- Sync down from Firebase Storage on app start (not implemented)
- Explicit retry button (current behavior auto-retries when app opens or network restores)
- Compress photos if too large before uploading to Firebase Storage (not implemented)
