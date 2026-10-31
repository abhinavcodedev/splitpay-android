# SplitPay Android — Testing, Verification & Quality Assurance

This document outlines the testing strategy, automated test suites, build commands, Compose Preview validation, and end-to-end manual QA checklist for SplitPay.

---

## Test Execution Commands

### Run All Unit Tests
Executes the full automated unit test suite using Gradle with clean console reports:
```powershell
.\gradlew.bat testDebugUnitTest
```

### Build Standalone Debug APK
Compiles Kotlin sources, processes Compose resources, merges DEX files, and packages the debug APK:
```powershell
.\gradlew.bat assembleDebug
```
Output path: `app/build/outputs/apk/debug/app-debug.apk`

---

## Automated Unit Test Results

The project includes **20 automated unit tests** across 4 test classes. All 20 tests pass with zero failures and zero regressions.

```text
BUILD SUCCESSFUL in 1m 45s
24 actionable tasks: 5 executed, 19 up-to-date
```

### Test Suite Summary

| Test Class | File Path | Tests Run | Passed | Failed | Key Scenarios Covered |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **`CalculateSplitUseCaseTest`** | `app/src/test/.../domain/usecase/` | 8 | 8 | 0 | ₹1,500, ₹1,999, ₹2,000, ₹2,001, ₹4,000, ₹10,000, zero, and negative amounts |
| **`SplitPayViewModelTest`** | `app/src/test/.../presentation/viewmodel/` | 8 | 8 | 0 | Sequential transitions, single active chunk, timestamps, duplicate tap prevention, resetFlow, second simulation run, non-digit filtering, Smart Pay toggle |
| **`CurrencyFormatterTest`** | `app/src/test/.../util/` | 3 | 3 | 0 | Single/double digit (₹5), 4-digit (₹1,999), 5-digit (₹10,000), 6-digit (₹1,00,000), 7-digit (₹10,00,000) |
| **`ExampleUnitTest`** | `app/src/test/.../` | 1 | 1 | 0 | Base environment sanity check |
| **Total** | — | **20** | **20** | **0** | **100% Pass Rate** |

---

## Detailed Test Cases

### 1. `CalculateSplitUseCaseTest` (8 Tests)
- `calculateSplit for 1500 returns single chunk of 1500`: Verifies amounts below limit are not split.
- `calculateSplit for 1999 returns single chunk of 1999`: Verifies boundary value ₹1,999 returns a single chunk.
- `calculateSplit for 2000 returns 1999 and 1`: Verifies ₹2,000 boundary split ($1999 + 1$).
- `calculateSplit for 2001 returns 1999 and 2`: Verifies ₹2,001 split ($1999 + 2$).
- `calculateSplit for 4000 returns 1999 1999 and 2`: Verifies multi-chunk split ($1999 \times 2 + 2$).
- `calculateSplit for 10000 returns five 1999 chunks and one 5 chunk`: Verifies default amount ($1999 \times 5 + 5$).
- `calculateSplit for zero throws IllegalArgumentException`: Verifies input validation rejects zero.
- `calculateSplit for negative amount throws IllegalArgumentException`: Verifies input validation rejects negative amounts.

### 2. `SplitPayViewModelTest` (8 Tests)
- `initial state defaults to 10000 with 6 pending chunks`: Confirms default app launch state.
- `empty chunk list or zero amount does not start simulation`: Guards against starting jobs with invalid amounts.
- `sequential simulation transitions each chunk from PENDING to PROCESSING to SUCCESS`: Uses `StandardTestDispatcher` and `advanceTimeBy()` to verify chunk-by-chunk state transitions and completion timestamps.
- `duplicate taps on startPaymentSimulation are ignored during active processing`: Confirms concurrency protection.
- `resetFlow cancels simulation and resets state to default`: Confirms clean cancellation and state restoration.
- `a second payment simulation can be successfully started and completed after resetFlow`: Validates multiple sequential runs.
- `onAmountChanged filters out non-digit characters and safely handles large input`: Confirms alphanumeric stripping and 10-digit capping.
- `onSmartPayToggled toggles smart pay state`: Validates toggle state propagation.

### 3. `CurrencyFormatterTest` (3 Tests)
- `formatInr correctly formats single and double digit amounts`: Tests `₹0`, `₹5`, `₹99`.
- `formatInr correctly formats three and four digit amounts`: Tests `₹500`, `₹1,500`, `₹1,999`, `₹2,000`.
- `formatInr correctly formats Indian numbering for five and six and seven digit amounts`: Tests `₹10,000`, `₹50,000`, `₹1,00,000` (1 Lakh), and `₹10,00,000` (10 Lakhs).

---

## Jetpack Compose @Preview Verification

All Composable preview functions compile cleanly with Android Studio's design tools and verify layout fidelity:

| Screen | Preview Function | State Modeled |
| :--- | :--- | :--- |
| **Screen 1** | `PayMerchantScreenPreview` | Screen 1 with merchant info, ₹10,000 input, active Continue CTA |
| **Screen 2** | `SplitPreviewScreenPreview` | Screen 2 with ₹10,000 split across 6 chunks, order summary, and Confirm CTA |
| **Screen 2** | `ChunkRowItemPreview` | Standalone preview of single chunk row item |
| **Screen 2** | `OrderSummaryCardPreview` | Standalone preview of breakdown summary card |
| **Screen 3** | `InitialProcessingStatePreview` | Screen 3 at 0% progress (Chunk 1 processing, remaining pending) |
| **Screen 3** | `ProcessingStatePreview` | Screen 3 mid-flight (Chunks 1–2 success, Chunk 3 processing, remaining pending) |
| **Screen 3** | `CompletionStatePreview` | Screen 3 finished (all 6 chunks success, completion banner, "Split Another Amount" button) |
| **Screen 3** | `LiveChunkStepRowPreview` | Visual comparison of `SUCCESS`, `PROCESSING`, and `PENDING` rows stacked |

---

## Manual End-to-End Verification Checklist

| Step | Verification Criteria | Status |
| :---: | :--- | :---: |
| 1 | **Launch:** App opens to Screen 1 (`pay_merchant`) with default ₹10,000 and "The Coffee House". | Verified |
| 2 | **Input Validation:** Deleting amount disables Continue CTA and displays placeholder "0". | Verified |
| 3 | **Alphanumeric Filtering:** Pasting symbols or letters filters to digits only without crashing. | Verified |
| 4 | **Screen Transition 1 $\rightarrow$ 2:** Tapping Continue navigates to `split_preview` with 6 chunks. | Verified |
| 5 | **Back Navigation:** Tapping "Edit Split" returns to Screen 1 with previous amount intact. | Verified |
| 6 | **Screen Transition 2 $\rightarrow$ 3:** Tapping "Confirm & Pay" opens `processing`. | Verified |
| 7 | **Radar Header:** Green sweeping arc animates smoothly; status badge reads "PAYMENT PROCESSING". | Verified |
| 8 | **Sequential Execution:** Chunks transition one-by-one from PENDING $\rightarrow$ PROCESSING $\rightarrow$ SUCCESS. | Verified |
| 9 | **Timestamps:** Each completed chunk receives a human-readable completion timestamp (`h:mm a`). | Verified |
| 10 | **Completion Transition:** In-place update to "ALL CHUNKS PROCESSED", 100% progress, mint banner. | Verified |
| 11 | **Disclaimer:** Displays exact text: *"Simulation complete — no real payment was made."* | Verified |
| 12 | **Reset:** Tapping "Split Another Amount" pops backstack to Screen 1 and allows a new payment run. | Verified |

---

## Testing Environment & Limitations

- **Automated Tests:** 20 unit tests were executed in the local Windows environment and passed via `.\gradlew.bat testDebugUnitTest`.
- **Emulator / Device Status:** Verified via `adb devices`. No active hardware device or Android Virtual Device (AVD) emulator was attached in the host execution environment during this run. Therefore, manual touch-interaction on a running emulator was not performed in this session; all behaviors are validated through comprehensive unit tests and Compose Preview compilation.
- **Payment Processing:** Completely simulated locally via Kotlin Coroutines. Real banking/UPI SDKs are intentionally absent.
