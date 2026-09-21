package com.pierre.tunescout.screenshotfixtures

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val HOMEWORK_ID = 10L
private const val DISCOVERY_ID = 20L
private const val RANDOM_ACCESS_MEMORIES_ID = 30L
private val homeworkArtwork = Artwork(artworkUrl("homework"))
private val discoveryArtwork = Artwork(artworkUrl("discovery"))
private val randomAccessMemoriesArtwork = Artwork(artworkUrl("random_access_memories"))

val oneMoreTime: Song = discoverySong(id = 1, title = "One More Time", trackNumber = 1)
val harderBetterFasterStronger: Song = discoverySong(
    id = 2,
    title = "Harder Better Faster Stronger",
    trackNumber = 4,
)
val faceToFace: Song = discoverySong(id = 3, title = "Face to Face", trackNumber = 10)
val daFunk: Song = homeworkSong(id = 4, title = "Da Funk", trackNumber = 3)
val aroundTheWorld: Song = homeworkSong(id = 5, title = "Around the World", trackNumber = 7)
val getLucky: Song = randomAccessMemoriesSong(
    id = 6,
    title = "Get Lucky",
    artistName = "Daft Punk, Pharrell Williams & Nile Rodgers",
    trackNumber = 8,
)
val instantCrush: Song = randomAccessMemoriesSong(
    id = 7,
    title = "Instant Crush",
    artistName = "Daft Punk & Julian Casablancas",
    trackNumber = 5,
)
val loseYourselfToDance: Song = randomAccessMemoriesSong(
    id = 8,
    title = "Lose Yourself to Dance",
    artistName = "Daft Punk & Pharrell Williams",
    trackNumber = 6,
)
val giorgioByMoroder: Song = randomAccessMemoriesSong(id = 9, title = "Giorgio by Moroder", trackNumber = 3)
val touch: Song = randomAccessMemoriesSong(
    id = 11,
    title = "Touch",
    artistName = "Daft Punk & Paul Williams",
    trackNumber = 7,
)
val recentlyPlayed: List<Song> = listOf(
    aroundTheWorld,
    harderBetterFasterStronger,
    instantCrush,
    getLucky,
    daFunk,
)
val searchSongs: List<Song> = listOf(
    oneMoreTime,
    getLucky,
    aroundTheWorld,
    harderBetterFasterStronger,
    instantCrush,
    loseYourselfToDance,
    daFunk,
    faceToFace,
    giorgioByMoroder,
    touch,
)
val randomAccessMemories: Album = album(
    id = RANDOM_ACCESS_MEMORIES_ID,
    title = "Random Access Memories",
    artwork = randomAccessMemoriesArtwork,
    songs = listOf(
        randomAccessMemoriesSong(id = 12, title = "Give Life Back to Music", trackNumber = 1),
        randomAccessMemoriesSong(id = 13, title = "The Game of Love", trackNumber = 2),
        giorgioByMoroder,
        randomAccessMemoriesSong(id = 14, title = "Within", trackNumber = 4),
        instantCrush,
        loseYourselfToDance,
        touch,
        getLucky,
        randomAccessMemoriesSong(id = 15, title = "Beyond", trackNumber = 9),
        randomAccessMemoriesSong(id = 16, title = "Motherboard", trackNumber = 10),
        randomAccessMemoriesSong(id = 17, title = "Fragments of Time", trackNumber = 11),
    ),
)

private fun homeworkSong(
    id: Long,
    title: String,
    trackNumber: Int,
    duration: Duration = 300.seconds,
): Song = song(
    id = id,
    title = title,
    artistName = "Daft Punk",
    albumId = HOMEWORK_ID,
    albumTitle = "Homework",
    artwork = homeworkArtwork,
    duration = duration,
    trackNumber = trackNumber,
)

private fun discoverySong(
    id: Long,
    title: String,
    trackNumber: Int,
    duration: Duration = 300.seconds,
): Song = song(
    id = id,
    title = title,
    artistName = "Daft Punk",
    albumId = DISCOVERY_ID,
    albumTitle = "Discovery",
    artwork = discoveryArtwork,
    duration = duration,
    trackNumber = trackNumber,
)

private fun randomAccessMemoriesSong(
    id: Long,
    title: String,
    trackNumber: Int,
    artistName: String = "Daft Punk",
    duration: Duration = 300.seconds,
): Song = song(
    id = id,
    title = title,
    artistName = artistName,
    albumId = RANDOM_ACCESS_MEMORIES_ID,
    albumTitle = "Random Access Memories",
    artwork = randomAccessMemoriesArtwork,
    duration = duration,
    trackNumber = trackNumber,
)
