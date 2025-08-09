# Gladiator Society - Build Subproject

This folder contains an isolated Gradle project used only for building the mod jar, keeping the mod root clean for easy releases.

## Setup
1. Copy `gradle.properties.example` to `gradle.properties`.
2. Set `STARSECTOR_DIR` to your Starsector install path, e.g.
   - `C:/Program Files (x86)/Steam/steamapps/common/Starsector`

## Build
- Using system Gradle:
  - From the repo root: `gradle -p gradle-project clean jar`
- Using Gradle Wrapper (recommended):
  1. Generate wrapper once: `gradle -p gradle-project wrapper`
  2. Build with wrapper:
     - Windows: `gradle-project\\gradlew.bat -p gradle-project clean jar`
     - Unix: `./gradle-project/gradlew -p gradle-project clean jar`

The output jar is written to `../jars/GladiatorSocietyJar.jar` to match `mod_info.json`.

## Notes
- Sources are compiled from `../src` and `../com`.
- Dependencies to Starsector core libs are `compileOnly` (provided by the game at runtime).
- The mod root remains your release folder; simply copy the mod root without the `gradle-project/` directory when packaging.
