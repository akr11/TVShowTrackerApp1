package com.tvshowtracker.shared

import com.tvshowtracker.shared.di.initKoin
import com.tvshowtracker.shared.di.iosInfrastructureModule

fun bootstrapSharedForIos() {
    initKoin(listOf(iosInfrastructureModule()))
}
