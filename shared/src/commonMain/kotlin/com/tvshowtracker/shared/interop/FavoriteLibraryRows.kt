package com.tvshowtracker.shared.interop

import com.tvshowtracker.shared.data.repository.FavoriteListItem

/** Reference snapshot for frameworks that require a class wrapper (Swift `FlowWrapper<T>` vs `Array`). */
data class FavoriteLibraryRows(val items: List<FavoriteListItem>)
