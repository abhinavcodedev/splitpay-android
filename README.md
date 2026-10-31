# SplitPay — Smart Payment Splitter

A smart payment-splitting prototype built with **Kotlin and Jetpack Compose** that divides a larger payment amount into smaller chunks and simulates sequential payment processing with individual transaction tracking.

SplitPay demonstrates how a payment-splitting experience can be designed using modern Android development practices, clean UI architecture, reactive state management, and Kotlin Coroutines.

> **Disclaimer:** SplitPay is an educational UI and logic prototype. It does not perform real UPI transactions, transfer money, or provide a workaround for merchant charges, MDR, or payment regulations. All payment statuses are simulated locally.

---

## ✨ Features

- **Amount Entry:** Enter and edit the payment amount.
- **Automatic Splitting:** Divide an amount into chunks with a configurable maximum limit of ₹1,999.
- **Split Preview:** Review individual chunks, total amount, and payment count before proceeding.
- **Sequential Simulation:** Simulate payment processing one chunk at a time.
- **Individual Transaction Tracking:** Track every chunk independently through its own status.
- **Live Progress:** Visualize completed, currently processing, and pending chunks.
- **Completion State:** Display the final simulated payment summary.
- **Reset Flow:** Start another payment simulation without restarting the app.

## 📱 App Screens

SplitPay contains exactly three screens. Payment completion is an in-place state of the Processing screen, not a separate navigation destination.

| Screen | Description |
|---|---|
| Pay Merchant | Enter the amount and configure Smart Pay. |
| Split Preview | Review the calculated payment chunks and total. |
| Processing | Observe individual transaction statuses and final completion. |

<!-- Add screenshots here after emulator testing -->

## 🧮 How Splitting Works

The prototype uses a maximum chunk limit of **₹1,999**.

- If the amount is less than or equal to ₹1,999, it remains a single chunk.
- If the amount exceeds ₹1,999, it is divided into chunks of up to ₹1,999.
- Any remaining amount becomes the final chunk.

### Examples

| Original Amount | Generated Chunks | Payments |
|---:|---|---:|
| ₹1,500 | ₹1,500 | 1 |
| ₹1,999 | ₹1,999 | 1 |
| ₹2,000 | ₹1,999 + ₹1 | 2 |
| ₹2,001 | ₹1,999 + ₹2 | 2 |
| ₹4,000 | ₹1,999 + ₹1,999 + ₹2 | 3 |
| ₹10,000 | ₹1,999 × 5 + ₹5 | 6 |

All monetary calculations use Kotlin `Long` to represent whole-rupee amounts.

## 🔄 Transaction Simulation

Each payment chunk maintains an independent status:

`PENDING → PROCESSING → SUCCESS`

The simulation uses Kotlin Coroutines and updates the UI reactively through StateFlow.

For example, a six-chunk simulation processes each chunk sequentially:

1. Mark the current chunk as PROCESSING.
2. Simulate a short processing delay.
3. Mark the chunk as SUCCESS and assign a completion timestamp.
4. Move to the next chunk.
5. Display the completion state when all chunks succeed.

The simulation is entirely local. No real payment is initiated.

## 🏗️ Tech Stack

| Technology | Usage |
|---|---|
| Kotlin | Primary programming language |
| Jetpack Compose | Declarative UI |
| Material 3 | UI components and theming |
| MVVM | Presentation architecture |
| StateFlow | Reactive UI state management |
| Kotlin Coroutines | Sequential payment simulation |
| Hilt | Dependency injection |
| Navigation Compose | Three-screen navigation |
| JUnit | Unit testing |

## 🧱 Architecture

The project follows a lightweight MVVM architecture with separation of business logic and UI responsibilities.

```text
com.splitpay.app/
│
├── data/
│   └── model/
│       ├── MerchantInfo.kt
│       ├── PaymentChunk.kt
│       └── ChunkStatus.kt
│
├── domain/
│   └── usecase/
│       └── CalculateSplitUseCase.kt
│
├── presentation/
│   ├── navigation/
│   │   ├── Screen.kt
│   │   └── NavGraph.kt
│   │
│   ├── viewmodel/
│   │   ├── SplitPayUiState.kt
│   │   └── SplitPayViewModel.kt
│   │
│   └── screens/
│       ├── PayMerchantScreen.kt
│       ├── SplitPreviewScreen.kt
│       └── ProcessingScreen.kt
│
├── ui/
│   └── theme/
│
└── SplitPayApplication.kt
```

Additional documentation is available in the [`doc/`](doc/README.md) directory.

## 🚀 Getting Started

### Prerequisites

- Android Studio
- Android SDK compatible with the project's compileSdk configuration
- A compatible JDK and Gradle environment
- Android emulator or physical Android device

### Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/splitpay-android.git

cd splitpay-android
```

Replace `YOUR_USERNAME` with your GitHub username.

### Open the Project

1. Open Android Studio.
2. Select **Open**.
3. Choose the cloned `splitpay-android` directory.
4. Allow Gradle synchronization to complete.
5. Run the app on an emulator or Android device.

### Build the Project

Windows:

```bash
.\gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

### Run Unit Tests

Windows:

```bash
.\gradlew.bat testDebugUnitTest
```

macOS/Linux:

```bash
./gradlew testDebugUnitTest
```

## 🧪 Testing

The project includes unit tests for:

- Payment split calculation.
- Amount validation.
- Indian currency formatting.
- ViewModel state transitions.
- Sequential chunk simulation.
- Duplicate simulation prevention.
- Reset and repeated simulation flows.

Run the test suite using the commands above.

See [`doc/TESTING.md`](doc/TESTING.md) for the detailed testing documentation and verification status.

## 📚 Documentation

The `doc/` directory contains detailed technical documentation:

| Document | Description |
|---|---|
| [README](doc/README.md) | Documentation index and project overview |
| [App Flow](doc/APP_FLOW.md) | Complete application flow and navigation |
| [Architecture](doc/ARCHITECTURE.md) | Architecture, packages, and responsibilities |
| [Business Logic](doc/BUSINESS_LOGIC.md) | Splitting algorithm and amount validation |
| [State & Simulation](doc/STATE_AND_SIMULATION.md) | State management and transaction simulation |
| [Testing](doc/TESTING.md) | Unit tests and manual verification |

## 🎨 Design

The UI was designed using **Google Stitch** with the Kinetic Ledger design system and implemented in Jetpack Compose.

The implementation includes reusable UI components and Compose `@Preview` functions for visual inspection.

## 🛣️ Project Status

- [x] Architecture and foundation
- [x] Pay Merchant screen
- [x] Split Preview screen
- [x] Processing screen
- [x] Independent chunk status tracking
- [x] Completion state
- [x] Unit tests
- [x] Technical documentation
- [x] Debug APK build
- [ ] Emulator end-to-end verification
- [ ] Final UI polish based on device testing

## 🔮 Future Improvements

Potential improvements for future iterations:

- More comprehensive UI and instrumentation tests.
- Accessibility improvements.
- Additional UI refinements based on real-device testing.
- Better handling of lifecycle interruptions.
- Configurable split limits and simulation timing.

These are potential enhancements, not currently implemented features.

## ⚠️ Disclaimer

SplitPay is an experimental Android development project created for learning, demonstration, and portfolio purposes.

It does not connect to UPI, banks, payment gateways, or merchant systems. All transactions and payment statuses are simulated locally.

The splitting logic must not be interpreted as a method to avoid MDR, merchant fees, taxes, or applicable payment regulations.

## 📄 License

No license has been added yet. Unless a license is added to this repository, all rights are reserved by the copyright holder.
