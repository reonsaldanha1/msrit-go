# MSRIT GO 🎓🚀

> **Next-Generation Academic Companion & Student Portal for Ramaiah Institute of Technology (MSRIT)**

Designed using the **Google Stitch Design System** and connecting directly to the [MSRIT Parents & Students Portal](https://parents.msrit.edu/newparents/index.php), **MSRIT GO** brings modern, intuitive mobile academic tracking, real-time attendance analytics, CIE marks calculators, and direct portal integration to your fingertips.

---

## 📸 Screenshots & Aesthetics

| First-Launch Portal Login | Home Dashboard | Smart Attendance & Simulator | CIE Marks Breakdown | Live Portal Bridge |
|---|---|---|---|---|
| Native Stitch UI asking for USN and Date of Birth matching the Contineo portal with auto-authentication. | Modern dark canvas with overall attendance radial, CIE averages, and live circular tickers. | Subject-by-subject attendance progress with one-tap Bunk & Attendance Calculators. | Comprehensive Continuous Internal Evaluation breakdown with projected SEE grade targets. | Seamless in-app WebView connecting to `parents.msrit.edu` with autofill and custom clean styling. |

---

## ✨ Features

### 1. 🔐 Official Portal Login & Real-Time DOM Data Extraction
- **Initial Portal Authentication Screen**:
    - **University Seat Number (USN)** (e.g., `1MS22CS042`)
    - **Date of Birth** (Day, Month, Year dropdowns matching the Contineo password format)
    - **Verification Type**: Choose from `Father Mobile Last 4 Digits`, `Mother Mobile Last 4 Digits`, or `ABC ID Last 4 Digits`
    - **4-Digit PIN Input**: 4 individual styled PIN boxes to enter the last 4 digits of the selected ID
    - **Remember Me** toggle for persistent auto-login
    - **Demo Mode** fallback to preview the companion without college credentials
    - **One-Tap Sample Filler** for instant testing
- **Automated Hands-Free Login & Sync Engine (`PortalBridge`)**:
  - Automatically enters credentials into `parents.msrit.edu` behind the scenes without showing the website to the student.
  - Presents a Google Stitch animated loading screen with real-time progress steps.
  - Automatically detects and passes the 2-step verification challenge.
  - Automatically scrapes and synchronizes records without requiring the user to click "Extract Data".
  - **Auto-Sync on Every App Open**: Automatically updates attendance and CIE marks each time the app is launched.
  - **High-Accuracy DOM Extraction Engine**:
    - Multi-row header parsing for Contineo tables with dynamic column identification.
    - Verified attendance calculations (`Attended` / `Held` ratio validation).
    - Null-safe CIE marks parsing (never injects fake numbers; accurately reflects tests, quizzes, and assignments).
    - Extended USN recognition including lateral and section tags (e.g., `1MS26CI143-T`).
- **Offline Cache & Persistence (`AcademicDataRepository`)**:
  - Extracted records are serialized and saved locally to device storage (`UserPreferences`) for instant, zero-latency startup.
  - One-tap "Sync Now" button in the top navigation bar to refresh records anytime from the college portal.
  - Status-bar friendly layout with native system insets (`statusBarsPadding`).

### 2. 🛡️ Smart Attendance Tracker & Bunk Simulator
- **MSRIT 85% Mandate Compliance**: Live color-coded zones:
  - 🟢 **Safe Zone (>= 85%)**: Displays exactly how many upcoming classes you can safely skip.
  - 🟡 **Warning Zone (75% - 85%)**: VTU/Autonomous condonation zone with alert pills.
  - 🔴 **Critical Shortage (< 75%)**: High-priority alert calculating how many consecutive classes you must attend to recover.
- **Interactive Bunk Simulator**:
  - Simulate: *"If I miss 2 classes, what will my attendance become?"*
  - Simulate: *"If I attend the next 4 classes, will I cross 85%?"*

### 3. 📊 CIE Marks & Grade Analytics
- **Internal Assessment Breakdown**:
  - Test 1 (CIE 1), Test 2 (CIE 2), Test 3 (CIE 3), Assignments, Quizzes, and Lab Internals normalized out of 50.
- **Projected Grade Estimation**:
  - Real-time grade forecasting (O, A+, A, B+, B, C, F).
- **SEE Target Calculator**:
  - Tells you the exact score required in your 100-mark Semester End Exam to achieve an 'O' or 'A+' grade.
- **SGPA & CGPA Trackers**:
  - Projected SGPA and cumulative CGPA telemetry.

### 4. 🌐 Seamless Portal Bridge
- Directly connects to **`https://parents.msrit.edu/newparents/index.php`**.
- **One-Tap Autofill**: Enter USN and DOB once; the app auto-populates the login fields and synchronizes sessions.
- **In-App Mobile View**: Injects modern dark responsive CSS to remove clutter and make tables touch-friendly.
- **Instant Portal Switching**:
  - [Parents & Students Portal](https://parents.msrit.edu/newparents/index.php)
  - [MSRIT e-Results & Examination Portal](https://exam.msrit.edu/)
  - [Open Electives Allocation Portal](https://msrit-oe.contineo.in:5055/index.php)

### 5. 📢 Live Circulars & Notice Board
- Direct feeds of real college notifications, including:
  - ODD Semester Academic Commencement circulars
  - Bachelor of Engineering & B.Arch regular results announcements
  - Mandatory Course Registration PDF links
  - Parents Notice on Timely Course Registration

### 6. 🏛️ Campus Hub & Services
- **IT & Wi-Fi NOC**: Direct contacts and reporting for campus high-speed Wi-Fi.
- **Proctorial Mentorship**: Faculty mentorship system and counseling channels.
- **Technical & Student Societies**: Direct access to IEEE MSRIT, Google Developer Student Club (GDSC), Team Chimera (Formula Student), and EDC.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Android Jetpack Compose & Material 3
- **Design System**: Google Stitch Dark Surface System (`#0B0E14` Obsidian, `#B82226` MSRIT Crimson, `#152F5A` Royal Navy, `#10B981` Emerald, `#38BDF8` Cyan)
- **Language**: Kotlin 2.2+ / Java 21
- **Web Bridge & Scraper**: Android WebView with `JavascriptInterface`, DOM scrapers, and CSS injection
- **Local Persistence & Cache**: SharedPreferences JSON cache & state management via Kotlin `StateFlow`
- **Build System**: Android Gradle Plugin (AGP) 9.0+ & Gradle 9.1+

---

## 📲 Download & Installation

### Direct APK Download
You can find the ready-to-install debug APK located in the repository:
```
release/msrit-go-v1.1.0.apk
```

To install on your connected Android phone via ADB:
```bash
adb install release/msrit-go-v1.1.0.apk
```

### Build from Source
```bash
git clone https://github.com/reonsaldanha1/msrit-go.git
cd msrit-go
./gradlew assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License
MIT License.
