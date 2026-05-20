package io.gh.reisxd.aliuplugins;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Http;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.utils.DimenUtils;
import com.discord.databinding.WidgetIncomingShareBinding;
import com.discord.utilities.SnowflakeUtils;
import com.discord.utilities.captcha.CaptchaHelper;
import com.discord.utilities.intent.IntentUtils;
import com.discord.utilities.time.Clock;
import com.discord.widgets.chat.list.ViewEmbedGameInvite;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.discord.widgets.share.WidgetIncomingShare;
import com.discord.widgets.user.search.WidgetGlobalSearchModel;
import com.google.android.material.appbar.AppBarLayout;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import io.gh.reisxd.aliuplugins.MirroredDrawable;

@AliucordPlugin(requiresRestart = true)

public class ForwardMessagesPatch extends Plugin {
    @Override
    public void start(Context context) throws Throwable {
        var forwardId = View.generateViewId();

        Drawable replyIcon = ContextCompat.getDrawable(Utils.appActivity, com.lytefast.flexinput.R.e.ic_reply_24dp).mutate();
        replyIcon.setAutoMirrored(true);
        Utils.tintToTheme(replyIcon);
        
        MirroredDrawable forwardIcon = new MirroredDrawable(replyIcon);

        Method bindingReflection = WidgetIncomingShare.class.getDeclaredMethod("getBinding");
        bindingReflection.setAccessible(true);
        Field modelCommentField = WidgetIncomingShare.Model.class.getDeclaredField("comment");
        modelCommentField.setAccessible(true);

        // Add "Forward" action to Action menu
        patcher.patch(WidgetChatListActions.class.getDeclaredMethod("configureUI", WidgetChatListActions.Model.class),
                new PreHook(param -> {
                    var actions = (WidgetChatListActions) param.thisObject;
                    var scrollView = (NestedScrollView) actions.getView();
                    var lay = (LinearLayout) scrollView.getChildAt(0);

                    if (lay.findViewById(forwardId) == null) {
                        TextView tw = new TextView(lay.getContext(), null, 0,
                                com.lytefast.flexinput.R.i.UiKit_Settings_Item_Icon);
                        tw.setId(forwardId);
                        tw.setText("Forward");
                        tw.setCompoundDrawablesRelativeWithIntrinsicBounds(forwardIcon, null, null, null);
                        int childrenCount = lay.getChildCount();
                        boolean foundIndex = false;
                        for (int i = 0; i < childrenCount; i++) {
                            View view = lay.getChildAt(i);
                            if (view.getId() == Utils.getResId("dialog_chat_actions_reply", "id")) {
                                foundIndex = true;
                                lay.addView(tw, i + 1);
                                break;
                            }
                        }
                        if (!foundIndex) lay.addView(tw, 5);
                        tw.setOnClickListener((v) -> {
                            WidgetChatListActions.Model model = (WidgetChatListActions.Model) param.args[0];
                            long messageId = model.getMessage().getId();
                            String messageContent = model.getMessage().getContent();
                            long channelId = model.getChannel().k();

                            Intent putExtra = new Intent()
                                    .putExtra("io.gh.reisxd.aliuplugins.MESSAGE_CONTENT", messageContent)
                                    .putExtra("io.gh.reisxd.aliuplugins.MESSAGE_ID", messageId)
                                    .putExtra("io.gh.reisxd.aliuplugins.CHANNEL_ID", channelId);
                            Utils.mainThread.post(() -> {
                                Utils.openPage(Utils.getAppActivity(), WidgetIncomingShare.class, putExtra);
                                actions.dismiss();
                            });
                        });
                    }
                }));

        // Check if the incoming intent is a forwarding intent or not, add forwarding
        // data and change labels if it is
        patcher.patch(WidgetIncomingShare.class.getDeclaredMethod("initialize", WidgetIncomingShare.ContentModel.class),
                new PreHook(param -> {
                    WidgetIncomingShare share = (WidgetIncomingShare) param.thisObject;
                    Intent intent = share.getMostRecentIntent();
                    long messageId = intent.getLongExtra("io.gh.reisxd.aliuplugins.MESSAGE_ID", 0);
                    long channelId = intent.getLongExtra("io.gh.reisxd.aliuplugins.CHANNEL_ID", 0);
                    String messageContent = intent.getStringExtra("io.gh.reisxd.aliuplugins.MESSAGE_CONTENT");

                    if (messageId == 0 || channelId == 0)
                        return;

                    WidgetIncomingShareBinding binding = null;
                    try {
                        binding = (WidgetIncomingShareBinding) bindingReflection.invoke(share);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    AppBarLayout appBar = (AppBarLayout) binding.a.getChildAt(0);
                    Toolbar toolbar = (Toolbar) appBar.getChildAt(0);
                    toolbar.setTitle("Forward");
                    LinearLayout layout = (LinearLayout) binding.j.getChildAt(0);

                    TextView shareToText = (TextView) layout.getChildAt(4);
                    shareToText.setText("Forward To");

                    TextView messagePreviewText = (TextView) layout.getChildAt(0);
                    messagePreviewText.setText("Optional Message");

                    TextView previewText = new TextView(layout.getContext(), null, 0,
                            com.lytefast.flexinput.R.i.UiKit_TextAppearance);
                    TextView messagePreviewCustom = new TextView(layout.getContext(), null, 0,
                            com.lytefast.flexinput.R.i.UiKit_Search_Header);
                    messagePreviewCustom.setText("Message Preview");
                    previewText.setText(messageContent);
                    previewText.setPadding(DimenUtils.dpToPx(16), DimenUtils.dpToPx(2), 0, 0);

                    layout.addView(messagePreviewCustom, 0);
                    layout.addView(previewText, 1);

                }));

        // Send the forwarded message to selected channel with comment if any
        patcher.patch(WidgetIncomingShare.class.getDeclaredMethod("onSendClicked", Context.class,
                WidgetGlobalSearchModel.ItemDataPayload.class, ViewEmbedGameInvite.Model.class,
                WidgetIncomingShare.ContentModel.class, boolean.class, int.class, boolean.class,
                CaptchaHelper.CaptchaPayload.class), new PreHook(param -> {
            WidgetIncomingShare share = (WidgetIncomingShare) param.thisObject;
            WidgetGlobalSearchModel.ItemDataPayload itemDataPayload = (WidgetGlobalSearchModel.ItemDataPayload) param.args[1];
            Intent intent = share.getMostRecentIntent();

            WidgetIncomingShareBinding binding = null;
            try {
                binding = (WidgetIncomingShareBinding) bindingReflection.invoke(share);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            EditText textInput = binding.d.getEditText();

            long messageId = intent.getLongExtra("io.gh.reisxd.aliuplugins.MESSAGE_ID", 0);
            long channelId = intent.getLongExtra("io.gh.reisxd.aliuplugins.CHANNEL_ID", 0);
            if (messageId != 0 && channelId != 0) {
                long selectedChannel = itemDataPayload.getChannel().k();
                String commentMessage = textInput.getText().toString();
                Utils.threadPool.submit(() -> {
                    try {
                        Http.Response res = Http.Request
                                .newDiscordRNRequest(
                                        String.format("/channels/%d/messages", selectedChannel), "POST")
                                .executeWithJson(new Message(
                                        new MessageReference(1, messageId, channelId, null, false), ""));
                        if (!res.ok())
                            Toast.makeText(context, "Forwarding failed: " + res.statusCode,
                                    Toast.LENGTH_SHORT).show();
                        else {
                            if (!commentMessage.isEmpty()) {
                                Http.Request.newDiscordRNRequest(
                                                String.format("/channels/%d/messages", selectedChannel), "POST")
                                        .executeWithJson(new Message(null, commentMessage));
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                Utils.mainThread.post(() -> share.startActivity(IntentUtils.RouteBuilders.selectChannel(selectedChannel, 0, null)
                        .setPackage(Utils.getAppContext().getPackageName())));

                param.setResult(null);
            }
        }));

        // Activate the send button all the time if forwarding data exists
        patcher.patch(WidgetIncomingShare.class.getDeclaredMethod("configureUi", WidgetIncomingShare.Model.class,
                Clock.class), new PreHook(param -> {
            WidgetIncomingShare.Model model = (WidgetIncomingShare.Model) param.args[0];
            WidgetIncomingShare share = (WidgetIncomingShare) param.thisObject;
            Intent intent = share.getMostRecentIntent();
            long messageId = intent.getLongExtra("io.gh.reisxd.aliuplugins.MESSAGE_ID", 0);
            try {
                if (messageId != 0) {
                    modelCommentField.set(model, "...");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    public class MessageReference {
        public int type;
        public long message_id;
        public long channel_id;
        public Long guild_id;
        public boolean fail_if_not_exists;

        public MessageReference(int type, long message_id, long channel_id, Long guild_id, boolean fail_if_not_exists) {
            this.type = type;
            this.message_id = message_id;
            this.channel_id = channel_id;
            this.guild_id = guild_id;
            this.fail_if_not_exists = fail_if_not_exists;
        }
    }

    public int nextBits(Random rng, int bits) {
        if (bits < 0 || bits > 32)
            throw new IllegalArgumentException("bits must be 0..32");
        if (bits == 0)
            return 0;
        if (bits == 32)
            return rng.nextInt();
        int mask = (1 << bits) - 1;
        return rng.nextInt() & mask;
    }

    public int nextBits(int bits) {
        return nextBits(ThreadLocalRandom.current(), bits);
    }

    public class Message {
        public String content = "";
        public int flags = 0;
        public boolean tts = false;
        public String nonce = String.valueOf((SnowflakeUtils.fromTimestamp(System.currentTimeMillis()) + nextBits(23)));
        public String mobile_network_type = "unknown";
        public int signal_strength = 0;
        public MessageReference message_reference;

        public Message(MessageReference reference, String content) {
            this.message_reference = reference;
            this.content = content;
            Context context = Utils.getAppContext();
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        mobile_network_type = "wifi";
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        mobile_network_type = "cellular";
                    }

                    if (Build.VERSION.SDK_INT >= 28) {
                        SignalStrength ss = telephonyManager.getSignalStrength();
                        signal_strength = (ss != null) ? ss.getLevel() : 0;
                    }
                }
            }
        }
    }
}
