# Changelog

All notable changes to DAFI Desktop are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- XDG Base Directory layout for data and config (`~/.local/share/dafi`,
  `~/.config/dafi`) with one-time migration from the legacy `~/.dafi/data`.
- Canonical application ID `io.github.davichus1303.DafiDesktop` for Flatpak
  packaging (desktop file, icon and manifest).
- Flatpak manifest (`packaging/flatpak/io.github.davichus1303.DafiDesktop.yml`)
  with least-privilege `finish-args`: no network, keyring and desktop portal
  only, sandbox-scoped filesystem access.
- AppStream metainfo and screenshots for the Flathub listing.

### Changed

- Data and config are now resolved via XDG environment variables with legacy
  `~/.dafi` fallback; the migration ignores files already present in the XDG
  target.
- Encryption key is stored in the OS keyring (Secret Service on Linux)
  instead of a plain file.

## [1.0.1] - 2026-08-27

### Fixed

- Installer shortcuts point directly at the jpackage `.exe`.
- NSIS installer: user-level install, simplified Java detection and a
  yes/no prompt before downloading Java.
- Java 21 runtime used by the packaged application.
- Removed `--add-modules` from jpackage (broke launch via classpath).

## [1.0.0] - 2026-08-26

### Added

- Client management with encrypted local storage (AES-256-GCM).
- Secure authentication with Argon2id.
- Bulk client import from Excel with validation and error reporting.
- Contract type and payment method catalogs.
- Real-time search with debounce.
- JavaFX 21 graphical interface, Spanish i18n.
- Windows packaging pipeline (CI + installer).

[1.0.1]: https://github.com/davichus1303/DAFI_desktop/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/davichus1303/DAFI_desktop/releases/tag/v1.0.0