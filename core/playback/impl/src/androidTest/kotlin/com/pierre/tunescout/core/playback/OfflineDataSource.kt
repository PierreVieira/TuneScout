package com.pierre.tunescout.core.playback

import android.net.Uri
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.io.IOException

/** A network with no connection: every read fails the way the real one does in airplane mode. */
internal class OfflineDataSource : DataSource {
    override fun addTransferListener(transferListener: TransferListener) {
        Unit
    }

    override fun open(dataSpec: DataSpec): Long = throw IOException("offline")

    override fun read(
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ): Int = throw IOException("offline")

    override fun getUri(): Uri? = null

    override fun close() {
        Unit
    }
}
