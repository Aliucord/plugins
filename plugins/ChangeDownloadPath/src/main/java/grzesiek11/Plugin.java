package grzesiek11.aliucordplugins.changedownloadpath;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Context;
import android.net.Uri;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.Logger;
import com.aliucord.patcher.InsteadHook;
import com.discord.utilities.io.NetworkUtils;
import java.io.File;
import kotlin.jvm.functions.Function1;

@AliucordPlugin(requiresRestart = false)
public class Plugin extends com.aliucord.entities.Plugin {
    static final String PATH_SETTING_KEY = "path";
    static final String PATH_SETTING_DEFAULT = "/storage/emulated/0/Download";

    @Override
    public void start(Context context) throws Throwable {
        var settings = new SettingsAPI("ChangeDownloadPath");
        this.settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.PAGE).withArgs(settings);

        this.patcher.patch(
            NetworkUtils.class.getDeclaredMethod("downloadFile", Context.class, Uri.class, String.class, String.class, Function1.class, Function1.class),
            new InsteadHook(param -> {
                var uri = (Uri) param.args[1];
                var filename = (String) param.args[2];
                var description = (String) param.args[3];
                var successCallback = param.args[4];
                var errorCallback = param.args[5];

                var downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                var downloadPath = settings.getString(PATH_SETTING_KEY, PATH_SETTING_DEFAULT);
                var downloadUri = Uri.fromFile(new File(downloadPath));
                var downloadRequest = new DownloadManager.Request(uri)
                    .setTitle(filename)
                    .setDescription(description)
                    .setDestinationUri(Uri.withAppendedPath(downloadUri, filename));
                downloadManager.enqueue(downloadRequest);

                return null;
            })
        );
    }

    @Override
    public void stop(Context context) {
        this.patcher.unpatchAll();
    }
}
