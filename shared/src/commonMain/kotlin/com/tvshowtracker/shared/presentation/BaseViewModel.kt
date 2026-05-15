package com.tvshowtracker.shared.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseViewModel {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    open fun clear() {
        scope.cancel()
    }
}
