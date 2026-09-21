package com.pierre.tunescout.core.navigation.scene

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ListDetailSceneStrategyTest {
    private val home = entry(key = "home", metadata = ListDetailSceneStrategy.listPane())
    private val firstAlbum = entry(key = "album 10", metadata = ListDetailSceneStrategy.detailPane())
    private val secondAlbum = entry(key = "album 20", metadata = ListDetailSceneStrategy.detailPane())
    private val player = entry(key = "player")
    private val splash = entry(key = "splash")

    private lateinit var strategy: ListDetailSceneStrategy<String>

    @Test
    fun `GIVEN two panes and a detail over the list WHEN calculating THEN lays them side by side`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val scene = calculateScene(listOf(home, firstAlbum))

        // Then
        assertThat(scene?.entries).containsExactly(home, firstAlbum).inOrder()
        assertThat(scene?.previousEntries).containsExactly(home)
    }

    @Test
    fun `GIVEN two panes and a detail over another WHEN calculating THEN the top one replaces it beside the list`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val scene = calculateScene(listOf(home, firstAlbum, secondAlbum))

        // Then
        assertThat(scene?.entries).containsExactly(home, secondAlbum).inOrder()
        assertThat(scene?.previousEntries).containsExactly(home, firstAlbum).inOrder()
    }

    @Test
    fun `GIVEN two panes WHEN a detail replaces another THEN the scene keeps the list's key`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val first = calculateScene(listOf(home, firstAlbum))
        val second = calculateScene(listOf(home, firstAlbum, secondAlbum))

        // Then
        assertThat(first?.key).isEqualTo(home.contentKey)
        assertThat(second?.key).isEqualTo(first?.key)
    }

    @Test
    fun `GIVEN one pane WHEN calculating THEN leaves the detail to cover the list`() {
        // Given
        prepareScenario(isTwoPane = false)

        // When
        val scene = calculateScene(listOf(home, firstAlbum))

        // Then
        assertThat(scene).isNull()
    }

    @Test
    fun `GIVEN two panes and the list alone WHEN calculating THEN lays it beside the empty detail pane`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val scene = calculateScene(listOf(splash, home))

        // Then
        assertThat(scene?.entries).containsExactly(home)
        assertThat(scene?.previousEntries).containsExactly(splash)
    }

    @Test
    fun `GIVEN two panes WHEN a detail opens over the list alone THEN the scene changes`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val alone = calculateScene(listOf(home))
        val withDetail = calculateScene(listOf(home, firstAlbum))

        // Then
        assertThat(withDetail?.key).isNotEqualTo(alone?.key)
    }

    @Test
    fun `GIVEN one pane and the list alone WHEN calculating THEN leaves it to a single pane`() {
        // Given
        prepareScenario(isTwoPane = false)

        // When
        val scene = calculateScene(listOf(home))

        // Then
        assertThat(scene).isNull()
    }

    @Test
    fun `GIVEN two panes and a screen alone WHEN calculating THEN leaves it to a single pane`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val scene = calculateScene(listOf(splash))

        // Then
        assertThat(scene).isNull()
    }

    @Test
    fun `GIVEN two panes and a screen over the detail WHEN calculating THEN leaves the screen to a single pane`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val scene = calculateScene(listOf(home, firstAlbum, player))

        // Then
        assertThat(scene).isNull()
    }

    @Test
    fun `GIVEN two panes and a detail over a screen WHEN calculating THEN leaves it to a single pane`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val scene = calculateScene(listOf(home, player, firstAlbum))

        // Then
        assertThat(scene).isNull()
    }

    @Test
    fun `GIVEN two panes and details with no list under them WHEN calculating THEN leaves them to a single pane`() {
        // Given
        prepareScenario(isTwoPane = true)

        // When
        val scene = calculateScene(listOf(firstAlbum, secondAlbum))

        // Then
        assertThat(scene).isNull()
    }

    private fun calculateScene(entries: List<NavEntry<String>>): Scene<String>? = with(strategy) {
        SceneStrategyScope<String>().calculateScene(entries)
    }

    private fun entry(
        key: String,
        metadata: Map<String, Any> = emptyMap(),
    ): NavEntry<String> = NavEntry(key = key, metadata = metadata) {}

    private fun prepareScenario(isTwoPane: Boolean) {
        strategy = ListDetailSceneStrategy(isTwoPane = isTwoPane, emptyDetailPane = {})
    }
}
