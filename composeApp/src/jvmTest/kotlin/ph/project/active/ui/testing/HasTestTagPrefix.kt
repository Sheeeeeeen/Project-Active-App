package ph.project.active.ui.testing

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher

/** Matches nodes whose test tag starts with [prefix], for example every `student_class_card_` card. */
fun hasTestTagPrefix(prefix: String): SemanticsMatcher =
    SemanticsMatcher("TestTag starts with $prefix") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
    }
