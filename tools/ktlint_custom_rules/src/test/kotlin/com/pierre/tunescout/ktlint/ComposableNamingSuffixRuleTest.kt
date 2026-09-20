package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ComposableNamingSuffixRuleTest {
    private val composableNamingSuffixRuleAssertThat = assertThatRule { ComposableNamingSuffixRule() }

    @Test
    fun `flags a composable whose name ends in a word outside the list`() {
        val code =
            """
            @Composable
            fun SongItem(title: String) {
                Text(text = title)
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("SongItem"))
    }

    @Test
    fun `flags a private composable`() {
        val code =
            """
            @Composable
            private fun PlayerControls() {
                Row {}
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("PlayerControls"))
    }

    @Test
    fun `flags a composable that carries an allowed word anywhere but the end`() {
        val code =
            """
            @Composable
            fun ScreenWrapper() {
                Box {}
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("ScreenWrapper"))
    }

    @Test
    fun `flags a composable whose suffix is not capitalized`() {
        val code =
            """
            @Composable
            fun Songscreen() {
                Box {}
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("Songscreen"))
    }

    @Test
    fun `allows a composable that ends in an allowed suffix`() {
        val code =
            """
            @Composable
            fun SongsScreen() {
                Box {}
            }

            @Composable
            private fun SongRow(title: String) {
                Text(text = title)
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows the component fallback suffix`() {
        val code =
            """
            @Composable
            fun PlayerControlsComponent() {
                Row {}
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a lowercase composable that returns a value`() {
        val code =
            """
            @Composable
            fun rememberBarSong(): Song? = remember { null }

            @Composable
            fun songCountText(count: Int): String = count.toString()
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a preview named after what it previews`() {
        val code =
            """
            @Preview
            @Composable
            private fun SongRowPreview() {
                SongRow(title = "Song")
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a multi preview annotation`() {
        val code =
            """
            @PreviewLightDark
            @Composable
            private fun SongRowPreview() {
                SongRow(title = "Song")
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an uppercase function that is not a composable`() {
        val code =
            """
            fun SongItem(title: String): Song = Song(title = title)
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Composable '$name' must end with one of the allowed suffixes: Action, Artwork, Badge, Bar, Box, Button, " +
        "Card, Cell, Collector, Component, Content, Cover, Dialog, Effect, Field, Grid, Handle, Header, Heading, " +
        "Icon, Image, Label, Line, List, Message, Row, Scaffold, Screen, Sheet, Skeleton, Text, Theme, Title, " +
        "Toggle. When none describes it, use 'Component'"
