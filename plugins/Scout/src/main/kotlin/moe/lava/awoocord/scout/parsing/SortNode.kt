package moe.lava.awoocord.scout.parsing

import android.content.Context
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Rule
import com.discord.utilities.search.network.SearchQuery
import com.discord.utilities.search.query.FilterType
import com.discord.utilities.search.query.node.QueryNode
import com.discord.utilities.search.query.node.answer.AnswerNode
import com.discord.utilities.search.query.node.filter.FilterNode
import com.discord.utilities.search.validation.SearchData
import moe.lava.awoocord.scout.FilterTypeExtension
import moe.lava.awoocord.scout.ui.ScoutSearchStringProvider
import java.util.regex.Pattern

class SortNode(private val text: String): AnswerNode() {
    companion object {
        fun getSortRule(ssProvider: ScoutSearchStringProvider): Rule<Context, QueryNode, Any> {
            val regexStr = "^\\s*(${ssProvider.sortOldString})"
            val regex = Pattern.compile(regexStr, Pattern.UNICODE_CASE)
            return SimpleParserRule(regex) { _, _, obj ->
                ParseSpec(SortNode(ssProvider.sortOldString), obj)
            }
        }

        fun getFilterRule(str: String): ParserRule {
            val regex = Pattern.compile("^\\s*?(${str}):", 64)
            return SimpleParserRule(regex) { _, _, obj ->
                ParseSpec(FilterNode(FilterTypeExtension.SORT, str), obj)
            }
        }
    }

    override fun getValidFilters() = setOf(FilterTypeExtension.SORT)
    override fun isValid(searchData: SearchData?) = true
    override fun getText() = this.text

    override fun updateQuery(
        builder: SearchQuery.Builder,
        searchData: SearchData?,
        filterType: FilterType?
    ) {
        builder.appendParam("sort_order", "asc")
    }
}
