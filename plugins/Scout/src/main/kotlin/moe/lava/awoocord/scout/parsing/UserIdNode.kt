package moe.lava.awoocord.scout.parsing

import android.content.Context
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Rule
import com.discord.utilities.search.network.SearchQuery
import com.discord.utilities.search.query.FilterType
import com.discord.utilities.search.query.node.QueryNode
import com.discord.utilities.search.query.node.answer.AnswerNode
import com.discord.utilities.search.validation.SearchData
import java.util.regex.Pattern

class UserIdNode(private val userID: String) : AnswerNode() {
    companion object {
        fun getUserIdRule(): Rule<Context, QueryNode, Any> {
            val regex = Pattern.compile("^\\d{17,19}", Pattern.UNICODE_CASE)
            return SimpleParserRule(regex) { matcher, _, obj ->
                ParseSpec(UserIdNode(matcher.group()), obj)
            }
        }
    }

    override fun getValidFilters() = setOf(FilterType.FROM, FilterType.MENTIONS)
    override fun isValid(searchData: SearchData?) = true
    override fun getText() = userID

    override fun updateQuery(
        builder: SearchQuery.Builder?,
        searchData: SearchData?,
        filterType: FilterType?
    ) {
        checkNotNull(builder) { "queryBuilder" }
        checkNotNull(searchData) { "searchData" }
        val str = when (filterType) {
            FilterType.FROM -> "author_id"
            FilterType.MENTIONS -> "mentions"
            else -> return
        }
        builder.appendParam(str, userID)
    }
}
