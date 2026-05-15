@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.tvshowtracker.shared.platform

import platform.CoreFoundation.CFAbsoluteTimeGetCurrent

/**
 * CFAbsoluteTime is seconds since 2001-01-01; offset is seconds from UNIX epoch to that reference.
 * See Apple documentation for `CFAbsoluteTimeGetCurrent`.
 */
private const val SecondsFromUnixEpochToAppleReferenceDate = 978_307_200.0

actual fun currentEpochMillis(): Long =
    ((CFAbsoluteTimeGetCurrent() + SecondsFromUnixEpochToAppleReferenceDate) * 1000.0).toLong()
