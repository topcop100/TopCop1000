# TopCop1000 Standalone checkpoint

Branch: standalone-firetv-app

## Implemented
- Native Android / Fire TV app, no Kodi dependency
- Landscape graveyard UI with 10 movable tombstone tiles plus separate EXIT
- Fire TV D-pad navigation and blood-drop focus marker
- Lightweight zombie background with optional idle behavior
- Persistent zombie, idle, tile-order and tile-visibility settings
- Tile edit mode: reorder and hide/show with the remote
- Native MP3 Center with MediaPlayer, local audio picker, editable HTTP(S) stream slots and favorites
- Video hub with local video picker, HTTP(S) video URLs and favorites
- Live TV / Portal M3U and M3U8 parsing with browsable channel list
- Stalker/MAC profile validation, handshake, channel loading, create_link resolution and token refresh for favorites
- Xtream profile validation and remote M3U playlist import
- External Apps launcher with normal and Leanback app discovery
- Files center using Android Storage Access Framework, Downloads hint and portal-file handoff
- Tools center with diagnosis, Pydroid launcher and runtime information
- Persistent Favorites center for MP3, video, Live TV and portals
- Settings reset for tile layout and app preferences
- Reserved future tile
- Manual-only GitHub Actions workflow

## Build policy
Do not run Actions during development. Before final APK builds:
1. First complete source review.
2. Second independent source/configuration review.
3. Only then manually build the final requested 32-bit and 64-bit variants.

## Still to complete before final build
- Mixed TXT/M3U/M3U8 portal importer/splitter for M3U and Stalker/MAC data while retaining the original source
- Decide and implement whether tiles.json becomes the local/server-configurable tile source; it is currently only an unused asset
- Final visual/Fire-TV navigation review
- Final first full source/configuration review
- Final second independent source/configuration review
- Final 32-bit / 64-bit packaging setup and signing/build check

## Build status
- Automatic push builds are disabled.
- No GitHub Action should be started until the final double-check is complete.
