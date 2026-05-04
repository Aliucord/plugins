package grzesiek11.aliucordplugins.changedownloadpath;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import com.aliucord.api.SettingsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.TextInput;

public class Settings extends SettingsPage {
    private SettingsAPI settings;

    Settings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        var context = this.requireContext();

        this.setActionBarTitle("ChangeDownloadPath");

        var pathSetting = new TextInput(context);
        pathSetting.setHint("Path");
        var pathEditText = pathSetting.getEditText();
        pathEditText.setText(this.settings.getString(Plugin.PATH_SETTING_KEY, Plugin.PATH_SETTING_DEFAULT));
        pathEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                Settings.this.settings.setString(Plugin.PATH_SETTING_KEY, editable.toString());
            }
        });
        this.addView(pathSetting);
    }
}
