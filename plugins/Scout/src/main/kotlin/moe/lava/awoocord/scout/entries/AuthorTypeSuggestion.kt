package moe.lava.awoocord.scout.entries

import com.discord.utilities.search.suggestion.entries.SearchSuggestion
import moe.lava.awoocord.scout.SuggestionCategoryExtension
import moe.lava.awoocord.scout.parsing.AuthorType

data class AuthorTypeSuggestion(val type: AuthorType) : SearchSuggestion {
    override fun getCategory() = SuggestionCategoryExtension.AUTHOR_TYPE
}
