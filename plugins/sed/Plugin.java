package grzesiek11.aliucordplugins.sed;

import android.content.Context;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.ReflectUtils;
import com.discord.stores.StoreMessagesHolder;
import com.discord.stores.StoreStream;
import com.discord.widgets.chat.input.models.ApplicationCommandData;
import com.discord.widgets.chat.input.WidgetChatInput$configureSendListeners$2;
import java.util.List;
import kotlin.jvm.functions.Function1;

@AliucordPlugin(requiresRestart = false)
public class Plugin extends com.aliucord.entities.Plugin {
    static final String ADVANCED_MODE_SETTING_KEY = "advanced_mode";
    static final boolean ADVANCED_MODE_SETTING_DEFAULT = false;

    @Override
    public void start(Context context) throws Throwable {
        var settings = new SettingsAPI("Sed");
        this.settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.PAGE).withArgs(settings);

        var storeMessages = StoreStream.getMessages();
        var storeMessagesHolder = (StoreMessagesHolder) ReflectUtils.getField(storeMessages, "holder");
        var storeChannelsSelected = StoreStream.getChannelsSelected();
        var storeUsers = StoreStream.getUsers();

        patcher.patch(
            WidgetChatInput$configureSendListeners$2.class.getDeclaredMethod("invoke", List.class, ApplicationCommandData.class, Function1.class),
            new PreHook(param -> {
                var this_ = (WidgetChatInput$configureSendListeners$2) param.thisObject;
                var input = this_.$chatInput.getText();
                var command = Command.parse(input, settings.getBool(Plugin.ADVANCED_MODE_SETTING_KEY, Plugin.ADVANCED_MODE_SETTING_DEFAULT));
                if (!command.isPresent()) {
                    return;
                }
                var validationResultCallback = (Function1) param.args[2];
                validationResultCallback.invoke(Boolean.TRUE);

                var selectedChannelId = storeChannelsSelected.getSelectedChannel().k();
                var currentUserId = storeUsers.getMe().getId();
                var optionalMessage = storeMessagesHolder
                    .getMessagesForChannel(selectedChannelId)
                    .descendingMap()
                    .values()
                    .stream()
                    .filter(m -> m.getAuthor().getId() == currentUserId)
                    .findFirst();
                if (!optionalMessage.isPresent()) {
                    param.setResult(null);
                    return;
                }

                var message = optionalMessage.get();
                var content = message.getContent();
                var newContent = command.get().replace(content);
                if (!newContent.isEmpty()) {
                    storeMessages.editMessage(message.getId(), selectedChannelId, newContent, message.getAllowedMentions());
                } else {
                    storeMessages.deleteMessage(storeMessages.getMessage(selectedChannelId, message.getId()));
                }

                param.setResult(null);
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
