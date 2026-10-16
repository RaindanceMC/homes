# RaindanceMC/homes

- **`homes-module`** — Rain's Flexible Modules — Homes: Somewhere of your own to come back to: name it, set it, go to it, and pick from a menu of them. The places are RainsCore's — a home is a POI of kind "home" — and so is the going, which is the same warm-up and safe arrival the warps and the teleport requests use. What is here is what a home means: how many you may have, who else may use one, and the screens.
- **`homes-standalone`** — Rain's Homes: Rain's Homes, as a plugin of its own. Nothing here but a build file and a descriptor: the homes module and the standard wrapper are shaded together, and that is the whole plugin. The name and the data folder are unchanged from the standalone plugin it replaces.

Part of [RaindanceMC](https://github.com/RaindanceMC); built on [RainsCore](https://github.com/RaindanceMC/RainsCore) and the shared parent in [framework](https://github.com/RaindanceMC/framework). Split out of `rains-flexible-modules` on 16.10.2026, history kept.

## Build

```
mvn install
```

Needs Java 25 and read access to `packages.tstieh.de` (server ids `packages-releases` / `packages-snapshots` in `~/.m2/settings.xml`).

## Plugins

- `homes-standalone/target/RainsHomes-2.8.3.jar` — also published as `de.raindancer.modules:homes-standalone:2.8.3`

## Versions

A module's version is a property in this repository's root `pom.xml`, a plugin's is `plugin.version` in its own pom. CI publishes every version that is not in the registry yet on the next push to master.
