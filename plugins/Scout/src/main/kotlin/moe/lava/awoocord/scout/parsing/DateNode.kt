package moe.lava.awoocord.scout.parsing

import com.discord.simpleast.core.parser.ParseSpec
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.search.network.SearchQuery
import com.discord.utilities.search.query.FilterType
import com.discord.utilities.search.query.node.answer.AnswerNode
import com.discord.utilities.search.query.node.filter.FilterNode
import com.discord.utilities.search.validation.SearchData
import moe.lava.awoocord.scout.FilterTypeExtension
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

class DateNode(private val date: Long?, private val unparsed: String) : AnswerNode() {

    constructor(unparsed: String) : this(fmt.parse(unparsed)?.time, unparsed)

    companion object {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val regex: Pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}", Pattern.UNICODE_CASE)
        fun getDateRule(): ParserRule {
            return SimpleParserRule(regex) { matcher, _, obj ->
                val match = matcher.group()
                val date = fmt.parse(match)
                val node = DateNode(date?.time, match)
                ParseSpec(node, obj)
            }
        }

        private fun getFilterRule(str: String, type: FilterType): ParserRule {
            val regex = Pattern.compile("^\\s*?(${str}):", 64)
            return SimpleParserRule(regex) { _, _, obj ->
                ParseSpec(FilterNode(type, str), obj)
            }
        }

        fun getBeforeRule(str: String): ParserRule = getFilterRule(str, FilterTypeExtension.BEFORE)
        fun getDuringRule(str: String): ParserRule = getFilterRule(str, FilterTypeExtension.DURING)
        fun getAfterRule(str: String): ParserRule = getFilterRule(str, FilterTypeExtension.AFTER)
    }

    override fun getValidFilters(): Set<FilterType> = FilterTypeExtension.dates.toSet()
    override fun isValid(searchData: SearchData?): Boolean = date != null
    override fun getText(): CharSequence = unparsed

    private val snowflake: String?
        get() = date?.let { SnowflakeUtils.fromTimestamp(date).toString() }
    private val nextDaySnowflake: String?
        get() = date?.let { SnowflakeUtils.fromTimestamp(date + 86_400_000).toString() }

    override fun updateQuery(
        builder: SearchQuery.Builder?,
        searchData: SearchData?,
        filterType: FilterType?
    ) {
        checkNotNull(builder) { "queryBuilder" }
        checkNotNull(date) { "date" }
        when (filterType) {
            FilterTypeExtension.BEFORE -> {
                builder.appendParam("max_id", snowflake)
            }
            FilterTypeExtension.AFTER -> {
                builder.appendParam("min_id", nextDaySnowflake)
            }
            FilterTypeExtension.DURING -> {
                builder.appendParam("min_id", snowflake)
                builder.appendParam("max_id", nextDaySnowflake)
            }
            else -> return
        }
    }
}
