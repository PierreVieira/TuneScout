package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class KdocTagsRuleTest {
    private val kdocTagsRuleAssertThat = assertThatRule { KdocTagsRule() }

    @Test
    fun `flags a documented class that does not describe its property`() {
        val code =
            """
            /**
             * The song a list marks as the one the player is on.
             */
            data class NowPlaying(
                val songId: Long,
            )
            """.trimIndent()

        kdocTagsRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 1, buildMissingMessage("NowPlaying", "property", "songId"))
    }

    @Test
    fun `flags a plain constructor parameter and a type parameter without a param tag`() {
        val code =
            """
            /**
             * A sheet, or a dialog in a window too short to open one.
             */
            class BottomSheetScene<T : Any>(
                entry: NavEntry<T>,
            )
            """.trimIndent()

        kdocTagsRuleAssertThat(code).hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 1, buildMissingMessage("BottomSheetScene", "param", "T")),
            LintViolation(1, 1, buildMissingMessage("BottomSheetScene", "param", "entry")),
        )
    }

    @Test
    fun `flags a property described with a param tag`() {
        val code =
            """
            /**
             * A group of members.
             *
             * @param name the name of this group.
             */
            class Group(
                val name: String,
            )
            """.trimIndent()

        kdocTagsRuleAssertThat(code).hasLintViolationsWithoutAutoCorrect(
            LintViolation(1, 1, buildMissingMessage("Group", "property", "name")),
            LintViolation(1, 1, buildStaleMessage("Group", "param", "name")),
        )
    }

    @Test
    fun `flags a tag that names something the constructor does not declare`() {
        val code =
            """
            /**
             * A group of members.
             *
             * @property name the name of this group.
             * @property size how many members it has.
             */
            class Group(
                val name: String,
            )
            """.trimIndent()

        kdocTagsRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(1, 1, buildStaleMessage("Group", "property", "size"))
    }

    @Test
    fun `flags a documented function that does not describe what it returns`() {
        val code =
            """
            /**
             * Adds a [member] to this group.
             */
            fun add(member: String): Int = members.size
            """.trimIndent()

        kdocTagsRuleAssertThat(code).hasLintViolationWithoutAutoCorrect(1, 1, buildReturnMessage("add", "Int"))
    }

    @Test
    fun `allows a class that describes everything its constructor takes`() {
        val code =
            """
            /**
             * A group of members.
             *
             * @param T the type of a member in this group.
             * @property name the name of this group.
             * @param capacity how many members fit in it.
             */
            class Group<T>(
                val name: String,
                capacity: Int,
            )
            """.trimIndent()

        kdocTagsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a function that describes what it returns`() {
        val code =
            """
            /**
             * Adds a [member] to this group.
             *
             * @return the new size of the group.
             */
            fun add(member: String): Int = members.size
            """.trimIndent()

        kdocTagsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a documented function that returns nothing`() {
        val code =
            """
            /**
             * The service is launched only once the player is actually playing.
             */
            fun handlePlaybackStarted() {
                serviceLauncher.launch()
            }
            """.trimIndent()

        kdocTagsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows declarations without a KDoc`() {
        val code =
            """
            class Group(
                val name: String,
            ) {
                fun add(member: String): Int = members.size
            }
            """.trimIndent()

        kdocTagsRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a documented class without a constructor`() {
        val code =
            """
            /**
             * Reads the country the store is searched in.
             */
            fun interface CountryProvider {
                fun getCountry(): String
            }
            """.trimIndent()

        kdocTagsRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildMissingMessage(
    owner: String,
    tag: String,
    name: String,
): String = "KDoc of '$owner' must describe '$name' with '@$tag $name'"

private fun buildStaleMessage(
    owner: String,
    tag: String,
    name: String,
): String = "KDoc of '$owner' has '@$tag $name', which its constructor does not declare"

private fun buildReturnMessage(
    name: String,
    returnType: String,
): String = "KDoc of '$name' must describe the '$returnType' it returns with '@return'"
