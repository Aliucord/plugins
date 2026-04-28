@file:Suppress("EnumValuesSoftDeprecate")

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
import java.util.regex.Pattern

// TODO: not localised, maybe one day
enum class AuthorType(val value: String) {
    User("user"),
    Bot("bot"),
    Webhook("webhook"),
    ;

    companion object {
        fun from(value: String) = when (value) {
            "user" -> User
            "bot" -> Bot
            "webhook" -> Webhook
            else -> throw IllegalArgumentException("Unknown author type $value")
        }
    }
}

class AuthorTypeNode(val type: AuthorType): AnswerNode() {
    companion object {
        fun getAuthorTypesRule(): Rule<Context, QueryNode, Any> {
            val joined = AuthorType.values().joinToString("|") { it.value }
            val regexStr = "^\\s*(${joined})"
            val regex = Pattern.compile(regexStr, Pattern.UNICODE_CASE)
            return SimpleParserRule(regex) { matcher, _, obj ->
                ParseSpec(AuthorTypeNode(AuthorType.from(matcher.group())), obj)
            }
        }

        fun getFilterRule(str: String): ParserRule {
            val regex = Pattern.compile("^\\s*?(${str}):", 64)
            return SimpleParserRule(regex) { _, _, obj ->
                ParseSpec(FilterNode(FilterTypeExtension.AUTHOR_TYPE, str), obj)
            }
        }
    }

    override fun getValidFilters() = setOf(FilterTypeExtension.AUTHOR_TYPE)
    override fun isValid(searchData: SearchData?) = true
    override fun getText() = type.value

    override fun updateQuery(
        builder: SearchQuery.Builder,
        searchData: SearchData?,
        filterType: FilterType?
    ) {
        builder.appendParam("author_type", type.value)
    }
}
