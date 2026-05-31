package com.aluicord.plugins;

import android.content.Context;
import android.net.Uri;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PreHook;
import com.discord.utilities.io.NetworkUtils;

import java.util.List;

import kotlin.jvm.functions.Function1;

@AliucordPlugin
public class TwitterDownloadFix extends Plugin {

    @Override
    public void start(Context context) throws Throwable {

        patcher.patch(
                NetworkUtils.class.getDeclaredMethod("downloadFile", Context.class, Uri.class, String.class, String.class, Function1.class, Function1.class),
                new PreHook(param -> {
                    Uri uri = (Uri) param.args[1];

                    String Url = "tmp"; // I have to set up this variable before the if statements so the compiler can see them when it compiles the try/catch statement.

                    String uriStr = uri.toString();
                    // logger.verbose(uriStr);
                    
                    if (uri.getPath().contains(".twimg.com")) {
                        Url = "twt";
                    // } else if (uri.getPath().contains("cdn.bsky.app"))  {
                    //     Url = "bsky";
                    } else {
                        return;
                    }

                    try {
                        if (Url == "twt") {
                            uriStr = uriStr.replaceAll("(?i)((\\.jpg:large)|(\\.jpg%3Alarge)|(\\.jpg_large)|(\\.jpg%5Flarge))$", ".jpg");
                            uriStr = uriStr.replaceAll(".*https\\/", "https://");
                            uri = Uri.parse(uriStr);
                        // } else if (Url == "bsky") {
                            // uriStr = uriStr.replaceAll("(?i)((@)|(%40))", ".");
                            // logger.verbose(uriStr);
                            // uriStr = uriStr.replaceAll(".*https\\/", "https://");
                            // logger.verbose(uriStr);
                            // uri = Uri.parse(uriStr);
                            // Bluesky actually stores the files as @jpeg instead of .jpeg so changing the URL doesn't work, will have to figure out how to change the file type after downloading.
                        }
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
