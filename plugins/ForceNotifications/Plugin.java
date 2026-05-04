package grzesiek11.aliucordplugins.forcenotifications;

import android.content.Context;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.patcher.Hook;
import com.discord.utilities.fcm.NotificationRenderer;
import com.discord.utilities.fcm.NotificationClient;
import com.discord.utilities.fcm.NotificationData;

@AliucordPlugin(requiresRestart = false)
public class Plugin extends com.aliucord.entities.Plugin {
    @Override
    public void start(Context context) throws Throwable {
        this.patcher.patch(
            NotificationRenderer.class.getDeclaredMethod("displayInApp", Context.class, NotificationData.class),
            new Hook(param -> {
                var this_ = (NotificationRenderer) param.thisObject;
                var notificationData = (NotificationData) param.args[1];

                this_.display(context, notificationData, NotificationClient.INSTANCE.getSettings$app_productionGoogleRelease());
            })
        );
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
