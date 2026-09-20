package com.pierre.tunescout.ktlint

import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId

private val tuneScoutAbout = About(
    maintainer = "Pierre Vieira",
    repositoryUrl = "https://github.com/PierreVieira/TuneScout",
    issueTrackerUrl = "https://github.com/PierreVieira/TuneScout/issues",
)

/**
 * Base class for every custom rule in this rule set: fills in [RuleId] (prefixed with [RULE_SET_ID]) and the
 * shared [About] block so each rule only has to name itself.
 *
 * @param id the name of the rule, without the rule set prefix.
 */
abstract class TuneScoutRule(
    id: String,
) : Rule(ruleId = RuleId("$RULE_SET_ID:$id"), about = tuneScoutAbout),
    RuleAutocorrectApproveHandler
