package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import com.pinterest.ktlint.test.LintViolation
import org.junit.jupiter.api.Test

class DtoSerialNameRuleTest {
    private val dtoSerialNameRuleAssertThat = assertThatRule { DtoSerialNameRule() }

    @Test
    fun `flags a field without a serial name`() {
        val code =
            """
            @Serializable
            data class SongDto(
                val trackName: String,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 9, buildMissingSerialNameMessage("trackName"))
    }

    @Test
    fun `flags a var field without a serial name`() {
        val code =
            """
            @Serializable
            class AlbumDto(
                var collectionName: String,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(3, 9, buildMissingSerialNameMessage("collectionName"))
    }

    @Test
    fun `flags only the field that misses its serial name`() {
        val code =
            """
            @Serializable
            data class SongDto(
                @SerialName("trackId")
                val trackId: Long,
                val trackName: String,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(5, 9, buildMissingSerialNameMessage("trackName"))
    }

    @Test
    fun `flags a field with a default value`() {
        val code =
            """
            @Serializable
            data class SongDto(
                @SerialName("trackName")
                val trackName: String = "",
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 29, buildDefaultValueMessage("trackName"))
    }

    @Test
    fun `flags a nullable field that defaults to null`() {
        val code =
            """
            @Serializable
            data class SongDto(
                @SerialName("artworkUrl100")
                val artworkUrl: String? = null,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(4, 31, buildDefaultValueMessage("artworkUrl"))
    }

    @Test
    fun `flags both problems on the same field`() {
        val code =
            """
            @Serializable
            data class SongDto(
                val trackName: String = "",
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code)
            .hasLintViolationsWithoutAutoCorrect(
                LintViolation(3, 9, buildMissingSerialNameMessage("trackName")),
                LintViolation(3, 29, buildDefaultValueMessage("trackName")),
            )
    }

    @Test
    fun `allows a dto whose fields all name their key and have no default`() {
        val code =
            """
            @Serializable
            data class SongDto(
                @SerialName("trackId")
                val trackId: Long,
                @SerialName("artworkUrl100")
                val artworkUrl: String?,
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a class that is not a dto`() {
        val code =
            """
            data class Song(
                val trackName: String = "",
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a class that only mentions dto in the middle of its name`() {
        val code =
            """
            class SongDtoMapper(
                val fallbackTitle: String = "",
            )
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a plain constructor parameter because it is not a field`() {
        val code =
            """
            class SongDto(
                trackName: String = "",
            ) {
                @SerialName("trackName")
                val name: String = trackName
            }
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a dto without a primary constructor`() {
        val code =
            """
            @Serializable
            class EmptyDto
            """.trimIndent()

        dtoSerialNameRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildMissingSerialNameMessage(name: String): String =
    "DTO field '$name' must declare its JSON key with @SerialName"

private fun buildDefaultValueMessage(name: String): String =
    "DTO field '$name' must not have a default value; make the type nullable and let 'explicitNulls = false' " +
        "read an absent key as null"
