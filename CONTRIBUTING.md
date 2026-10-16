# Contributing to Nope Remote

Thank you for your interest in contributing to Nope Remote. This document outlines how to set up your development environment, run checks, and submit contributions.

---

## Development Environment Setup

We use [mise](https://mise.jdx.dev/) to manage the development toolchain (OpenJDK 17) and provide convenient build shortcuts.

### 1. Install mise
If you do not have mise installed:
```bash
curl https://mise.run | sh
```

### 2. Install Project Dependencies
Run from the root of the repository:
```bash
mise install
```
This automatically provisions the required OpenJDK 17 distribution and configures your environment.

---

## Common Developer Tasks

You can run tasks using mise shortcuts or standard Gradle commands:

| Task | mise Command | Gradle Equivalent |
| :--- | :--- | :--- |
| **Build Debug APK** | `mise run build` | `./gradlew assembleDebug` |
| **Build Release APK** | `mise run release` | `./gradlew assembleRelease` |
| **Run Unit Tests** | `mise run test` | `./gradlew testDebugUnitTest` |
| **Check Code Style** | `mise run lint` | `./gradlew spotlessCheck` |
| **Auto-Format Code** | `mise run format` | `./gradlew spotlessApply` |
| **Install to Device** | `mise run install-device` | `./gradlew installDebug` |

---

## Code Style and Formatting

We use Spotless with ktlint to enforce consistent Kotlin code style:

- Always run `mise run format` before committing.
- Our [.editorconfig](.editorconfig) accommodates Jetpack Compose `@Composable` functions (allowing PascalCase naming).
- All PRs are automatically validated with `mise run lint`.

---

## Submitting a Pull Request

1. **Create a Feature Branch:**
   Branch from `main` using descriptive prefixes:
   - `feat/<feature-name>` for new features
   - `fix/<bug-name>` for bug fixes
   - `docs/<topic>` for documentation changes

2. **Commit Your Changes:**
   Write clear, concise commit messages adhering to standard conventions (e.g., `feat: add custom remote creation`).

3. **Verify Locally:**
   Ensure tests pass and formatting is clean before opening your PR:
   ```bash
   mise run format
   mise run test
   mise run build
   ```

4. **Pull Request Workflows and Approval:**
   - When you open a PR, our automated verification workflows will run Kotlin lint checks, execute unit tests, and build a testable debug APK.
   - For repository safety, external PR workflows require maintainer approval before executing.

---

## Reporting Bugs and Requesting Features

- Please search existing [Issues](https://github.com/monuk7735/nope-remote/issues) to avoid duplicates.
- Provide detailed steps to reproduce, including your Android version, device model, and target device (TV, AC, etc.).
