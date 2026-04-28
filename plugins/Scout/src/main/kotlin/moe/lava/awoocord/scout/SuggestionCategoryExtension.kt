package moe.lava.awoocord.scout

import com.discord.utilities.search.suggestion.entries.SearchSuggestion

object SuggestionCategoryExtension {
    lateinit var AUTHOR_TYPE: SearchSuggestion.Category
    lateinit var values: Array<SearchSuggestion.Category>

    object AdapterType {
        const val AUTHOR_TYPE = 7
    }
}
