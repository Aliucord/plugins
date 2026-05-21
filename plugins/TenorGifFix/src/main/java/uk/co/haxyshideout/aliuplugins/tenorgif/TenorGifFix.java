package uk.co.haxyshideout.aliuplugins.tenorgif;

import android.content.Context;
import android.net.Uri;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PreHook;
import com.discord.utilities.io.NetworkUtils;

import java.util.List;

import kotlin.jvm.functions.Function1;

@AliucordPlugin
public class TenorGifFix extends Plugin {

    @Override
    public void start(Context context) throws Throwable {

        patcher.patch(
                NetworkUtils.class.getDeclaredMethod("downloadFile", Context.class, Uri.class, String.class, String.class, Function1.class, Function1.class),
                new PreHook(param -> {
                    Uri uri = (Uri) param.args[1];

                    if (!uri.getPath().contains(".tenor.com")) {
                        return;
                    }

                    List<String> pathSegments = uri.getPathSegments();
                    int startSegment = 0;
                    for (int i = 0; i < pathSegments.size(); i++) {
                        if (pathSegments.get(i).contains(".tenor.com")) {
                            startSegment = i;
                            break;
                        }
                    }

                    String hostname = pathSegments.get(startSegment);
                    String tenorID = pathSegments.get(startSegment+1);
                    String fileName = pathSegments.get(startSegment+2);
                    if (tenorID.endsWith("AC")) {
                        return;
                    }

                    tenorID = tenorID.substring(0, tenorID.length()-2) + "AC";
                    fileName = fileName.replace(".mp4", ".gif");
                    String newUri = "https://"+hostname+"/"+tenorID+"/"+fileName;
                    try {
                        uri = Uri.parse(newUri);
                    } catch (Exception e) {
                        logger.error(e);
                    }

                    param.args[1] = uri;
                })
        );

    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }


}
