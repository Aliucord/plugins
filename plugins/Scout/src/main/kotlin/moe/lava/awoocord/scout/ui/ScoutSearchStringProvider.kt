package moe.lava.awoocord.scout.ui

import android.content.Context
import com.discord.utilities.search.query.FilterType
import com.discord.utilities.search.query.node.answer.HasAnswerOption
import moe.lava.awoocord.scout.FilterTypeExtension
import moe.lava.awoocord.scout.HasAnswerOptionExtension

private fun String.decapitalise(context: Context) =
    this.replaceFirstChar { it.lowercase(context.resources.configuration.locales[0]) }

class ScoutSearchStringProvider(private val context: Context) {
    fun getIdentifier(name: String) =
        context.resources.getIdentifier(name, "string", "com.discord")
    fun getString(name: String) =
        context.getString(getIdentifier(name))

    fun stringFor(type: FilterType) = when (type) {
        FilterTypeExtension.EXCLUDE -> excludeFilterString
        FilterTypeExtension.BEFORE -> beforeFilterString
        FilterTypeExtension.DURING -> duringFilterString
        FilterTypeExtension.AFTER -> afterFilterString
        FilterTypeExtension.SORT -> sortFilterString
        FilterTypeExtension.AUTHOR_TYPE -> authorTypeFilter
        else -> throw IllegalArgumentException("invalid extended filter type")
    }

    fun stringFor(type: HasAnswerOption) = when (type) {
        HasAnswerOptionExtension.POLL -> hasPollString
        HasAnswerOptionExtension.SNAPSHOT -> hasForwardString
        else -> throw IllegalArgumentException("invalid extended filter type")
    }

    // Surprising!! Discord has localised strings of these
    val beforeFilterString: String
        get() = getString("search_filter_before")
    val duringFilterString: String
        get() = getString("search_filter_during")
    val afterFilterString: String
        get() = getString("search_filter_after")
    val sortFilterString: String
        get() = getString("sort").decapitalise(context)
    val sortOldString: String
        get() = getString("search_oldest_short").decapitalise(context)
    val expandFilterString: String
        get() = getString("friends_pending_request_expand")

    // Not localised
    val hasPollString: String
        get() = "poll"
    val hasForwardString: String
        get() = "forward"
    val excludeFilterString: String
        get() = "exclude"
    val authorTypeFilter: String
        get() = "authorType"
    val authorTypeAnswer: String
        // TODO, could probably be localisable by joining each part together
        get() = "user, bot or webhook"
}
