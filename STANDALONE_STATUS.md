# TopCop1000 Standalone checkpoint

Branch: standalone-firetv-app

## Implemented
- Native Android / Fire TV shell, no Kodi dependency
- Landscape TV UI with 10 tombstones plus EXIT
- D-pad navigation and blood-drop focus marker
- Lightweight zombie background with idle behavior
- Persistent zombie and idle settings
- Native MP3 Center shell with Android MediaPlayer base
- Video hub with Android document picker
- Live TV / Portal hub structure for M3U/M3U8, Stalker/MAC and Xtream
- External Apps launcher
- Files and Tools centers
- Favorites center
- Reserved future tile
- Manual-only GitHub Actions workflow

## Build policy
Do not run Actions during development. Before final APK builds:
1. First complete source review.
2. Second independent source/configuration review.
3. Only then manually build final requested variants.

## Still to complete before final build
- Real playlist/portal data models and parsers
- Native playback integration for live/video streams
- MP3 local-file browsing and saved user stream sources
- Persistent favorites data
- Tools/diagnostics implementations
- Editable/reorderable tile mode and optional server configuration
- Final visual polish and TV navigation edge cases
- ABI/release packaging decision and signing
