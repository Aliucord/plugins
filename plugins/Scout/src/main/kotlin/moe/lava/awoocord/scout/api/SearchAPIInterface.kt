package moe.lava.awoocord.scout.api

import com.discord.models.domain.ModelSearchResponse
import i0.f0.f
import i0.f0.s
import i0.f0.t
import rx.Observable

// io.f0.f = retrofit @GET
// io.f0.s = retrofit @Path
// io.f0.t = retrofit @Query

interface SearchAPIInterface {
    @f("channels/{channelId}/messages/search")
    fun searchChannelMessages(
        @s("channelId") channelId: Long,
        @t("min_id") minId: List<String>?,
        @t("max_id") maxId: List<String>?,
        @t("author_id") authorId: List<String>?,
        @t("mentions") mentions: List<String>?,
        @t("has") has: List<String>?,
        @t("content") content: List<String>?,
        @t("attempts") attempts: Int?,
        @t("include_nsfw") includeNsfw: Boolean?,
        @t("sort_by") sortBy: List<String>?, // "timestamp" is one, not sure about any other sort types
        @t("sort_order") sortOrder: List<String>?, // "asc" or "desc"
        @t("author_type") authorType: List<String>?,
    ): Observable<ModelSearchResponse?>

    @f("guilds/{guildId}/messages/search")
    fun searchGuildMessages(
        @s("guildId") guildId: Long,
        @t("min_id") minId: List<String>?,
        @t("max_id") maxId: List<String>?,
        @t("author_id") authorId: List<String>?,
        @t("mentions") mentions: List<String>?,
        @t("channel_id") channelId: List<String>?,
        @t("has") has: List<String>?,
        @t("content") content: List<String>?,
        @t("attempts") attempts: Int?,
        @t("include_nsfw") includeNsfw: Boolean?,
        @t("sort_by") sortBy: List<String>?,
        @t("sort_order") sortOrder: List<String>?,
        @t("author_type") authorType: List<String>?,
    ): Observable<ModelSearchResponse?>
}
