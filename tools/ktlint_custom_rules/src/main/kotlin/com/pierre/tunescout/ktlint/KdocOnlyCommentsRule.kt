package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Documentation is written as KDoc on a declaration, where the IDE and Dokka can find it. A `//` or
 * block comment is reported; what it explains either becomes the KDoc of the declaration it
 * talks about or of a small function extracted to carry it.
 *
 * The `// Given` / `// When` / `// Then` markers that structure a test body are the one exception.
 */
class KdocOnlyCommentsRule : TuneScoutRule("kdoc-only-comments") {
    private val commentElementTypes = setOf(EOL_COMMENT, BLOCK_COMMENT)
    private val testSectionMarker = Regex("""// (Given|When|Then)( / (Given|When|Then))*""")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType !in commentElementTypes) return
        if (testSectionMarker.matches(node.text)) return
        emit(
            node.startOffset,
            "Comments must be KDoc (/** ... */) on a declaration; move this text to the KDoc of the declaration " +
                "it explains, or extract one to carry it",
            false,
        )
    }
}
