package com.github.ushie.ui

import android.content.res.Resources
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

class NewMemberBadgeResource(private val resources: Resources) {

    fun getId(name: String, type: String) =
        resources.getIdentifier(name, type, "com.github.ushie")

    @DrawableRes fun getDrawableId(name: String) =
        getId(name, "drawable")

    fun getDrawable(@DrawableRes id: Int) =
        ResourcesCompat.getDrawable(resources, id, null)

    fun getDrawable(name: String) =
        getDrawable(getDrawableId(name))
}
