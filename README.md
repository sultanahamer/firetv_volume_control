# Firetv volume control

In POC(Proof of concept) state - using it in my current fire tv lite, feel free to try it out

This is a second draft, first draft was without UI

## Usage:

Install apk and then grant below permissions from adb

1. To be able to listen to key presses on remote
2. To be able to show the volume controls UI

```shell
adb shell settings put secure enabled_accessibility_services com.example.volumecontrolservice/com.example.volumecontrolservice.AccessibilityService
adb shell appops set com.example.volumecontrolservice  SYSTEM_ALERT_WINDOW allow
```
