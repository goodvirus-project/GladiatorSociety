# Gladiator Society — Dynamic Mod Faction Discovery

This document explains how Gladiator Society detects and uses modded factions dynamically in Fleet Battles, without hardcoding any mod IDs.

## What it does
* __Auto-discovery__: Scans all loaded factions at runtime via `Global.getSector().getAllFactions()`.
* __Config filtering__: Applies `whitelist`/`blacklist`, ignores `player` and `neutral` by default.
* __Caching__: Caches results in campaign persistent data to avoid repeated scans.
* __Auto-refresh__: If the available faction set changes (mods added/removed and game restarted), the cache refreshes automatically.
* __Fair selection__: Enemy and ally each have a 50% chance to be drawn from vanilla vs modded buckets, with fallbacks if a bucket is empty.

## Key files
* Code
  * `src/data/scripts/campaign/GladiatorSociety_FactionDiscovery.java`
  * `src/data/scripts/campaign/GladiatorSociety_FactionDiscoveryConfig.java`
  * `src/data/scripts/campaign/GladiatorSociety_FleetBattleContent.java`
* Config
  * `data/config/gs_factions.json`

## Configuration: `data/config/gs_factions.json`
```json
{
  "whitelist": [],
  "blacklist": ["player", "neutral"],
  "includeHidden": true,
  "minFleetPointsForValidity": 0,
  "debug": false
}
```
* __whitelist__: Always include listed factions.
* __blacklist__: Always exclude listed factions (e.g., `player`, `neutral`).
* __includeHidden__: If true, considers hidden factions (some APIs may ignore hidden status; kept for compatibility).
* __minFleetPointsForValidity__: Reserved for future stricter validation (e.g., test-fleet spawning).
* __debug__: If true, logs discovered factions to `starsector.log`.

## How detection works (high level)
1. On first access, or if the set of available factions changed since last scan, the module:
   - Loads `gs_factions.json`.
   - Iterates all factions from `Global.getSector().getAllFactions()`.
   - Applies whitelist/blacklist and simple rules (`player`/`neutral` excluded).
   - Stores results in persistent data along with the set of seen faction IDs.
2. On subsequent calls, returns the cached set unless a change in available factions is detected (then it refreshes automatically).

## 50/50 selection policy
In `GladiatorSociety_FleetBattleContent.pickNextFactions()`:
* The discovered pool is split into two buckets:
  * __Vanilla__: core Starsector factions (Hegemony, Tri-Tachyon, Persean League, Pirates, Independents, Luddic Church, Luddic Path, Sindrian Diktat, Lion's Guard).
  * __Modded__: every other faction from the discovered pool.
* For both __enemy__ and __ally__, the code picks a bucket with 50% probability.
* If the chosen bucket is empty, it falls back to the other bucket; if both are empty, it uses a vanilla fallback (`pirates`/`independent`).

## Dynamic mod support
* No mod faction IDs are hardcoded. Any mod providing a faction (e.g., HMI) is discovered automatically.
* After enabling/disabling mods, __restart the game__ as usual. On next access, the detector recognizes the changed faction set and refreshes the cache.

## Troubleshooting
* __I only see vanilla factions__: Ensure your mod factions are not blacklisted. Enable debug to confirm discovery.
* __Enemy spawn failed__: Some factions may not produce fleets for the requested parameters. The system now:
  * Cancels the battle safely if enemy spawn is null (avoids NPEs), and
  * Retries spawning using stable fallback factions for enemy/ally.
* __Logging__: Set `"debug": true` in `gs_factions.json` to log discovered factions and assist troubleshooting.

## Extensibility
* You can tune selection further (e.g., bias ratio) by extending `pickNextFactions()` to read a future config key like `ratioModdedVsVanilla`.
* Stricter validation (e.g., test-fleet spawning) can be re-enabled later using in-game APIs when compatible.

## Safety guarantees
* Original hardcoded faction selection logic remains as a fallback; discovery augments rather than replaces it.
* Null checks and early-returns prevent battle UI errors if spawns fail.

---
If you have issues or want different selection behavior, open an issue or ping the maintainer with your `starsector.log` (with debug enabled).
