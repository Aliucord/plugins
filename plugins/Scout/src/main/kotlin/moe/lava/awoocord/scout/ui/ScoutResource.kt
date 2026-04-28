package moe.lava.awoocord.scout.ui

import android.content.res.Resources
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

class ScoutResource(private val resources: Resources) {
    companion object {
        val SORT_FILTER = View.generateViewId()
        val SORT_ANSWER = View.generateViewId()
        val EXCLUDE_FILTER = View.generateViewId()
        val AUTHOR_TYPE_FILTER = View.generateViewId()
        val AUTHOR_TYPE_ANSWER = View.generateViewId()
    }

    fun getId(name: String, type: String) =
        resources.getIdentifier(name, type, "moe.lava.awoocord.scout")

    @DrawableRes fun getDrawableId(name: String) =
        getId(name, "drawable")

    fun getDrawable(@DrawableRes id: Int) =
        ResourcesCompat.getDrawable(resources, id, null)

    fun getDrawable(name: String) =
        getDrawable(getDrawableId(name))
}
