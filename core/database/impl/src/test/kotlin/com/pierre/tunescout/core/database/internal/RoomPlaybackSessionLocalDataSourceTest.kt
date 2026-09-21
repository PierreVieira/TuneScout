package com.pierre.tunescout.core.database.internal

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.PlaybackSessionDao
import com.pierre.tunescout.core.database.entity.PlaybackQueueEntity
import com.pierre.tunescout.core.database.entity.PlaybackSessionEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class RoomPlaybackSessionLocalDataSourceTest {
    private lateinit var dao: FakePlaybackSessionDao
    private lateinit var localDataSource: RoomPlaybackSessionLocalDataSource

    @Test
    fun `GIVEN a session WHEN saving and reading it back THEN every entry survives in order`() = runTest {
        // Given
        prepareScenario()
        val session = session(
            entries = listOf(
                entry(id = "a", song = song(id = 1)),
                entry(id = "b", song = song(id = 9), source = QueueSource.UserQueue),
                entry(id = "c", song = song(id = 2)),
            ),
            currentEntryId = "a",
        )

        // When
        localDataSource.save(session)
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.entries?.map { queued -> queued.id })
            .containsExactly("a", "b", "c")
            .inOrder()
        assertThat(restored?.entries?.map { queued -> queued.source })
            .containsExactly(QueueSource.Context, QueueSource.UserQueue, QueueSource.Context)
            .inOrder()
        assertThat(restored?.currentEntryId).isEqualTo("a")
    }

    @Test
    fun `GIVEN the same song queued twice WHEN reading back THEN both entries come back`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(
            session(
                entries = listOf(entry(id = "a", song = song(id = 1)), entry(id = "b", song = song(id = 1))),
                currentEntryId = "a",
            ),
        )
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.entries?.map { queued -> queued.id }).containsExactly("a", "b").inOrder()
    }

    @Test
    fun `GIVEN an album context WHEN reading back THEN its id and title survive`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(
            session(
                entries = listOf(entry(id = "a", song = song(id = 1))),
                currentEntryId = "a",
                context = PlaybackContext.Album(id = 10, title = "Random Access Memories"),
                position = 12.seconds,
                repeatMode = RepeatMode.All,
            ),
        )
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.context).isEqualTo(PlaybackContext.Album(id = 10, title = "Random Access Memories"))
        assertThat(restored?.position).isEqualTo(12.seconds)
        assertThat(restored?.repeatMode).isEqualTo(RepeatMode.All)
    }

    @Test
    fun `GIVEN a playlist context WHEN reading back THEN its id and name survive`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(
            session(
                entries = listOf(entry(id = "a", song = song(id = 1))),
                currentEntryId = "a",
                context = PlaybackContext.Playlist(id = 3, title = "Road trip"),
            ),
        )
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.context).isEqualTo(PlaybackContext.Playlist(id = 3, title = "Road trip"))
    }

    @Test
    fun `GIVEN the liked songs context WHEN reading back THEN it is still the liked songs`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(
            session(
                entries = listOf(entry(id = "a", song = song(id = 1))),
                currentEntryId = "a",
                context = PlaybackContext.LikedSongs,
            ),
        )
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.context).isEqualTo(PlaybackContext.LikedSongs)
    }

    @Test
    fun `GIVEN the recently played context WHEN reading back THEN it is still the recently played`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(
            session(
                entries = listOf(entry(id = "a", song = song(id = 1))),
                currentEntryId = "a",
                context = PlaybackContext.RecentlyPlayed,
            ),
        )
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.context).isEqualTo(PlaybackContext.RecentlyPlayed)
    }

    @Test
    fun `GIVEN a context kind this version does not know WHEN reading back THEN it is a single song`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(session(entries = listOf(entry(id = "a", song = song(id = 1))), currentEntryId = "a"))
        dao.upsertSession(requireNotNull(dao.findSession()).copy(contextType = "Radio", contextId = 7))

        // When
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.context).isEqualTo(PlaybackContext.SingleSong)
    }

    @Test
    fun `GIVEN a shuffled queue WHEN reading back THEN it is still shuffled with the order to put back`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(
            session(
                entries = listOf(
                    entry(id = "a", song = song(id = 1)),
                    entry(id = "q", song = song(id = 9), source = QueueSource.UserQueue),
                    entry(id = "c", song = song(id = 3)),
                    entry(id = "b", song = song(id = 2)),
                ),
                currentEntryId = "a",
                isShuffleEnabled = true,
                unshuffledOrder = listOf("a", "b", "c"),
            ),
        )
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.isShuffleEnabled).isTrue()
        assertThat(restored?.unshuffledOrder).containsExactly("a", "b", "c").inOrder()
        assertThat(restored?.entries?.map { queued -> queued.id }).containsExactly("a", "q", "c", "b").inOrder()
    }

    @Test
    fun `GIVEN a queue in order WHEN reading back THEN there is no order to put back`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(
            session(entries = listOf(entry(id = "a", song = song(id = 1))), currentEntryId = "a"),
        )
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.isShuffleEnabled).isFalse()
        assertThat(restored?.unshuffledOrder).isEmpty()
        assertThat(restored?.repeatMode).isEqualTo(RepeatMode.Off)
    }

    @Test
    fun `GIVEN the song had ended WHEN reading back THEN it is still ended`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(
            session(
                entries = listOf(entry(id = "a", song = song(id = 1))),
                currentEntryId = "a",
                hasEnded = true,
            ),
        )
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.hasEnded).isTrue()
    }

    @Test
    fun `GIVEN a single song context WHEN reading back THEN it is not mistaken for an album`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(session(entries = listOf(entry(id = "a", song = song(id = 1))), currentEntryId = "a"))
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.context).isEqualTo(PlaybackContext.SingleSong)
    }

    @Test
    fun `GIVEN nothing was ever saved WHEN reading THEN there is no session`() = runTest {
        // Given
        prepareScenario()

        // When
        val restored = localDataSource.find()

        // Then
        assertThat(restored).isNull()
    }

    @Test
    fun `GIVEN the queue was emptied WHEN reading THEN there is nothing to restore`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(session(entries = listOf(entry(id = "a", song = song(id = 1))), currentEntryId = "a"))

        // When
        localDataSource.save(session(entries = emptyList(), currentEntryId = null))
        val restored = localDataSource.find()

        // Then
        assertThat(restored).isNull()
    }

    @Test
    fun `GIVEN a shorter queue WHEN saving over a longer one THEN the old entries are gone`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(
            session(
                entries = listOf(entry(id = "a", song = song(id = 1)), entry(id = "b", song = song(id = 2))),
                currentEntryId = "a",
            ),
        )

        // When
        localDataSource.save(session(entries = listOf(entry(id = "c", song = song(id = 3))), currentEntryId = "c"))
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.entries?.map { queued -> queued.id }).containsExactly("c")
    }

    @Test
    fun `GIVEN a queued song that is no longer stored WHEN reading THEN that entry is dropped`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(
            session(
                entries = listOf(entry(id = "a", song = song(id = 1)), entry(id = "b", song = song(id = 2))),
                currentEntryId = "a",
            ),
        )
        dao.songs.remove(2L)

        // When
        val restored = localDataSource.find()

        // Then
        assertThat(restored?.entries?.map { queued -> queued.id }).containsExactly("a")
    }

    private fun entry(
        id: String,
        song: com.pierre.tunescout.core.model.Song,
        source: QueueSource = QueueSource.Context,
    ): QueueEntry = QueueEntry(id = id, song = song, source = source)

    private fun session(
        entries: List<QueueEntry>,
        currentEntryId: String?,
        context: PlaybackContext = PlaybackContext.SingleSong,
        position: kotlin.time.Duration = 5.seconds,
        repeatMode: RepeatMode = RepeatMode.Off,
        isShuffleEnabled: Boolean = false,
        unshuffledOrder: List<String> = emptyList(),
        hasEnded: Boolean = false,
    ): PlaybackSession = PlaybackSession(
        entries = entries,
        currentEntryId = currentEntryId,
        context = context,
        position = position,
        repeatMode = repeatMode,
        isShuffleEnabled = isShuffleEnabled,
        unshuffledOrder = unshuffledOrder,
        hasEnded = hasEnded,
    )

    private fun prepareScenario() {
        dao = FakePlaybackSessionDao()
        localDataSource = RoomPlaybackSessionLocalDataSource(playbackSessionDao = dao, timestampProvider = { 0 })
    }
}

private class FakePlaybackSessionDao : PlaybackSessionDao {
    val songs = mutableMapOf<Long, SongEntity>()
    private var session: PlaybackSessionEntity? = null
    private val queue = mutableListOf<PlaybackQueueEntity>()

    override suspend fun upsertSession(session: PlaybackSessionEntity) {
        this.session = session
    }

    override suspend fun upsertSongs(songs: List<SongEntity>) {
        songs.forEach { song -> this.songs[song.id] = song }
    }

    override suspend fun clearQueue() {
        queue.clear()
    }

    override suspend fun insertQueue(entries: List<PlaybackQueueEntity>) {
        queue += entries
    }

    override suspend fun findSession(): PlaybackSessionEntity? = session

    override suspend fun findQueue(): List<PlaybackQueueEntity> = queue.sortedBy { entry -> entry.position }

    override suspend fun findSongs(songIds: List<Long>): List<SongEntity> = songIds.mapNotNull { id -> songs[id] }
}
