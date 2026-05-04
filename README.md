# BuildPilot

BuildPilot is a small mobile-first productivity app for solo game developers.

It helps track:

- Project goals
- Today’s tasks
- Bugs
- Build notes
- APK export checklist

The app is built with Godot 4 and is designed to export to Android APK.

## Version 1 Scope

- View the current project goal
- Add tasks
- Mark tasks complete
- Delete tasks
- Add bugs
- Mark bugs fixed
- Save data locally as JSON
- Show an APK build checklist

## Why this app exists

Big game ideas get messy fast. BuildPilot keeps the next playable step visible so a solo developer can stay focused.

## Run Locally

1. Install Godot 4.x.
2. Clone or download this repository.
3. Open the folder in Godot.
4. Run `scenes/Main.tscn`.

## Android APK Export

1. Open the project in Godot.
2. Install Android export templates.
3. Set Android build tools in Editor Settings.
4. Open Project > Export.
5. Add an Android preset.
6. Set package name to `com.gravestonestudios.buildpilot`.
7. Export an APK.

A GitHub Actions workflow is included at `.github/workflows/android.yml`, but Android signing/export settings may still need to be finalized in Godot before it can produce a release APK automatically.
