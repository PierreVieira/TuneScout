package com.pierre.tunescout.screenshots

import android.graphics.BitmapFactory
import coil3.Image
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.asImage
import coil3.test.FakeImageLoaderEngine

private const val ARTWORK_HOST = "https://artwork.tunescout.test"
private val artworkResources = mapOf(
    "homework" to "artwork/homework.jpg",
    "discovery" to "artwork/discovery.jpg",
    "random_access_memories" to "artwork/random_access_memories.jpg",
)

fun artworkUrl(album: String): String = "$ARTWORK_HOST/$album/100x100bb.jpg"

fun createArtworkImageLoader(context: PlatformContext): ImageLoader {
    val engine = artworkResources
        .entries
        .fold(FakeImageLoaderEngine.Builder()) { builder, (album, resource) ->
            builder.intercept(
                predicate = { data -> data is String && data.startsWith("$ARTWORK_HOST/$album/") },
                image = decodeArtwork(resource),
            )
        }.build()
    return ImageLoader
        .Builder(context)
        .components { add(engine) }
        .build()
}

private fun decodeArtwork(resource: String): Image {
    val loader = checkNotNull(Thread.currentThread().contextClassLoader) { "No class loader for $resource" }
    val stream = checkNotNull(loader.getResourceAsStream(resource)) { "Missing artwork resource $resource" }
    return stream.use(BitmapFactory::decodeStream).asImage()
}
