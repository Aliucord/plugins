import { Forms, ReactNative, URLOpener, React, Styles, Constants } from "aliucord/metro";
import { getAssetId } from "aliucord/utils";

const { FormRow, FormSection, FormIcon, FormText } = Forms;
const { ScrollView, View } = ReactNative;

const styles = Styles.createThemedStyleSheet({
    description: {
        marginLeft: 12,
        marginRight: 12,
        color: Styles.ThemeColorMap.TEXT_NORMAL,
    },
    descriptionBg: {
        backgroundColor: Styles.ThemeColorMap.BACKGROUND_SECONDARY,
        borderRadius: 15,
        paddingLeft: 12,
        paddingRight: 12,
        marginBottom: 10,
        fontSize: 14
    },
    title: {
        fontSize: 16,
        color: Styles.ThemeColorMap.TEXT_NORMAL,
        fontFamily: Constants.Fonts.PRIMARY_BOLD,
        marginBottom: 10
    }
});

export function SettingsPage() {
    return (<>
        <ScrollView>
            <FormSection title="Custom Background" android_noDivider={true}>
                <View style={styles.description}>
                    <FormText style={styles.title}>
                        Looking to change your background settings?
                    </FormText>
                    <FormText style={styles.descriptionBg}>
                        1 - Join the Blackbox Discord by tapping on the join button below.{"\n"}
                        2 - Go into the #background-requests channel and upload an image/GIF, no videos allowed.{"\n"}
                        3 - Check the #userbg-log channel and wait for your banner to get approved. You can see your own banner in the image preview.{"\n"}
                        4 - When your banner gets approved, restart AliucordRN. It will automatically load it for you.
                    </FormText>
                        
                    <FormText>
                        Please do not ask for support in the aliucord server if your problem is related to banner requests.
                    </FormText>
                </View>
            </FormSection>
            <FormSection title="Blackbox Discord">
                <FormRow
                    leading={<FormIcon source={getAssetId("Discord")} />}
                    label="Blackbox Discord"
                    subLabel="Tap to join the Discord server."
                    trailing={FormRow.Arrow}
                    onPress={() => { URLOpener.openURL("https://discord.gg/TeRQEPb") }}
                />
            </FormSection>
        </ScrollView>
    </>)
}
