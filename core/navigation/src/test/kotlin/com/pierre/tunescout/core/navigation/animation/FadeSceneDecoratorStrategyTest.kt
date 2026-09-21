package com.pierre.tunescout.core.navigation.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FadeSceneDecoratorStrategyTest {
    private lateinit var strategy: FadeSceneDecoratorStrategy<String>

    @BeforeEach
    fun setUp() {
        strategy = FadeSceneDecoratorStrategy()
    }

    @Test
    fun `WHEN decorating a scene THEN its key pairs the scene's class with the scene's own key`() {
        // When
        val decorated = decorate(FakeScene(key = "player"))

        // Then
        assertThat(decorated.key).isEqualTo(FakeScene::class to "player")
    }

    @Test
    fun `WHEN decorating a scene THEN it keeps the scene's entries, previous entries and metadata`() {
        // Given
        val scene = FakeScene(key = "player")

        // When
        val decorated = decorate(scene)

        // Then
        assertThat(decorated.entries).isEqualTo(scene.entries)
        assertThat(decorated.previousEntries).isEqualTo(scene.previousEntries)
        assertThat(decorated.metadata).isEqualTo(scene.metadata)
    }

    @Test
    fun `WHEN decorating the same scene twice THEN both decorations are equal`() {
        // Given
        val scene = FakeScene(key = "player")

        // When
        val first = decorate(scene)
        val second = decorate(scene)

        // Then
        assertThat(first).isEqualTo(second)
        assertThat(first.hashCode()).isEqualTo(second.hashCode())
    }

    @Test
    fun `WHEN decorating two different scenes THEN the decorations differ`() {
        // When
        val player = decorate(FakeScene(key = "player"))
        val album = decorate(FakeScene(key = "album"))

        // Then
        assertThat(player).isNotEqualTo(album)
        assertThat(player).isNotEqualTo(FakeScene(key = "player"))
    }

    @Test
    fun `WHEN creating the transform THEN it runs no fade of its own`() {
        // Given
        val scope = mockk<AnimatedContentTransitionScope<String>>()

        // When
        val transform = scope.createSceneFadeTransform()

        // Then
        assertThat(transform.targetContentEnter).isEqualTo(EnterTransition.None)
        assertThat(transform.initialContentExit).isEqualTo(ExitTransition.None)
    }

    private fun decorate(scene: Scene<String>): Scene<String> =
        with(strategy) { SceneDecoratorStrategyScope<String>().decorateScene(scene) }
}

private data class FakeScene(
    override val key: Any,
) : Scene<String> {
    override val entries: List<NavEntry<String>> = listOf(createEntry("home"), createEntry("$key"))
    override val previousEntries: List<NavEntry<String>> = listOf(createEntry("home"))
    override val content: @Composable () -> Unit = {}
}

private fun createEntry(key: String): NavEntry<String> = NavEntry(key = key, metadata = mapOf("route" to key)) {}
