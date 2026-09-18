package com.pierre.tunescout.core.model

private val sizeSegment = Regex("""\d+x\d+""")
private const val THUMBNAIL_PIXELS = 200
private const val MEDIUM_PIXELS = 600
private const val LARGE_PIXELS = 1000

@JvmInline
value class Artwork(
    val sourceUrl: String,
) {
    val thumbnailUrl: String
        get() = buildUrl(THUMBNAIL_PIXELS)

    val mediumUrl: String
        get() = buildUrl(MEDIUM_PIXELS)

    val largeUrl: String
        get() = buildUrl(LARGE_PIXELS)

    private fun buildUrl(pixels: Int): String {
        val fileName = sourceUrl.substringAfterLast('/')
        if (!sizeSegment.containsMatchIn(fileName)) return sourceUrl
        val resized = sizeSegment.replaceFirst(fileName, "${pixels}x$pixels")
        return sourceUrl.dropLast(fileName.length) + resized
    }
}
