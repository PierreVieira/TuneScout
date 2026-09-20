package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * A KDoc follows the shape of the official Kotlin documentation: a documented class describes what its
 * constructor takes — `@property` for each `val` / `var`, `@param` for each plain and type parameter —
 * and a documented function that returns a value says what with `@return`.
 *
 * Nothing here asks for a KDoc to exist: the rule only completes the ones that do. A tag that names
 * something the declaration no longer has is reported too, so a rename cannot leave a stale one behind.
 */
class KdocTagsRule : TuneScoutRule("kdoc-tags") {
    private val valuelessReturnTypes = setOf("Unit", "Nothing")
    private val namedTag = Regex("""@(param|property)\s+\[?(\w+)]?""")
    private val returnTag = Regex("""@return\b""")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        when (node.elementType) {
            CLASS -> (node.psi as? KtClass)?.let { ktClass -> visitClass(ktClass, emit) }
            FUN -> (node.psi as? KtNamedFunction)?.let { function -> visitFunction(function, emit) }
        }
    }

    private fun visitClass(
        ktClass: KtClass,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val kdoc = ktClass.docComment ?: return
        val expectedTags =
            ktClass.typeParameters.mapNotNull { parameter -> parameter.name }.map { name -> PARAM to name } +
                ktClass.primaryConstructorParameters.mapNotNull { parameter ->
                    parameter.name?.let { name -> (if (parameter.hasValOrVar()) PROPERTY else PARAM) to name }
                }
        val declaredTags = namedTag
            .findAll(kdoc.text)
            .map { match ->
                match.groupValues[1] to match.groupValues[2]
            }.toList()

        expectedTags.filterNot { tag -> tag in declaredTags }.forEach { (tag, name) ->
            emit(kdoc.textOffset, "KDoc of '${ktClass.name}' must describe '$name' with '@$tag $name'", false)
        }
        declaredTags.filterNot { tag -> tag in expectedTags }.forEach { (tag, name) ->
            emit(
                kdoc.textOffset,
                "KDoc of '${ktClass.name}' has '@$tag $name', which its constructor does not declare",
                false,
            )
        }
    }

    private fun visitFunction(
        function: KtNamedFunction,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val kdoc = function.docComment ?: return
        val returnType = function.typeReference?.text ?: return
        if (returnType in valuelessReturnTypes || hasReturnTag(kdoc)) return
        emit(
            kdoc.textOffset,
            "KDoc of '${function.name}' must describe the '$returnType' it returns with '@return'",
            false,
        )
    }

    private fun hasReturnTag(kdoc: KDoc): Boolean = returnTag.containsMatchIn(kdoc.text)

    private companion object {
        const val PARAM = "param"
        const val PROPERTY = "property"
    }
}
