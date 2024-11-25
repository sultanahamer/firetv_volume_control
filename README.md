# Firetv volume control

Using it in my current fire tv lite since bought it in 2023. Feel free to try it out

## Usage:

Install apk and then grant below permissions from adb

1. To be able to listen to key presses on remote
2. To be able to show the volume controls UI

```shell
adb shell settings put secure enabled_accessibility_services com.example.volumecontrolservice/com.example.volumecontrolservice.AccessibilityService
adb shell appops set com.example.volumecontrolservice  SYSTEM_ALERT_WINDOW allow
```
