package moe.lava.awoocord.roleblocks

import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.settings.delegate
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.wrappers.users.globalName
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.views.CheckedSetting
import com.lytefast.flexinput.R
import kotlin.math.roundToInt
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

enum class Mode {
    RoleDot,
    Block,
}

enum class BlockMode {
    ApcaLightWcagDark,
    WcagLightApcaDark,
    ApcaOnly,
    WcagOnly,
    ThemeOnly,
    InvertedThemeOnly,
    WhiteOnly,
    BlackOnly,
    Unchanged,
}

class SettingsDelegateEnum<T : Enum<T>>(
    private val defaultValue: T,
    private val settings: SettingsAPI,
    private val deserialiser: (String) -> T,
) : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        deserialiser(settings.getString(property.name, defaultValue.name))

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        settings.setString(property.name, value.name)
}

inline fun <reified T : Enum<T>> SettingsAPI.delegateEnum(
    defaultValue: T
) = SettingsDelegateEnum(defaultValue, this) { enumValueOf<T>(it) }

private inline fun <T : View> T.addTo(parent: ViewGroup, block: T.() -> Unit = {}) =
    apply {
        block()
        parent.addView(this)
    }

private typealias Delegate<Type> = ReadWriteProperty<Any, Type>

fun <T> basicDelegate(initial: T) = object : Delegate<T> {
    private var current = initial
    override fun getValue(self: Any?, prop: KProperty<*>): T = current
    override fun setValue(self: Any?, prop: KProperty<*>, value: T) { current = value }
}

private class StateDelegate<T>(
    private val inner: Delegate<T>,
    private val update: (T) -> Unit,
) : Delegate<T> {
    override fun getValue(self: Any?, prop: KProperty<*>): T = inner.getValue(self, prop)

    override fun setValue(self: Any?, prop: KProperty<*>, value: T) {
        inner.setValue(self, prop, value)
        update(value)
    }
}

object RoleBlocksSettings {
    private val api = SettingsAPI()

    private var onStateUpdate = {}

    private inline fun <T> reactive(backing: () -> Delegate<T>): StateDelegate<T> {
        return StateDelegate(backing()) { onStateUpdate() }
    }

    var mode by reactive { api.delegateEnum(Mode.Block) }

    var blockAlsoDefault by reactive { api.delegate(true) }
    var blockInverted by reactive { api.delegate(false) }
    var blockMode by reactive { api.delegateEnum(BlockMode.ApcaLightWcagDark) }
    var blockApcaThresholdLarge by reactive { api.delegate(45.0f) }
    var blockApcaThresholdMedium by reactive { api.delegate(45.0f) }
    var blockApcaThresholdSmall by reactive { api.delegate(45.0f) }
    var blockWcagThreshold by reactive { api.delegate(4.5f) }

    private val _alpha = reactive { api.delegate("alpha", 255) }
    var alpha by _alpha

    class Page : SettingsPage() {
        private val checks = mutableListOf<CheckedSetting>()

        private val _previewH = reactive { basicDelegate(0) }
        private var previewH by _previewH
        private val _previewS = reactive { basicDelegate(100) }
        private var previewS by _previewS
        private val _previewV = reactive { basicDelegate(100) }
        private var previewV by _previewV

        private fun addRadio(newMode: BlockMode, text: String, subtext: String? = null): CheckedSetting {
            return Utils.createCheckedSetting(requireContext(), CheckedSetting.ViewType.RADIO, text, subtext).addTo(linearLayout) {
                isChecked = blockMode == newMode
                setOnCheckedListener {
                    for (check in checks) check.isChecked = false
                    blockMode = newMode
                    isChecked = true
                }
                checks.add(this)
            }
        }

        private fun createLabel(text: String? = null): TextView {
            return TextView(context, null, 0, R.i.UiKit_TextView).apply {
                textSize = 16.0f
                typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_medium)
                this.text = text
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    bottomMargin = 4.dp
                }
            }
        }

        private fun addSlider(
            min: Int,
            max: Int,
            initial: Int = min,
            onChange: (value: Int, commit: Boolean) -> String
        ): LinearLayout {
            var pendingValue = initial
            return LinearLayout(requireContext(), null, 0, R.i.UiKit_Settings_Item).addTo(linearLayout) {
                orientation = LinearLayout.VERTICAL
                val display = createLabel(onChange(initial, false)).addTo(this)
                SeekBar(context, null, 0, R.i.UiKit_SeekBar).addTo(this) {
                    this.max = max - min
                    progress = initial
                    setPadding(12.dp, 0, 12.dp, 0)
                    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean,
                        ) {
                            pendingValue = min + progress
                            display.text = onChange(pendingValue, false)
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar) {
                            onChange(pendingValue, true)
                        }
                    })
                }
            }
        }

        private fun addSlider(binding: Delegate<Int>, min: Int, max: Int, immediate: Boolean = false, label: (Int) -> String): LinearLayout {
            var value by binding
            return addSlider(min, max, value) { newValue, commit ->
                @Suppress("AssignedValueIsNeverRead") // kt so dumb
                if (immediate || commit) value = newValue
                label(newValue)
            }
        }

        private fun createPreview(
            label: String,
            styleRes: Int,
        ): TextView {
            val ctx = requireContext()
            val view = TextView(ctx, null, 0, styleRes).apply {
                val me = StoreStream.getUsers().me
                text = me.globalName ?: me.username
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    marginStart = 16.dp
                    marginEnd = 16.dp
                }
            }
            LinearLayout(ctx, null, 0, R.i.UiKit_Settings_Item).addTo(linearLayout) {
                view.addTo(this)
                createLabel(label).addTo(this) {
                    layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                        bottomMargin = 0
                    }
                }
            }
            return view
        }

        override fun onDestroyView() {
            onStateUpdate = {}
            super.onDestroyView()
        }

        override fun onViewBound(view: View) {
            super.onViewBound(view)
            setActionBarTitle("RoleBlocks")
            setPadding(0)

            val ctx = requireContext()
            linearLayout.run {
                val blockSettings = mutableListOf<CheckedSetting>()
                val roleDotSettings = mutableListOf<CheckedSetting>()

                addHeader(ctx, "Text colour")
                addRadio(BlockMode.ApcaLightWcagDark, "Automatic", "Adjusts text colour based on optimal contrast with role colour")
                addRadio(BlockMode.ThemeOnly, "By theme", "Adjusts text colour based on system theme (dark/light)")
                addRadio(BlockMode.InvertedThemeOnly, "By theme (inverted)", "Same as above, but inverted")
                addRadio(BlockMode.WhiteOnly, "White", "Force text colour to be white")
                addRadio(BlockMode.BlackOnly, "Black", "Force text colour to be black")
                addRadio(BlockMode.Unchanged, "Unchanged", "Keep text colour; ideal for using with a translucent block")

                addHeader(ctx, "Block Settings")

                val invertSwitch = Utils.createCheckedSetting(
                    ctx,
                    CheckedSetting.ViewType.SWITCH,
                    "Invert block colours",
                    "By default, the role colour is applied as the block background. Turning this setting on inverts this.\nHas no effect with \"Unchanged\" colour option",
                ).addTo(this) {
                    isChecked = blockInverted
                    setOnCheckedListener {
                        blockInverted = !blockInverted
                    }
                    blockSettings.add(this)
                }

                addSlider(_alpha, 0, 255, true) { "Alpha: ${(it / 2.55f).roundToInt()}%" }

//                createSlider(0, 255, blockApcaThreshold.roundToInt()) { value, commit ->
//                    blockApcaThreshold = value.toFloat()
//                    "Apca Threshold: $value"
//                }

                addHeader(ctx, "Preview")
                val previews = mutableListOf(
                    Threshold.Large to createPreview("Message header username", R.i.UiKit_TextView_Large_SingleLine),
                    Threshold.Medium to createPreview("Channels list", R.i.UiKit_TextView).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.d.uikit_textsize_medium))
                    },
                    Threshold.Small to createPreview("Message reply username", R.i.UiKit_TextView).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.d.uikit_textsize_small))
                    },
                )

                val hsv = floatArrayOf(0f, 0f, 0f)
                Color.colorToHSV(ColorCompat.getThemedColor(this, R.b.color_brand), hsv)
                previewH = hsv[0].roundToInt()
                previewS = (hsv[1] * 100).roundToInt()
                previewV = (hsv[2] * 100).roundToInt()
                addSlider(_previewH, 0, 360, true) { "Hue: $it" }
                addSlider(_previewS, 0, 100, true) { "Saturation: $it%" }
                addSlider(_previewV, 0, 100, true) { "Value: $it%" }

                onStateUpdate = {
                    previews.forEach { updatePreview(it) }
                    if (blockMode != BlockMode.Unchanged) {
                        invertSwitch.l.b().isClickable = true
                        invertSwitch.alpha = 1f
                    } else {
                        invertSwitch.l.b().isClickable = false
                        invertSwitch.alpha = 0.3f
                    }
                }
                onStateUpdate()
            }
        }

        fun updatePreview(pair: Pair<Threshold, TextView>) {
            val (threshold, preview) = pair
            val colour = Color.HSVToColor(floatArrayOf(previewH.toFloat(), previewS / 100f, previewV / 100f))
            APCAUtil.configureOn(preview, colour, threshold)
        }
    }
}
