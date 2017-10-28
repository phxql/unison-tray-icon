# unison-tray-icon

A tray icon for the unison file synchronizer.

## Usage

1. Start the application (this will create the config file `~/.unison-tray.config`)
2. Edit the configuration file and restart the application

## Configuration reference

```json
{
  "syncInterval" : 300, // Interval in seconds between syncs
  "profile" : "moe"     // Name of the unison profile to use 
}
```

## Building

`gradlew clean build`

## Running

### With Gradle

`gradlew clean run`

### Standalone

* Build with `gradlew clean build`
* Check the `build/distributions` folder

## License

[LGPLv3](https://tldrlegal.com/license/gnu-lesser-general-public-license-v3-(lgpl-3))