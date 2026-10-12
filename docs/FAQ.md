# Frequently asked questions

## Does this use the overlapping screens icon on iPhone?

Yes, the intended flow is Control Center → **Screen Mirroring**. Keep the receiver open and ready on the TV, then select its displayed name. Physical Apple–TV connectivity has not yet been tested.

## Which TVs are supported?

ARM Android TV / Google TV on Android 7.0 or newer. Samsung Tizen and LG webOS cannot install this Android APK. Extract the ZIP on your computer and transfer the APK to the TV.

## The TV is missing from the mirroring list

Check the ready state and use the same local network. Guest networks, VPNs and device-isolation settings may block discovery. Reopen the mirroring list and use Connection help / Try again. After a name collision, select the name actually displayed on the TV.

## Why is a PIN required?

PIN is enabled by default. Enter the TV’s four-digit code; there is no fixed default code. If random generation fails, startup fails. Disabling PIN in Settings allows code-free local-network connections; this change applies to the next session.

## Why is quality lower than 4K or 60 FPS?

These are upper targets. Decoder capacity may require a lower profile, and the sender or network may deliver less. Save the quality setting, disconnect and reconnect. 4K is experimental; actual 4K/30 and 1080p/60 output has not been measured on a physical TV.

## Why does HOME disconnect the receiver?

The receiver runs while the app is visible. Leaving stops it; returning starts it again. No background receiver or boot auto-start is used.

## Can I update an older installation?

Official releases share the signing certificate and package ID; install the new APK without uninstalling. The default name for new installs is Android TV, but an existing saved name is preserved. Actual on-device update installation remains untested. An APK signed with your own key cannot update the official install in place; uninstalling may erase settings.

## Does every video app work?

No universal compatibility is claimed. DRM/protected content, direct video/HLS, audio-only streaming and mirroring are distinct paths. Complete AirPlay 2 support is not claimed.

## What should a bug report include?

TV model/Android version, application version, sender model/OS, selected quality, whether PIN is enabled, network type and reproduction steps. Do not include actual PINs, pairing keys, passwords, private media or sensitive URLs. See [CONTRIBUTING.md](../CONTRIBUTING.md).
