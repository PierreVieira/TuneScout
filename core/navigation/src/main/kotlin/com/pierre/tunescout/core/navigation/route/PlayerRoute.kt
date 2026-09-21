package com.pierre.tunescout.core.navigation.route

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.deeplink.DeepLinkKey
import kotlinx.serialization.Serializable

@Serializable
data class PlayerRoute(
    val songId: Long,
) : DeepLinkKey,
    DetailPaneRoute {
    override val parent: NavKey
        get() = HomeRoute
}
