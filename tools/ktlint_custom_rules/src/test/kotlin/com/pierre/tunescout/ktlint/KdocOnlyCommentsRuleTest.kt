package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class KdocOnlyCommentsRuleTest {
    private val kdocOnlyCommentsRuleAssertThat = assertThatRule { KdocOnlyCommentsRule() }

    @Test
    fun `flags an end of line comment above a declaration`() {
        val code =
            """
            // Holds the queue the player walks through
            class QueueViewModel
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 1, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags an end of line comment trailing a statement`() {
        val code =
            """
            val pageSize = 25 // items per page
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 19, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a block comment`() {
        val code =
            """
            /* Holds the queue the player walks through */
            class QueueViewModel
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 1, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a multiline block comment inside a function body`() {
        val code =
            """
            fun play() {
                /*
                 * The player has to be prepared first
                 */
                player.prepare()
            }
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags a test section marker followed by more words`() {
        val code =
            """
            fun test() {
                // Given a playing song
                player.play()
            }
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(2, 5, VIOLATION_MESSAGE)
    }

    @Test
    fun `flags every test section marker that is not written exactly`() {
        val code =
            """
            fun test() {
                //Given
                player.play()
                // when
                player.pause()
                // Then:
                check(player.isPaused)
            }
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(2, 5, VIOLATION_MESSAGE),
                LintViolation(4, 5, VIOLATION_MESSAGE),
                LintViolation(6, 5, VIOLATION_MESSAGE),
            )
    }

    @Test
    fun `allows a kdoc on a declaration`() {
        val code =
            """
            /**
             * Holds the queue the player walks through.
             */
            class QueueViewModel
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows the test section markers`() {
        val code =
            """
            fun test() {
                // Given
                player.prepare()

                // When
                player.play()

                // Then
                check(player.isPlaying)
            }
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows combined test section markers`() {
        val code =
            """
            fun test() {
                // Given / When
                player.play()

                // Then
                check(player.isPlaying)
            }

            fun otherTest() {
                // Given / When / Then
                check(player.isIdle)
            }
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a string literal that looks like a comment`() {
        val code =
            """
            val baseUrl = "https://itunes.apple.com"
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows code without comments`() {
        val code =
            """
            class QueueViewModel {
                fun play() {
                    player.play()
                }
            }
            """.trimIndent()

        kdocOnlyCommentsRuleAssertThat(code).hasNoLintViolations()
    }

    private companion object {
        const val VIOLATION_MESSAGE =
            "Comments must be KDoc (/** ... */) on a declaration; move this text to the KDoc of the declaration " +
                "it explains, or extract one to carry it"
    }
}
