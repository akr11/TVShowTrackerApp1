package com.tvshowtracker.shared.platform

/** Wall-clock milliseconds since UNIX epoch (for DB timestamps); platform-backed. */
expect fun currentEpochMillis(): Long
