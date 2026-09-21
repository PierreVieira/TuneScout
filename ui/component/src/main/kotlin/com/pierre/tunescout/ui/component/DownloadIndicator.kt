package com.pierre.tunescout.ui.component

/** What a song row shows of the song's download, beside its artist. */
enum class DownloadIndicator {
    None,
    Downloading,
    Downloaded,
    ;

    companion object {
        /**
         * @return [None] unless the song [isRequested], and then [Downloaded] once it [isComplete].
         */
        fun of(
            isRequested: Boolean,
            isComplete: Boolean,
        ): DownloadIndicator = when {
            !isRequested -> None
            isComplete -> Downloaded
            else -> Downloading
        }
    }
}
