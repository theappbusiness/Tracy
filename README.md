# Tracy

This repo is a bare bones example of how to set up Bluetooth LE on iOS and Android
in a way that could be used as a Contact Tracing app.

This code was used to validate and test claims in a post by Kane Cheshire for
Kin and Carta about what limitations and restrictions the iOS Core Bluetooth framework
imposes on developers, and why it can be a problem for Contact Tracing apps.

---

In this single repository exists both an iOS and Android project called Tracy,
in which the iOS and Android apps both advertise themselves to nearby devices to be
discovered as well as scanning for nearby devices to discover and connect to them.

Once they connect, each device simply reads the value of a characteristic from the other
device, which in this case will just be the name of the device.

---

## Where to start

Each project is set up as similarly as each platform will allow, but since each platform handles background execution differently, they aren't exactly the same.

Additionally, the iOS Core Bluetooth framework handles a lot for developers,
including queuing operations which is still required on Android, but developers are responsible for queuing operations themselves.

Each project has both a `Central` class and a `Peripheral` class, which are good places to start if you want to have a quick look.

---

The iOS app requires a device running iOS 13.4 or newer, and the Android app requires a device running API 26 or higher.

---

Neither project uses any 3rd party dependencies for their Bluetooth implementation. While there are plenty out there, this repo is designed to let you see how the underlying code works.


## Android

Since Android apps require a foreground service to run while not the active app, Tracy for Android has a `ForegroundBluetoothService` which is started by the `MainActivity` after requesting location permissions (required for Bluetooth scanning on Android).

When the `ForegroundBluetoothService` starts, it then is responsible for starting the Android `Peripheral` advertising to nearby `Central`s, and then also starts the android `Central` to scan for nearby `Peripheral`s.

### Central

Since Bluetooth LE on Android requires a bit more work than on iOS (like queuing operations ourselves), the `Central` class itself is quite simple and the code for scanning and connecting is split into two more classes called `Scanner` and `Connector`.

The `Scanner` uses another simple class called a `GattOperationQueue` which simply keeps an array of pending operations which are executed when the most recent one is marked as complete.

Additionally, `GattOperationQueue` also ensures all operations are executed on the main thread, since `BluetoothGatt` callbacks are called from a `Binder` thread, which on some devices will fail if used for executing `BluetoothGatt` operations.

### Peripheral

Similarly to the `Central` class, the `Peripheral` class is split into two classes called `Advertiser` and `Server`.

## iOS

Since iOS apps don't have the concept of foreground services to keep running in the background, instead Tracy for iOS enables the two background modes to continue to operate as a `Central` and `Peripheral` while not the active app.

When the app is suspended in the background by the system, the system takes over scanning and advertising on the app's behalf, even if the app is terminated by the system to free up memory for another app.

The system resumes the app to handle Bluetooth events like discoveries and connections or read requests. If the app was terminated, the app is relaunched in the background, and since we use the same restoration identifiers for the Central and Peripheral, the centrals are not recreated from scratch so there's no need to restart advertising etc.

Because there's no service to start the Bluetooth code like on Android, the iOS app just creates the `Central` and `Peripheral` in the `AppDelegate`.

## Testing

You may wish to test how the different platforms interact when they're in different states.

Notably, without employing the clever filtering that the [NHSx app uses](https://github.com/nhsx/COVID-19-app-Android-BETA/blob/43a167f8dba422fd9001b64f9c4fd82275abb1c8/app/src/main/java/uk/nhs/nhsx/sonar/android/app/ble/Scanner.kt#L67), when both iOS and Android apps are in the foreground, they will be able to discover each other without issues.

However if you put the iOS app into the background, you'll notice that the Android app can no longer discover and connect to it. This is the main issue with iOS apps running as Contact Tracing apps in the background.

## Caveats

Although this code does generally work, the Android Bluetooth code is more reliable on some devices than others. Specifically, this has been tested on a Google Pixel 3a on API 29, and a Nexus 5X on API 27, and the Pixel 3a was more consistent and reliable.

PRs are welcome if you know how and want to improve this further!
