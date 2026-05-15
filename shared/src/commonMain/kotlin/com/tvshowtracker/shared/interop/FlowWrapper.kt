package com.tvshowtracker.shared.interop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FlowWrapper<T : Any>(
    private val upstream: Flow<T>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun subscribe(onEach: (T) -> Unit) {
        upstream.onEach(onEach).launchIn(scope)
    }

    fun cancel() {
        scope.cancel()
    }
}
