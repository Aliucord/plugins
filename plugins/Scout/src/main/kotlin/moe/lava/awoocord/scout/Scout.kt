@file:Suppress("EnumValuesSoftDeprecate", "CanConvertToMultiDollarString")

/**
 * Hi to anyone who might be reading this; I am sorry for the atrocious code in this plugin
 * but I promise I'll be fixing it up soon :3
 */

package moe.lava.awoocord.scout

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PreHook
import com.aliucord.patcher.after
import com.aliucord.patcher.before
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.patcher.component3
import com.aliucord.patcher.component4
import com.aliucord.patcher.component5
import com.aliucord.patcher.instead
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.utils.ViewUtils.findViewById
import com.aliucord.utils.accessField
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.discord.BuildConfig
import com.discord.api.channel.Channel
import com.discord.api.channel.ChannelUtils
import com.discord.api.channel.`ChannelUtils$getSortByNameAndType$1`
import com.discord.api.permission.Permission
import com.discord.databinding.WidgetSearchSuggestionItemHeaderBinding
import com.discord.databinding.WidgetSearchSuggestionsItemHasBinding
import com.discord.databinding.WidgetSearchSuggestionsItemSuggestionBinding
import com.discord.models.member.GuildMember
import com.discord.models.user.User
import com.discord.restapi.RequiredHeadersInterceptor
import com.discord.restapi.RestAPIBuilder
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import com.discord.stores.StoreSearch
import com.discord.stores.StoreSearchInput
import com.discord.stores.StoreStream
import com.discord.utilities.mg_recycler.MGRecyclerDataPayload
import com.discord.utilities.mg_recycler.SingleTypePayload
import com.discord.utilities.rest.RestAPI.AppHeadersProvider
import com.discord.utilities.search.network.`SearchFetcher$getRestObservable$3`
import com.discord.utilities.search.network.SearchQuery
import com.discord.utilities.search.query.FilterType
import com.discord.utilities.search.query.node.QueryNode
import com.discord.utilities.search.query.node.answer.ChannelNode
import com.discord.utilities.search.query.node.answer.HasAnswerOption
import com.discord.utilities.search.query.node.answer.HasNode
import com.discord.utilities.search.query.node.answer.UserNode
import com.discord.utilities.search.query.node.content.ContentNode
import com.discord.utilities.search.query.node.filter.FilterNode
import com.discord.utilities.search.query.parsing.QueryParser
import com.discord.utilities.search.query.parsing.`QueryParser$Companion$getInAnswerRule$1`
import com.discord.utilities.search.strings.ContextSearchStringProvider
import com.discord.utilities.search.strings.SearchStringProvider
import com.discord.utilities.search.suggestion.SearchSuggestionEngine
import com.discord.utilities.search.suggestion.entries.ChannelSuggestion
import com.discord.utilities.search.suggestion.entries.FilterSuggestion
import com.discord.utilities.search.suggestion.entries.HasSuggestion
import com.discord.utilities.search.suggestion.entries.SearchSuggestion
import com.discord.utilities.search.validation.SearchData
import com.discord.widgets.search.results.WidgetSearchResults
import com.discord.widgets.search.suggestions.WidgetSearchSuggestions
import com.discord.widgets.search.suggestions.`WidgetSearchSuggestions$configureUI$1`
import com.discord.widgets.search.suggestions.WidgetSearchSuggestionsAdapter
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.lytefast.flexinput.R
import moe.lava.awoocord.scout.api.SearchAPIInterface
import moe.lava.awoocord.scout.entries.AuthorTypeSuggestion
import moe.lava.awoocord.scout.entries.AuthorTypeViewHolder
import moe.lava.awoocord.scout.parsing.AuthorType
import moe.lava.awoocord.scout.parsing.AuthorTypeNode
import moe.lava.awoocord.scout.parsing.DateNode
import moe.lava.awoocord.scout.parsing.SimpleParserRule
import moe.lava.awoocord.scout.parsing.SortNode
import moe.lava.awoocord.scout.parsing.UserIdNode
import moe.lava.awoocord.scout.ui.DatePickerFragment
import moe.lava.awoocord.scout.ui.ScoutResource
import moe.lava.awoocord.scout.ui.ScoutSearchStringProvider
import java.util.regex.Pattern
import b.a.k.b as FormatUtils

private val WidgetSearchSuggestionsAdapter.FilterViewHolder.binding
        by accessField<WidgetSearchSuggestionsItemSuggestionBinding>()

private val WidgetSearchSuggestionsAdapter.HeaderViewHolder.binding
        by accessField<WidgetSearchSuggestionItemHeaderBinding>()

@AliucordPlugin
@Suppress("unused", "unchecked_cast")
class Scout : Plugin() {
    lateinit var scoutRes: ScoutResource
    lateinit var ssProvider: ScoutSearchStringProvider
    lateinit var searchApi: SearchAPIInterface

    var optionsExpanded = false

    init {
        @Suppress("DEPRECATION")
        needsResources = true
    }

    override fun load(context: Context) {
        scoutRes = ScoutResource(resources!!)
        ssProvider = ScoutSearchStringProvider(context)
        searchApi = buildSearchApi(context)
    }

    override fun start(context: Context) {
        extendFilterType()
        extendHasAnswerOption()
        extendSuggestionCategory()
        fixFiltersKeying()
        fixHasFilterSuggestion()
        fixSearchPadding()
        patchHasAnswerOption()
        patchHasNode()
        patchQuery()
        patchQueryParser()
        patchSearchUI(context)
        patchThreadSupport()
        patchUsernameDiscriminator()
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        resetFilterType()
        resetHasAnswerOption()
        resetSuggestionCategory()
    }

    // Creates a new custom search API implementation, for the extra `min_id` param in search queries
    private fun buildSearchApi(context: Context): SearchAPIInterface {
        val appHeadersProvider = AppHeadersProvider.INSTANCE
        val requiredHeadersInterceptor = RequiredHeadersInterceptor(appHeadersProvider)
        val persistentCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
        val restAPIBuilder = RestAPIBuilder(BuildConfig.HOST_API, persistentCookieJar)

        return RestAPIBuilder.`build$default`(
            restAPIBuilder,
            SearchAPIInterface::class.java,
            false,
            0L,
            listOf(requiredHeadersInterceptor),
            "client_base",
            false,
            null,
            102,
            null
        ) as SearchAPIInterface
    }

    private var origFilterTypes: Array<FilterType>? = null
    // Creates new pseudo-values of the `FilterType` enum for date filters
    @Suppress("LocalVariableName", "AssignedValueIsNeverRead")
    private fun extendFilterType() {
        val cls = FilterType::class.java
        val constructor = cls.declaredConstructors[0]
        constructor.isAccessible = true

        val field = cls.getDeclaredField("\$VALUES")
        field.isAccessible = true
        val values = field.get(null) as Array<FilterType>
        origFilterTypes = origFilterTypes ?: values
        var nextIdx = values.size

        val EXPAND = constructor.newInstance("EXPAND", nextIdx++) as FilterType
        val SORT = constructor.newInstance("SORT", nextIdx++) as FilterType
        val EXCLUDE = constructor.newInstance("EXCLUDE", nextIdx++) as FilterType
        val AUTHOR_TYPE = constructor.newInstance("AUTHOR_TYPE", nextIdx++) as FilterType
        val BEFORE = constructor.newInstance("BEFORE", nextIdx++) as FilterType
        val DURING = constructor.newInstance("DURING", nextIdx++) as FilterType
        val AFTER = constructor.newInstance("AFTER", nextIdx++) as FilterType
        FilterTypeExtension.EXPAND = EXPAND
        FilterTypeExtension.SORT = SORT
        FilterTypeExtension.EXCLUDE = EXCLUDE
        FilterTypeExtension.AUTHOR_TYPE = AUTHOR_TYPE
        FilterTypeExtension.BEFORE = BEFORE
        FilterTypeExtension.DURING = DURING
        FilterTypeExtension.AFTER = AFTER
        FilterTypeExtension.dates = arrayOf(BEFORE, DURING, AFTER)
        FilterTypeExtension.filters = arrayOf(SORT, AUTHOR_TYPE, EXCLUDE) + FilterTypeExtension.dates
        FilterTypeExtension.values = arrayOf(EXPAND) + FilterTypeExtension.filters

        val newValues = values.toMutableList()
        newValues.addAll(FilterTypeExtension.values)
        field.set(null, newValues.toTypedArray())
    }

    private fun resetFilterType() {
        if (origFilterTypes == null)
            return logger.error("No unpatched filter types?", null)

        val cls = FilterType::class.java
        val field = cls.getDeclaredField("\$VALUES")
        field.isAccessible = true
        field.set(null, origFilterTypes)
        origFilterTypes = null
    }

    private var origHasAnswerOptions: Array<HasAnswerOption>? = null
    // Creates new pseudo-values of the `HasAnswerOption` enum for poll and forwarded filters
    @Suppress("LocalVariableName", "AssignedValueIsNeverRead")
    private fun extendHasAnswerOption() {
        val cls = HasAnswerOption::class.java
        val constructor = cls.declaredConstructors[0]
        constructor.isAccessible = true

        val field = cls.getDeclaredField("\$VALUES")
        field.isAccessible = true
        val values = field.get(null) as Array<HasAnswerOption>
        origHasAnswerOptions = origHasAnswerOptions ?: values
        var nextIdx = values.size

        val POLL = constructor.newInstance("POLL", nextIdx++, "poll") as HasAnswerOption
        val SNAPSHOT = constructor.newInstance("SNAPSHOT", nextIdx++, "snapshot") as HasAnswerOption
        HasAnswerOptionExtension.POLL = POLL
        HasAnswerOptionExtension.SNAPSHOT = SNAPSHOT
        HasAnswerOptionExtension.values = arrayOf(POLL, SNAPSHOT)

        val newValues = values.toMutableList()
        newValues.addAll(HasAnswerOptionExtension.values)
        field.set(null, newValues.toTypedArray())
    }

    private fun resetHasAnswerOption() {
        if (origHasAnswerOptions == null)
            return logger.error("No unpatched 'has' options?", null)

        val cls = HasAnswerOption::class.java
        val field = cls.getDeclaredField("\$VALUES")
        field.isAccessible = true
        field.set(null, origHasAnswerOptions)
        origHasAnswerOptions = null
    }

    private var origSuggestionCategories: Array<SearchSuggestion.Category>? = null
    // Creates new pseudo-values of the suggestion categories to add correct headers
    @Suppress("LocalVariableName", "AssignedValueIsNeverRead")
    private fun extendSuggestionCategory() {
        val cls = SearchSuggestion.Category::class.java
        val constructor = cls.declaredConstructors[0]
        constructor.isAccessible = true

        val field = cls.getDeclaredField("\$VALUES")
        field.isAccessible = true
        val values = field.get(null) as Array<SearchSuggestion.Category>
        origSuggestionCategories = origSuggestionCategories ?: values
        var nextIdx = values.size

        val AUTHOR_TYPE = constructor.newInstance("AUTHOR_TYPE", nextIdx++) as SearchSuggestion.Category
        SuggestionCategoryExtension.AUTHOR_TYPE = AUTHOR_TYPE
        SuggestionCategoryExtension.values = arrayOf(AUTHOR_TYPE)

        val newValues = values.toMutableList()
        newValues.addAll(SuggestionCategoryExtension.values)
        field.set(null, newValues.toTypedArray())
    }

    private fun resetSuggestionCategory() {
        if (origSuggestionCategories == null)
            return logger.error("No unpatched suggestion categories?", null)

        val cls = SearchSuggestion.Category::class.java
        val field = cls.getDeclaredField("\$VALUES")
        field.isAccessible = true
        field.set(null, origSuggestionCategories)
        origSuggestionCategories = null
    }

    // Patch to key filters properly for smoother recycling
    // Thank u discord for keying every filter type the same thing!! /s
    private fun fixFiltersKeying() {
        patcher.instead<WidgetSearchSuggestionsAdapter.Companion>(
            "getFilterItem",
            FilterSuggestion::class.java,
        ) { (_, suggestion: FilterSuggestion) ->
            SingleTypePayload(suggestion, suggestion.filterType.name, 2) // 2 = WidgetSearchSuggestionsAdapter.TYPE_FILTER
        }
    }

    // YES DISCORD TYPO'ED THIS HAHAHAHAHAHAFAUHFAIUFHAIFBHUKFHYRISFSUOIRN
    private fun fixHasFilterSuggestion() {
        patcher.before<FilterSuggestion.Companion>(
            "getStringRepresentation",
            FilterType::class.java,
            SearchStringProvider::class.java,
        ) { (param, filter: FilterType, provider: SearchStringProvider) ->
            if (filter == FilterType.HAS) {
                param.result = provider.hasFilterString + ":"
            }
        }
    }

    // Patch out the gigantic padding in search results
    private fun fixSearchPadding() {
        patcher.after<WidgetSearchResults>("onViewBound", View::class.java) {
            view?.run {
                fitsSystemWindows = false
                setPadding(paddingLeft, 16.dp, paddingRight, paddingBottom)
            }
        }

        patcher.after<WidgetSearchSuggestions>("onViewBound", View::class.java) {
            // Being a bit sneaky and reset the expanded flag here
            optionsExpanded = false
            view?.run {
                fitsSystemWindows = false
                setPadding(paddingLeft, 16.dp, paddingRight, paddingBottom)
            }
        }
    }

    // Patches various methods that use HasAnswerOption to include our new options
    private fun patchHasAnswerOption() {
        patcher.before<HasAnswerOption.Companion>(
            "getOptionFromString",
            String::class.java,
            SearchStringProvider::class.java
        ) { param ->
            val str = param.args[0] as String
            if (str == ssProvider.hasPollString)
                param.result = HasAnswerOptionExtension.POLL
            else if (str == ssProvider.hasForwardString)
                param.result = HasAnswerOptionExtension.SNAPSHOT
        }

        patcher.before<HasAnswerOption>(
            "getLocalizedInputText",
            SearchStringProvider::class.java
        ) { param ->
            if (this == HasAnswerOptionExtension.POLL)
                param.result = ssProvider.hasPollString
            else if (this == HasAnswerOptionExtension.SNAPSHOT)
                param.result = ssProvider.hasForwardString
        }

        patcher.instead<QueryParser.Companion>(
            "createHasAnswerRegex",
            SearchStringProvider::class.java
        ) { param ->
            val ossProvider = param.args[0] as SearchStringProvider

            val matches = HasAnswerOption.values().joinToString("|") { it.getLocalizedInputText(ossProvider) }
            "^\\s*($matches)"
        }

        // Patch to set icons
        patcher.before<WidgetSearchSuggestionsAdapter.HasViewHolder>(
            "onConfigure",
            Int::class.java,
            MGRecyclerDataPayload::class.java,
        ) { param ->
            val suggestion = (param.args[1] as SingleTypePayload<HasSuggestion>).data
            val option = suggestion.hasAnswerOption

            val resID = when (option) {
                HasAnswerOptionExtension.POLL -> "baseline_poll_24"
                HasAnswerOptionExtension.SNAPSHOT -> "baseline_forward_to_inbox_24"
                else -> null
            }

            resID?.let {
                val bindingField = this::class.java.getDeclaredField("binding")
                bindingField.isAccessible = true
                val binding = bindingField.get(this) as WidgetSearchSuggestionsItemHasBinding

                binding.d.text = option.getLocalizedInputText(null)
                binding.b.setOnClickListener {
                    WidgetSearchSuggestionsAdapter.HasViewHolder.`access$getAdapter$p`(this).onHasClicked.invoke(option)
                }

                binding.c.setImageDrawable(scoutRes.getDrawable(it))

                param.result = null
            }
        }

        patcher.instead<SearchSuggestionEngine>(
            "getHasSuggestions",
            CharSequence::class.java,
            FilterType::class.java,
            SearchStringProvider::class.java,
        ) { (_, query: CharSequence, type: FilterType, provider: SearchStringProvider) ->
            // Generate entries for author type
            if (type == FilterTypeExtension.AUTHOR_TYPE) {
                return@instead AuthorType.values()
                    .filter { it.value.contains(query) }
                    .map { AuthorTypeSuggestion(it) }
            }

            // Generate entries for has options, including new ones
            if (type == FilterType.HAS || type == FilterTypeExtension.EXCLUDE)
                return@instead HasAnswerOption.values()
                    .filter { it.getLocalizedInputText(provider).contains(query) }
                    .map { HasSuggestion(it) }

            listOf<Any>()
        }
    }

    // Patching HasNode related methods for our exclude: filter type
    private fun patchHasNode() {
        patcher.instead<HasNode>("getValidFilters") {
            setOf(FilterTypeExtension.EXCLUDE, FilterType.HAS)
        }

        // Patch updateQuery to either include or exclude our has option
        patcher.instead<HasNode>(
            "updateQuery",
            SearchQuery.Builder::class.java,
            SearchData::class.java,
            FilterType::class.java,
        ) { param ->
            val builder = param.args[0] as SearchQuery.Builder?
            val filterType = param.args[2] as FilterType

            checkNotNull(builder) { "queryBuilder" }

            val field = HasNode::class.java.getDeclaredField("hasAnswerOption")
            field.isAccessible = true
            val opt = field.get(this) as HasAnswerOption

            if (filterType == FilterType.HAS)
                builder.appendParam("has", opt.restParamValue)
            else if (filterType == FilterTypeExtension.EXCLUDE)
                builder.appendParam("has", "-" + opt.restParamValue)
        }

        // Patching the behaviour when the has suggestion is clicked
        patcher.before<StoreSearchInput>(
            "onHasClicked",
            HasAnswerOption::class.java,
            CharSequence::class.java,
            CharSequence::class.java,
            List::class.java,
        ) { param ->
            val opt = param.args[0] as HasAnswerOption
            val hasFilterText = param.args[1] as CharSequence
            val filterAnswer = param.args[2] as CharSequence
            val query = param.args[3] as List<QueryNode>

            val replaceAndPublish = StoreSearchInput::class.java.getDeclaredMethod(
                "replaceAndPublish",
                Int::class.javaPrimitiveType!!,
                List::class.java,
                List::class.java
            )
            replaceAndPublish.isAccessible = true

            val getAnswerReplacementStart = StoreSearchInput::class.java.getDeclaredMethod(
                "getAnswerReplacementStart",
                List::class.java,
            )
            getAnswerReplacementStart.isAccessible = true

            val replacementIdx = getAnswerReplacementStart.invoke(this, query) as Int
            val previousFilterText = query[replacementIdx]
            val filterNode = if (previousFilterText.text == ssProvider.excludeFilterString)
                FilterNode(FilterTypeExtension.EXCLUDE, ssProvider.excludeFilterString)
            else
                FilterNode(FilterType.HAS, hasFilterText)

            replaceAndPublish.invoke(this, replacementIdx, listOf(filterNode, HasNode(opt, filterAnswer)), query)
        }
    }

    // Patches the search query to also insert `min_id`, required for searching "after:" and "during:"
    private fun patchQuery() {
        patcher.patch(
            `SearchFetcher$getRestObservable$3`::class.java.getDeclaredMethod("call", Integer::class.java),
            PreHook { param ->
                val self = param.thisObject as `SearchFetcher$getRestObservable$3`<*, *>
                val retryAttempts = param.args[0] as Int?
                val params = self.`$searchQuery`.params

                var minID = params["min_id"]
                var maxID = params["max_id"]
                val sortOrder = params["sort_order"]
                val authorType = params["author_type"]
                self.`$oldestMessageId`?.let {
                    if (sortOrder?.getOrNull(0) == "asc")
                        minID = listOf(it.toString())
                    else
                        maxID = listOf(it.toString())
                }

                param.result = if (self.`$searchTarget`.type == StoreSearch.SearchTarget.Type.GUILD)
                    searchApi.searchGuildMessages(
                        self.`$searchTarget`.id,
                        minID,
                        maxID,
                        params["author_id"],
                        params["mentions"],
                        params["channel_id"],
                        params["has"],
                        params["content"],
                        retryAttempts,
                        self.`$searchQuery`.includeNsfw,
                        listOf("timestamp"),
                        sortOrder,
                        authorType,
                    )
                else
                    searchApi.searchChannelMessages(
                        self.`$searchTarget`.id,
                        minID,
                        maxID,
                        params["author_id"],
                        params["mentions"],
                        params["has"],
                        params["content"],
                        retryAttempts,
                        self.`$searchQuery`.includeNsfw,
                        listOf("timestamp"),
                        sortOrder,
                        authorType,
                    )
            }
        )
    }

    // Patch parser for date parsing
    private fun patchQueryParser() {
        patcher.after<QueryParser>(SearchStringProvider::class.java) {
            // We need to access and insert into the rules before the rest
            val field = Parser::class.java.getDeclaredField("rules").apply { isAccessible = true }
            val rules = field.get(this) as ArrayList<Rule<Context, QueryNode, Any>>
            rules.addAll(0, listOf(
                UserIdNode.getUserIdRule(),
                DateNode.getBeforeRule(ssProvider.beforeFilterString),
                DateNode.getDuringRule(ssProvider.duringFilterString),
                DateNode.getAfterRule(ssProvider.afterFilterString),
                DateNode.getDateRule(),
                SortNode.getFilterRule(ssProvider.sortFilterString),
                SortNode.getSortRule(ssProvider),
                AuthorTypeNode.getFilterRule(ssProvider.authorTypeFilter),
                AuthorTypeNode.getAuthorTypesRule(),
                SimpleParserRule(Pattern.compile("^\\s*?${ssProvider.excludeFilterString}:", 64)) { _, _, obj ->
                    ParseSpec(FilterNode(FilterTypeExtension.EXCLUDE, ssProvider.excludeFilterString), obj)
                }
            ))
        }
    }

    // This is probably the worst bit of this plugin
    @SuppressLint("SetTextI18n")
    private fun patchSearchUI(context: Context) {
        // Run when a filter suggestion is clicked
        // Most of the code is copied from its implementation
        // Patch needed to support the new filter types
        patcher.before<StoreSearchInput>(
            "onFilterClicked",
            FilterType::class.java,
            SearchStringProvider::class.java,
            List::class.java,
        ) { param ->
            val filter = param.args[0] as FilterType
            if (filter !in FilterTypeExtension.values)
                return@before // Exit if not an extended filter type

            val replaceAndPublish = StoreSearchInput::class.java.getDeclaredMethod(
                "replaceAndPublish",
                Int::class.javaPrimitiveType!!,
                List::class.java,
                List::class.java
            )
            replaceAndPublish.isAccessible = true

            val getAnswerReplacementStart = StoreSearchInput::class.java.getDeclaredMethod(
                "getAnswerReplacementStart",
                List::class.java,
            )
            getAnswerReplacementStart.isAccessible = true

            // Original implementation
            val filterNode = FilterNode(filter, ssProvider.stringFor(filter))
            val list = (param.args[2] as List<QueryNode>).toMutableList()
            val lastIndex = if (list.isEmpty()) {
                0
            } else if (list.last() is ContentNode)
                list.lastIndex
            else
                list.size

            // Open a Date Picker
            if (filter in FilterTypeExtension.dates) {
                replaceAndPublish.invoke(this, lastIndex, listOf(filterNode), list)
                DatePickerFragment.open(Utils.appActivity.supportFragmentManager) {
                    replaceAndPublish.invoke(this,
                        getAnswerReplacementStart.invoke(this, list),
                        listOf(filterNode, DateNode(it)),
                        list
                    )
                }
            }

            if (filter == FilterTypeExtension.SORT)
                replaceAndPublish.invoke(this,
                    lastIndex,
                    listOf(filterNode, SortNode(ssProvider.sortOldString)),
                    list
                )

            if (filter == FilterTypeExtension.EXCLUDE)
                replaceAndPublish.invoke(this,
                    lastIndex,
                    listOf(filterNode),
                    list
                )

            if (filter == FilterTypeExtension.AUTHOR_TYPE)
                replaceAndPublish.invoke(this,
                    lastIndex,
                    listOf(filterNode),
                    list
                )

            param.result = null
        }

        // Patch to set icons
        @Suppress("ResourceType")
        patcher.before<WidgetSearchSuggestionsAdapter.FilterViewHolder>(
            "getIconDrawable",
            Context::class.java,
            FilterType::class.java
        ) { param ->
            val type = param.args[1] as FilterType
            val (isDiscord, resID) = when (type) {
                FilterTypeExtension.BEFORE -> true to R.e.ic_history_white_24dp
                FilterTypeExtension.DURING -> false to scoutRes.getDrawableId("baseline_clock_24")
                FilterTypeExtension.AFTER -> false to scoutRes.getDrawableId("baseline_update_24")
                FilterTypeExtension.SORT -> true to R.e.ic_sort_white_24dp
                FilterTypeExtension.EXCLUDE -> false to scoutRes.getDrawableId("baseline_do_disturb_on_24")
                FilterTypeExtension.AUTHOR_TYPE -> true to R.e.ic_members_24dp
                else -> false to null
            }

            resID?.let {
                val res = if (isDiscord) context.resources else resources!!
                param.result = ResourcesCompat.getDrawable(res, it, null)
            }
        }

        // Patch for retrieving sample filter answer/placeholder
        patcher.before<WidgetSearchSuggestionsAdapter.FilterViewHolder>(
            "getAnswerText",
            FilterType::class.java
        ) { param ->
            val type = param.args[0] as FilterType
            if (type in FilterTypeExtension.dates)
                param.result = ssProvider.getIdentifier("search_answer_date")
            if (type == FilterTypeExtension.SORT)
                param.result = ScoutResource.SORT_ANSWER
            if (type == FilterTypeExtension.EXCLUDE)
                param.result = ssProvider.getIdentifier("search_answer_has")
            if (type == FilterTypeExtension.AUTHOR_TYPE)
                param.result = ScoutResource.AUTHOR_TYPE_ANSWER
        }

        // Patch for retrieving filter name
        patcher.before<WidgetSearchSuggestionsAdapter.FilterViewHolder>(
            "getFilterText",
            FilterType::class.java
        ) { param ->
            val type = param.args[0] as FilterType
            val res = when (type) {
                FilterTypeExtension.EXCLUDE -> ScoutResource.EXCLUDE_FILTER
                FilterTypeExtension.BEFORE -> ssProvider.getIdentifier("search_filter_before")
                FilterTypeExtension.DURING -> ssProvider.getIdentifier("search_filter_during")
                FilterTypeExtension.AFTER -> ssProvider.getIdentifier("search_filter_after")
                FilterTypeExtension.SORT -> ScoutResource.SORT_FILTER
                FilterTypeExtension.AUTHOR_TYPE -> ScoutResource.AUTHOR_TYPE_FILTER
                else -> null
            }
            res?.let { param.result = it }
        }

        // Patch formatting utils to use our custom lowercase strings
        // This is called by FilterViewHolder.onConfigure, using the results from getAnswerText and getFilterText
        patcher.patch(
            FormatUtils::class.java.getDeclaredMethod(
                "c",
                Resources::class.java,
                Int::class.javaPrimitiveType!!,
                Array::class.java,
                Function1::class.java
            ),
            PreHook { param ->
                val resID = param.args[1] as Int
                val objArr = param.args[2] as Array<*>
                val override = when (resID) {
                    ScoutResource.SORT_FILTER -> ssProvider.sortFilterString
                    ScoutResource.SORT_ANSWER -> ssProvider.sortOldString
                    ScoutResource.EXCLUDE_FILTER -> ssProvider.excludeFilterString
                    ScoutResource.AUTHOR_TYPE_FILTER -> ssProvider.authorTypeFilter
                    ScoutResource.AUTHOR_TYPE_ANSWER -> ssProvider.authorTypeAnswer
                    else -> null
                }
                override?.let {
                    param.result = FormatUtils.g(it, objArr.copyOf(), param.args[3] as b.a.k.`b$b`)
                }
            }
        )

        // Patch to manually configure expander, need to do this to update the suggestions widget
        patcher.before<WidgetSearchSuggestionsAdapter.FilterViewHolder>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            MGRecyclerDataPayload::class.java,
        ) { (param, _: Int, payload: SingleTypePayload<FilterSuggestion>) ->
            val suggestion = payload.data
            if (suggestion.filterType != FilterTypeExtension.EXPAND) {
                return@before
            }
            param.result = null

            val sampleText = binding.b
            val layout = binding.c
            val filterText = binding.d
            val icon = binding.e
            layout.setOnClickListener {
                val onFilter = adapter.onFilterClicked as `WidgetSearchSuggestions$configureUI$1`
                val widget = onFilter.`this$0`
                optionsExpanded = true
                WidgetSearchSuggestions.Model.Companion!!.get(ContextSearchStringProvider(context)).z().subscribe {
                    WidgetSearchSuggestions.`access$configureUI`(widget, this)
                }
            }
            sampleText.text = null
            filterText.text = ssProvider.expandFilterString
            val drawable = R.e.ic_chevron_right_primary_300_12dp
            icon.setImageDrawable(ResourcesCompat.getDrawable(context.resources, drawable, null))
        }

        // Patch to add our new filters into the initial suggestions
        patcher.after<SearchSuggestionEngine>(
            "getFilterSuggestions",
            CharSequence::class.java,
            SearchStringProvider::class.java,
            Boolean::class.javaPrimitiveType!!,
        ) { (param, query: CharSequence) ->
            val res = (param.result as List<SearchSuggestion>).toMutableList()

            if (optionsExpanded || query != "") {
                for (type in FilterTypeExtension.filters) {
                    val st = ssProvider.stringFor(type) + ":"

                    if (st.contains(query))
                        res.add(FilterSuggestion(type))
                }
            } else {
                res.add(FilterSuggestion(FilterTypeExtension.EXPAND))
            }
            param.result = res.toList()
        }

        // Patch to add header for new categories
        patcher.before<WidgetSearchSuggestionsAdapter.HeaderViewHolder>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            MGRecyclerDataPayload::class.java,
        ) { (param, _: Int, payload: SingleTypePayload<SearchSuggestion.Category>) ->
            val category = payload.data
            if (category == SuggestionCategoryExtension.AUTHOR_TYPE) {
                binding.b.text = "Author Type"
                param.result = null
            }
        }

        // Patch to add entries depending on category
        patcher.after<WidgetSearchSuggestions.Model>(
            List::class.java,
            List::class.java,
        ) { (_, _: List<QueryNode>, suggestions: List<SearchSuggestion>) ->
            var lastCategory: SearchSuggestion.Category? = null
            val newItems = mutableListOf<MGRecyclerDataPayload>()
            suggestions.forEach {
                if (it is AuthorTypeSuggestion) {
                    if (lastCategory != it.category) {
                        newItems.add(
                            SingleTypePayload(it.category, it.category.name, 0)
                        )
                        lastCategory = it.category
                    }
                    newItems.add(
                        SingleTypePayload(it, it.type.value, SuggestionCategoryExtension.AdapterType.AUTHOR_TYPE)
                    )
                }
            }
            suggestionItems.removeAll { it in newItems }
            suggestionItems.addAll(0, newItems)
        }

        // Patch to add new types of suggestion entries
        patcher.before<WidgetSearchSuggestionsAdapter>(
            "onCreateViewHolder",
            ViewGroup::class.java,
            Int::class.javaPrimitiveType!!,
        ) { (param, _: ViewGroup, id: Int) ->
            when (id) {
                SuggestionCategoryExtension.AdapterType.AUTHOR_TYPE -> {
                    param.result = AuthorTypeViewHolder(this, scoutRes)
                }
            }
        }
    }

    // Adds support for searching in threads
    private fun patchThreadSupport() {
        // Patch query parser for in: to support names with spaces, by wrapping them in quotes
        // This enables searching for threads which can have spaces in their names
        patcher.instead<QueryParser.Companion>("getInAnswerRule") {
            val compile = Pattern.compile("^\\s*#(\".*?\"|[^ ]+)", 64)
            `QueryParser$Companion$getInAnswerRule$1`(compile, compile)
        }

        // Patch Search data model builder to also add in threads
        patcher.before<SearchData.Builder>(
            "buildForGuild",
            Map::class.java,
            Map::class.java,
            Map::class.java,
            Map::class.java
        ) { (
                param,
                /* members */ _: Map<Long, GuildMember>,
                /* users*/ _: Map<Long, User>,
                channels: Map<Long, Channel>,
                permissions: Map<Long, Long>
            ) ->
            val threads = StoreStream.getChannels().`getThreadsForGuildInternal$app_productionGoogleRelease`(
                StoreStream.getGuildSelected().selectedGuildId
            )
            val mergedChannels = channels.toMutableMap()
            val mergedPermissions = permissions.toMutableMap()
            for (thread in threads) {
                mergedChannels[thread.id] = thread
                mergedPermissions[thread.id] = Permission.VIEW_CHANNEL
            }
            param.args[2] = mergedChannels
            param.args[3] = mergedPermissions
        }

        // Post-process the name-id map to wrap the names in quotes if they have spaces
        patcher.after<SearchData.Builder>(
            "buildForGuild",
            Map::class.java,
            Map::class.java,
            Map::class.java,
            Map::class.java
        ) { param ->
            val res = param.result as SearchData
            val nameMap = res.channelNameIndex as HashMap<String, Long>
            nameMap
                .filter { (name) -> name.contains(" ") }
                .forEach { (name, value) ->
                    val wrapped = "\"${name}\""
                    nameMap.remove(name)
                    nameMap[wrapped] = value
                }
        }

        // Patch the channel node to automatically insert quotes for names with spaces
        patcher.before<ChannelNode>(String::class.java) { (param, name: String) ->
            if (name.contains(" ") && !name.startsWith("\""))
                param.args[0] = "\"${name}\""
        }

        // Patch the search sorter to place threads last
        patcher.before<`ChannelUtils$getSortByNameAndType$1`<*>>(
            "compare",
            Object::class.java, // ?? :sob:
            Object::class.java,
        ) { (param, ch1: Channel?, ch2: Channel?) ->
            if (ch1 == null || ch2 == null) return@before

            // ChannelUtils.H <=> ChannelUtils.isThread
            if (ChannelUtils.H(ch1) && !ChannelUtils.H(ch2)) {
                param.result = 1
            }
            if (!ChannelUtils.H(ch1) && ChannelUtils.H(ch2)) {
                param.result = -1
            }
        }

        // Patch search suggestions to set icon to thread icon if it is a thread
        patcher.after<WidgetSearchSuggestionsAdapter.InChannelViewHolder>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            MGRecyclerDataPayload::class.java
        ) { (_, _: Int, payload: SingleTypePayload<ChannelSuggestion>) ->
            StoreStream.getChannels().getChannel(payload.data.channelId)?.let {
                if (ChannelUtils.H(it)) {
                    itemView.findViewById<ImageView>("search_suggestions_item_channel_icon")
                        .setImageDrawable(scoutRes.getDrawable("ic_thread_actually_white_24dp"))
                }
            }
        }
    }

    // Removes the #0000 discriminator from usernames when searching
    private fun patchUsernameDiscriminator() {
        // Change the regex for the user rule
        // Previously it matches something like <username>#<discrim>
        // Now it matches something like @<username>[#<discrim>] (bots still have discriminators)
        // The @ is required unfortunately, to distinguish it from literally any other word
        patcher.instead<QueryParser.Companion>("getUserRule") {
            val regex = Pattern.compile("^\\s*@(?:([^@#:]+)#([0-9]{4})|([a-z0-9._]{2,32}))", 64)

            // Returns a new rule to support our optional second group (discriminator)
            return@instead SimpleParserRule(regex) { matcher, _, obj ->
                val username = matcher.group(3) ?: matcher.group(1)!!
                val discrim = matcher.group(2)?.toInt() ?: 0
                ParseSpec(UserNode(username, discrim), obj)
            }
        }

        // Patches the node's string representation to add an @ and remove empty discriminators
        patcher.after<UserNode>("getText") { param ->
            param.result = "@" + (param.result as String).replace("#0000", "")
        }
    }
}
