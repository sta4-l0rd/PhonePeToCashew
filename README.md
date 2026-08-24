# PhonePeToCashew

An Android companion utility that bridges PhonePe payment receipts to the [Cashew](https://github.com/jameskokoska/Cashew) expense tracker.

Share a PhonePe payment screenshot directly from the Android share sheet. The app extracts receipt fields on-device and opens Cashew with pre-filled transaction data.

---

## Features

- **Share Target Integration**: Appears in the standard Android share sheet for images.
- **On-Device OCR**: Reads text locally using Google ML Kit Text Recognition. No receipt data leaves your device.
- **Field Extraction**:
  - Amount (handles rupee glyphs and OCR symbol variants)
  - Transaction Direction (Expense vs Income)
  - Payee / Sender Name and UPI ID
  - Date and Time
  - PhonePe Transaction ID
  - Bank UTR Number
  - Masked Debit / Credit Account
  - Custom transaction messages
- **Structured Cashew Notes**: Assembles all metadata into formatted notes for Cashew.
- **Deep Link Routing**: Uses Cashew app links to open the Add Transaction page with fields pre-populated.
- **Duplicate Prevention**: Tracks processed transaction IDs to avoid double entry.
- **Compact Build**: Release APK is under 2 MB with R8 bytecode minification and resource shrinking.

---

## Requirements

- Android 7.0 (API level 24) or higher
- [Cashew Budget App](https://github.com/jameskokoska/Cashew) installed on the same device

---

## How to Use

1. Open **PhonePe** and complete a transaction or open an existing receipt.
2. Tap the **Share** button or take a screenshot of the receipt.
3. Select **PhonePe to Cashew** from the Android share sheet.
4. Review the extracted amount, direction, and metadata in the confirmation screen.
5. Tap **Add to Cashew**. Cashew opens immediately with the transaction ready to save.

---

## Architecture

- **UI**: Jetpack Compose with Material 3 (Dark Theme tailored to match PhonePe and Cashew).
- **OCR Engine**: `play-services-mlkit-text-recognition` with layout-based regional parsing and full-text fallback.
- **Integration**: Direct Android `ACTION_VIEW` intent dispatching to `https://cashewapp.web.app/addTransactionRoute`.

---

## Build and Installation

### Prerequisites

- JDK 17
- Android SDK 34

### Build Debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Build Optimized Release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Run Unit Tests

```bash
./gradlew test
```

### Install via ADB

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
