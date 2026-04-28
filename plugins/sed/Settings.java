package grzesiek11.aliucordplugins.sed;

import android.os.Bundle;
import android.view.View;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.Utils;
import com.discord.views.CheckedSetting;

public class Settings extends SettingsPage {
    private SettingsAPI settings;

    Settings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        var context = this.requireContext();

        this.setActionBarTitle("Sed");

        var advancedModeSetting = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Advanced mode", "Enables advanced features like regular expressions");
        advancedModeSetting.setChecked(this.settings.getBool(Plugin.ADVANCED_MODE_SETTING_KEY, Plugin.ADVANCED_MODE_SETTING_DEFAULT));
        advancedModeSetting.setOnCheckedListener(checked -> {
            this.settings.setBool(Plugin.ADVANCED_MODE_SETTING_KEY, checked);
        });
        this.addView(advancedModeSetting);
    }
}
