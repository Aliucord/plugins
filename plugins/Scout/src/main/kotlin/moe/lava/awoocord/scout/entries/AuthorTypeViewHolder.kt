package moe.lava.awoocord.scout.entries

import android.widget.ImageView
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.utils.ViewUtils.findViewById
import com.discord.stores.StoreSearchInput
import com.discord.stores.StoreStream
import com.discord.utilities.mg_recycler.MGRecyclerDataPayload
import com.discord.utilities.mg_recycler.MGRecyclerViewHolder
import com.discord.utilities.mg_recycler.SingleTypePayload
import com.discord.utilities.search.query.node.filter.FilterNode
import com.discord.widgets.search.suggestions.`WidgetSearchSuggestions$configureUI$4`
import com.discord.widgets.search.suggestions.WidgetSearchSuggestionsAdapter
import com.lytefast.flexinput.R
import moe.lava.awoocord.scout.FilterTypeExtension
import moe.lava.awoocord.scout.parsing.AuthorType
import moe.lava.awoocord.scout.parsing.AuthorTypeNode
import moe.lava.awoocord.scout.ui.ScoutResource

private val replaceAndPublish = StoreSearchInput::class.java.getDeclaredMethod(
    "replaceAndPublish",
    Int::class.javaPrimitiveType!!,
    List::class.java,
    List::class.java
).apply { isAccessible = true }

private val getAnswerReplacementStart = StoreSearchInput::class.java.getDeclaredMethod(
    "getAnswerReplacementStart",
    List::class.java,
).apply { isAccessible = true }

class AuthorTypeViewHolder(
    adapter: WidgetSearchSuggestionsAdapter,
    // This should be fine (?)
    private val scoutRes: ScoutResource,
) : MGRecyclerViewHolder<WidgetSearchSuggestionsAdapter, MGRecyclerDataPayload>(
    Utils.getResId("widget_search_suggestions_item_has", "layout"),
    adapter,
) {
    private val imageView = itemView.findViewById<ImageView>("search_suggestions_item_has_icon")
    private val textView = itemView.findViewById<TextView>("search_suggestions_item_has_text")

    override fun onConfigure(i: Int, oPayload: MGRecyclerDataPayload) {
        super.onConfigure(i, oPayload)

        @Suppress("UNCHECKED_CAST")
        val payload = oPayload as SingleTypePayload<AuthorTypeSuggestion>
        val type = payload.data.type
        textView.text = when (type) {
            AuthorType.Bot -> "bot"
            AuthorType.User -> "user"
            AuthorType.Webhook -> "webhook"
        }
        when (type) {
            AuthorType.Bot -> imageView.setImageDrawable(scoutRes.getDrawable("smart_toy_24px"))
            AuthorType.User -> imageView.setImageResource(R.e.ic_members_24dp)
            AuthorType.Webhook -> imageView.setImageDrawable(scoutRes.getDrawable("webhook_24px"))
        }

        itemView.setOnClickListener {
            val hasHandler = adapter.onHasClicked as `WidgetSearchSuggestions$configureUI$4`
            val query = hasHandler.`$model`.query

            val storeInput = StoreStream.getSearch().storeSearchInput
            replaceAndPublish.invoke(
                storeInput,
                getAnswerReplacementStart.invoke(storeInput, query) as Int,
                listOf(
                    FilterNode(FilterTypeExtension.AUTHOR_TYPE, "authorType"),
                    AuthorTypeNode(type)
                ),
                query,
            )
        }
    }
}
