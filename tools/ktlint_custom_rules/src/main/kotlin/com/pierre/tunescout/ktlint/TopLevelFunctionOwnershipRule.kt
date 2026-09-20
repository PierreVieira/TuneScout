package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * A top-level function has no owner: nothing can replace it in a test and nothing says which class it
 * serves. It belongs to a class with a clear responsibility (a factory, a mapper) that is injected.
 *
 * Four shapes stay at file scope: a `@Composable`, which is not a class member by nature, an extension
 * function, whose receiver is its owner, an `inline` function, which is control flow rather than a
 * collaborator, and a `private` helper that top-level code calls — the helper of a composable. A
 * `private` function that only one class of the file calls is that class's method.
 */
class TopLevelFunctionOwnershipRule : TuneScoutRule("top-level-function-ownership") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return
        if (node.parent?.elementType != FILE) return
        val function = node.psi as? KtNamedFunction ?: return
        if (function.receiverTypeReference != null || function.hasModifier(KtTokens.INLINE_KEYWORD)) return
        if (function.annotationEntries.any { annotation -> annotation.shortName?.asString() == "Composable" }) return

        val identifier = node.findChildByType(IDENTIFIER) ?: return
        if (!function.hasModifier(KtTokens.PRIVATE_KEYWORD)) {
            emit(
                identifier.startOffset,
                "Top-level function '${function.name}' has no owner; declare it as a method of a class with a " +
                    "clear responsibility and inject that class",
                false,
            )
            return
        }
        val owner = function.findSingleOwningClassOrNull() ?: return
        emit(
            identifier.startOffset,
            "Top-level private function '${function.name}' is only called by '${owner.name}' and should be " +
                "declared as its method",
            false,
        )
    }

    private fun KtNamedFunction.findSingleOwningClassOrNull(): KtClassOrObject? {
        val callers = containingKtFile
            .collectDescendantsOfType<KtNameReferenceExpression>()
            .filter { reference -> reference.getReferencedName() == name && !isAncestor(reference) }
            .map { reference -> reference.parents.filterIsInstance<KtClassOrObject>().lastOrNull() }
        return callers.distinct().singleOrNull()
    }
}
