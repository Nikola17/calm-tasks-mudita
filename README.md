# Calm Tasks

Calm Tasks is a local-first Android task app designed for Mudita Kompakt and its
E Ink display. It helps users capture tasks, choose a small number of daily
priorities, and receive calm local reminders without accounts, cloud sync, or
attention-driven mechanics.

## Target Device

- Mudita Kompakt
- MuditaOS K 1.5.0 / Android 12 AOSP
- 480 x 800 portrait E Ink display
- No Google Services dependency

## Product Principles

- Today starts with 3 priorities by default.
- The priority limit is configurable in Settings up to 99 tasks.
- All data stays on the device.
- Reminders are local Android notifications with a chosen day and time.
- No gamification, streaks, feeds, cloud sync, or engagement loops.
- UI uses black, white, and light gray only.

## Current Features

- Today view with day-by-day navigation for upcoming dates.
- Unplanned view for tasks that do not have a chosen day yet.
- Default folders plus custom folders created from Settings.
- Custom folder rename, delete, and manual ordering from Settings.
- Duplicate folder names are blocked.
- Deleting a custom folder moves its tasks back to Unplanned.
- Add and edit tasks with title, folder, planned day, and optional reminder.
- Precise reminders by date and time.
- Local notification actions: Done, Later, Open.
- Completed task history with restore and delete actions.
- Manual task deletion from the task detail screen.
- Settings for Today priority limit, app language, notifications, and folders.
- Settings are grouped into collapsible sections for easier navigation on E Ink.
- Empty Today state with monochrome illustrations optimized for E Ink.

## Languages

The app includes resources for:

- English
- French
- Polish
- Spanish
- Portuguese
- Italian
- German
- Russian

The app can follow the system language or use a language chosen in Settings.

## Build

This project expects a standard Android toolchain with JDK 17+ and Android SDK
35 installed.

```powershell
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

If Gradle wrapper files are not present yet, open the project in Android Studio
and let it sync with the Gradle files.

## Verification Checklist

Run before sharing a new APK:

- `./gradlew testDebugUnitTest`
- `./gradlew assembleDebug`
- Install on Android 12 / MuditaOS K without Google Services.
- Check at 480 x 800 portrait:
  - Today empty state and Today with tasks.
  - Settings with collapsible language, notifications, priority limit, and custom folders.
  - Add Task with folder, selected day, reminder date, and reminder time.
  - Task Detail with edit, reminder, and Done.
  - Completed tasks with restore and delete actions.
- Confirm the UI remains black, white, and light gray only.
- Confirm selected buttons render with black fill and white text.
- Confirm every visible string is translated in the 8 supported locales.

## MVP Scope

Included:

- Today, Unplanned, Folders, Task Detail, Add Task, and Settings screens.
- Completed task archive.
- Room persistence.
- Default and custom folders.
- Custom folder rename, delete, duplicate prevention, and manual ordering.
- Date navigation for Today and reminder selection.
- Local reminder scheduling with precise time selection.
- Restore and delete flows for completed tasks.
- Notification enablement from Settings.
- Language selection from Settings.

Out of scope:

- Cloud sync.
- Accounts.
- Collaboration.
- Subtasks.
- Repeating tasks.
- Productivity statistics.

## License

Calm Tasks is released under the MIT License. See [LICENSE](LICENSE).
