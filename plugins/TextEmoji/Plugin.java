package grzesiek11.aliucordplugins.textemoji;

import android.content.Context;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.InsteadHook;
import com.discord.api.message.reaction.MessageReaction;
import com.discord.stores.StoreEmoji;
import com.discord.utilities.textprocessing.node.EmojiNode;
import com.discord.views.ReactionView;
import java.util.regex.Pattern;

@AliucordPlugin(requiresRestart = false)
public class Plugin extends com.aliucord.entities.Plugin {
    // This regex is not possible to match
    private static final Pattern UNMATCHABLE_PATTERN = Pattern.compile("$a");

    @Override
    public void start(Context context) throws Throwable {
        // Text emoji in message contents
        // Could also be done by patching com.discord.utilities.textprocessing.Rules$PATTERN_UNICODE_EMOJI$2.invoke
        // together with com.discord.utilities.textprocessing.Rules.replaceEmojiSurrogates,
        // but I figured this is simpler.
        patcher.patch(
            StoreEmoji.class.getDeclaredMethod("getUnicodeEmojisPattern"),
            InsteadHook.returnConstant(Plugin.UNMATCHABLE_PATTERN)
        );

        // Text emoji in reactions
        patcher.patch(
            EmojiNode.Companion.class.getDeclaredMethod("from", int.class, EmojiNode.EmojiIdAndType.class),
            new Hook(param -> {
                var emojiIdAndType = param.args[1];
                if (emojiIdAndType instanceof EmojiNode.EmojiIdAndType.Unicode) {
                    param.setResult(null);
                }
            })
        );

        // Fix opacity in text emoji reactions
        // This is a Discord bug that makes text emoji in reactions weirdly
        // transparent, it's also present without the plugin, but it's only
        // noticeable with emoji that don't have image replacements.
        patcher.patch(
            ReactionView.class.getDeclaredMethod("a", MessageReaction.class, long.class, boolean.class),
            new Hook(param -> {
                var this_ = (ReactionView) param.thisObject;
                var reactionEmojiText = this_.o.e;
                reactionEmojiText.setTextColor(0xffffffff);
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
