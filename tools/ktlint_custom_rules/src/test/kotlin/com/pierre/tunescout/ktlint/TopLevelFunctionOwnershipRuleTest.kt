package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class TopLevelFunctionOwnershipRuleTest {
    private val topLevelFunctionOwnershipRuleAssertThat = assertThatRule { TopLevelFunctionOwnershipRule() }

    @Test
    fun `flags a public top level function`() {
        val code =
            """
            fun createExoPlayer(context: Context): ExoPlayer = ExoPlayer.Builder(context).build()
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 5, buildNoOwnerMessage("createExoPlayer"))
    }

    @Test
    fun `flags an internal top level function`() {
        val code =
            """
            internal fun buildSongKey(id: Long): String = id.toString()
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 14, buildNoOwnerMessage("buildSongKey"))
    }

    @Test
    fun `flags a private function only the single class in the file calls`() {
        val code =
            """
            private fun formatDuration(seconds: Int): String = seconds.toString()

            class SongMapper {
                fun toLabel(seconds: Int): String = formatDuration(seconds)
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 13, buildSingleCallerMessage("formatDuration", "SongMapper"))
    }

    @Test
    fun `flags a private function only an object calls`() {
        val code =
            """
            private fun buildTag(name: String): String = name.uppercase()

            internal object Logger {
                fun log(name: String) {
                    println(buildTag(name))
                }
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 13, buildSingleCallerMessage("buildTag", "Logger"))
    }

    @Test
    fun `flags a private function a nested class calls and names the outermost class`() {
        val code =
            """
            private fun buildKey(id: Long): String = id.toString()

            class SongsCache {
                class Entry(val id: Long) {
                    fun key(): String = buildKey(id)
                }
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 13, buildSingleCallerMessage("buildKey", "SongsCache"))
    }

    @Test
    fun `flags a recursive private function only one class calls`() {
        val code =
            """
            private fun countDown(from: Int): Int = if (from == 0) 0 else countDown(from - 1)

            class Timer {
                fun run(): Int = countDown(3)
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 13, buildSingleCallerMessage("countDown", "Timer"))
    }

    @Test
    fun `allows a top level composable`() {
        val code =
            """
            @Composable
            fun SongRow(title: String) {
                Text(text = title)
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an extension function because its receiver owns it`() {
        val code =
            """
            fun SongDto.toSong(): Song = Song(id = id)
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an inline function`() {
        val code =
            """
            inline fun runCatchingCancellable(block: () -> Unit) {
                block()
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private helper a top level composable calls`() {
        val code =
            """
            @Composable
            fun SongRow(seconds: Int) {
                Text(text = formatDuration(seconds))
            }

            private fun formatDuration(seconds: Int): String = seconds.toString()
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private helper a class and top level code share`() {
        val code =
            """
            private fun formatDuration(seconds: Int): String = seconds.toString()

            class SongMapper {
                fun toLabel(seconds: Int): String = formatDuration(seconds)
            }

            @Composable
            fun DurationText(seconds: Int) {
                Text(text = formatDuration(seconds))
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private helper two classes share`() {
        val code =
            """
            private fun formatDuration(seconds: Int): String = seconds.toString()

            class SongMapper {
                fun toLabel(seconds: Int): String = formatDuration(seconds)
            }

            class AlbumMapper {
                fun toLabel(seconds: Int): String = formatDuration(seconds)
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a private function nothing calls`() {
        val code =
            """
            private fun formatDuration(seconds: Int): String = seconds.toString()

            class SongMapper
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a method of a class`() {
        val code =
            """
            class ExoPlayerFactory {
                fun createExoPlayer(context: Context): ExoPlayer = ExoPlayer.Builder(context).build()
            }
            """.trimIndent()

        topLevelFunctionOwnershipRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildNoOwnerMessage(name: String): String =
    "Top-level function '$name' has no owner; declare it as a method of a class with a clear responsibility " +
        "and inject that class"

private fun buildSingleCallerMessage(
    name: String,
    ownerName: String,
): String = "Top-level private function '$name' is only called by '$ownerName' and should be declared as its method"
