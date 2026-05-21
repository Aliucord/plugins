package uk.co.haxyshideout.aliuplugins.noupsidedown;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.PreHook;
import com.discord.app.AppActivity;

@AliucordPlugin(requiresRestart = true)
public class NoUpsideDown extends Plugin {

    @Override
    public void start(Context context) throws Throwable {
        patcher.patch(
                AppActivity.class.getDeclaredMethod("onCreate", Bundle.class),
                new Hook(param -> {
                    AppActivity activity = (AppActivity) param.thisObject;
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                })
        );

    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

}
