package com.github.lampdelivery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.settings.delegate

@AliucordPlugin(requiresRestart = false)
class AtPrefixProfiles : Plugin() {
    private val debugLog by settings.delegate(true)
    private val enableMemberList by settings.delegate(true)
    private val enableMiniProfile by settings.delegate(true)

    override fun start(context: Context) {
        logger.info("AtPrefixProfiles start")
        val candidates = listOf(
            "com.discord.widgets.user.profile.UserProfileHeaderView",
            "com.discord.widgets.user.profile.WidgetUserProfile",
            "com.discord.widgets.user.usersheet.WidgetUserSheet",
            "com.discord.widgets.user.profile.UserProfileHeaderViewV2"
        )
        var hooked = false
        for (name in candidates) {
            try {
                val cls = Class.forName(name)
                val bundleCls = Class.forName("android.os.Bundle")
                val method = try {
                    cls.getDeclaredMethod("onViewCreated", View::class.java, bundleCls)
                } catch (_: Throwable) {
                    cls.getMethod("onViewCreated", View::class.java, bundleCls)
                }
                patcher.patch(method, Hook { cf ->
                    try {
                        val root = cf.args?.getOrNull(0) as? View ?: return@Hook
                        val idCandidates = listOf(
                            "user_profile_header_primary_name",
                            "user_profile_header_secondary_name",
                            "user_profile_header_name",
                            "profile_header_username"
                        )
                        val pkgCandidates = listOf(
                            "com.discord",
                            "com.discord.app",
                            root.context.packageName
                        )
                        val nameView = findByIdNames(root, idCandidates, pkgCandidates)
                            ?: findUsernameTextView(root)
                        if (nameView != null) {
                            logger.info("AtPrefixProfiles: applying @ to view id from candidates: ${nameView.id}")
                            applyAtPrefixSafely(nameView)
                            attachReapplyGuards(nameView)
                        } else {
                            if (debugLog) {
                                logger.info("AtPrefixProfiles: username TextView not found; dumping up to 50 TextViews")
                                dumpTextViews(root, limit = 50)
                            }
                        }
                    } catch (e: Throwable) {
                        logger.error("AtPrefixProfiles: hook body error", e)
                    }
                })
                logger.info("AtPrefixProfiles: hooked $name")
                hooked = true
                break
            } catch (t: Throwable) {
                logger.info("AtPrefixProfiles: failed to hook $name: ${t.javaClass.simpleName}: ${t.message ?: "no message"}")
            }
        }
        if (!hooked) logger.info("AtPrefixProfiles: no profile class matched; username prefix may not apply")

        // Bind-time fallback
        val bindCandidates = listOf(
            Pair("com.discord.widgets.user.profile.UserProfileHeaderView", listOf("bind", "configure", "setUser")),
            Pair("com.discord.widgets.user.profile.WidgetUserProfile", listOf("onResume")),
            Pair("com.discord.widgets.user.usersheet.WidgetUserSheet", listOf("onResume"))
        )
        for ((clsName, methods) in bindCandidates) {
            try {
                val c = Class.forName(clsName)
                for (mName in methods) {
                    for (m in c.declaredMethods) {
                        if (m.name != mName) continue
                        patcher.patch(m, Hook { cf ->
                            try {
                                val owner = cf.thisObject
                                val rootField = owner?.javaClass?.declaredFields?.firstOrNull { f ->
                                    View::class.java.isAssignableFrom(f.type)
                                }?.apply { isAccessible = true }
                                val rootView = (rootField?.get(owner) as? View) ?: return@Hook
                                val idCandidates = listOf(
                                    "user_profile_header_primary_name",
                                    "user_profile_header_secondary_name",
                                    "user_profile_header_name",
                                    "profile_header_username"
                                )
                                val pkgCandidates = listOf("com.discord", "com.discord.app", rootView.context.packageName)
                                val tv = findByIdNames(rootView, idCandidates, pkgCandidates) ?: findUsernameTextView(rootView)
                                if (tv != null) {
                                    applyAtPrefixSafely(tv)
                                    attachReapplyGuards(tv)
                                }
                            } catch (_: Throwable) {}
                        })
                        logger.info("AtPrefixProfiles: bind-time hook applied ${clsName}#${mName}")
                    }
                }
            } catch (_: Throwable) { 
            }
        }

        try {
            val appFragment = Class.forName("com.discord.app.AppFragment")
            for (m in appFragment.declaredMethods) {
                if (m.name == "onViewCreated") {
                    patcher.patch(m, Hook { cf ->
                        val root = cf.args?.getOrNull(0) as? View ?: return@Hook
                        val idCandidates = listOf(
                            "user_profile_header_primary_name",
                            "user_profile_header_secondary_name",
                            "user_profile_header_name",
                            "profile_header_username",
                            "settings_user_profile_header_name",
                            "settings_profile_preview_name",
                            "user_profile_header_name_wrap"
                        )
                        val pkgCandidates = listOf("com.discord", root.context.packageName)
                        var tv = findByIdNames(root, idCandidates, pkgCandidates)
                        if (tv == null) {
                            val ctx = root.context
                            val wrapId = ctx.resources.getIdentifier("user_profile_header_name_wrap", "id", ctx.packageName)
                            val wrap = if (wrapId != 0) root.findViewById<ViewGroup?>(wrapId) else null
                            if (wrap != null) {
                                tv = findFirstTextView(wrap) { candidate ->
                                    val text = candidate.text?.toString() ?: return@findFirstTextView false
                                    text.isNotBlank() && !text.startsWith("@") && !text.contains("#")
                                }
                            }
                        }
                        if (tv == null) tv = findUsernameTextView(root)
                        if (tv != null) {
                            applyAtPrefixSafely(tv)
                            attachReapplyGuards(tv)
                            logger.info("AtPrefixProfiles: applied @ in settings profile preview")
                        }
                    })
                    logger.info("AtPrefixProfiles: hooked AppFragment#onViewCreated for settings preview")
                }
            }
        } catch (_: Throwable) {}

        if (enableMemberList) {
        val memberRowCandidates = listOf(
            "com.discord.widgets.user.list.adapter.UserListItem",
            "com.discord.widgets.user.list.adapter.WidgetUserListAdapterItem",
            "com.discord.widgets.user.list.adapter.WidgetUserListAdapterItemMember"
        )
        for (name in memberRowCandidates) {
            try {
                val cls = Class.forName(name)
                for (m in cls.declaredMethods) {
                    if (m.name == "bind" || m.name == "configure" || m.name == "onConfigure") {
                        patcher.patch(m, Hook { cf ->
                            val root = cf.args?.firstOrNull { it is View } as? View
                                ?: (cf.thisObject?.let { owner ->
                                    owner.javaClass.declaredFields.firstOrNull { f ->
                                        View::class.java.isAssignableFrom(f.type)
                                    }?.apply { isAccessible = true }?.get(owner) as? View
                                })
                            if (root != null) {
                                val idCandidates = listOf(
                                    "user_list_item_name",
                                    "member_list_item_name",
                                    "widget_user_list_item_name"
                                )
                                val pkgCandidates = listOf("com.discord", root.context.packageName)
                                val tv = findByIdNames(root, idCandidates, pkgCandidates) ?: findUsernameTextView(root)
                                if (tv != null) {
                                    applyAtPrefixSafely(tv)
                                    attachReapplyGuards(tv)
                                }
                            }
                        })
                        logger.info("AtPrefixProfiles: hooked member row ${name}#${m.name}")
                    }
                }
            } catch (_: Throwable) {}
        }
        }

        if (enableMiniProfile) {
        val miniProfileCandidates = listOf(
            "com.discord.widgets.user.usersheet.WidgetUserSheet",
            "com.discord.widgets.user.profile.UserProfileHeaderViewV2"
        )
        for (name in miniProfileCandidates) {
            try {
                val cls = Class.forName(name)
                for (m in cls.declaredMethods) {
                    if (m.name == "onViewCreated" || m.name == "onResume" || m.name == "bind") {
                        patcher.patch(m, Hook { cf ->
                            val root = cf.args?.firstOrNull { it is View } as? View
                                ?: (cf.thisObject?.let { owner ->
                                    owner.javaClass.declaredFields.firstOrNull { f ->
                                        View::class.java.isAssignableFrom(f.type)
                                    }?.apply { isAccessible = true }?.get(owner) as? View
                                })
                            if (root != null) {
                                val idCandidates = listOf(
                                    "user_sheet_header_name",
                                    "user_profile_header_primary_name",
                                    "user_profile_header_name"
                                )
                                val pkgCandidates = listOf("com.discord", root.context.packageName)
                                val tv = findByIdNames(root, idCandidates, pkgCandidates) ?: findUsernameTextView(root)
                                if (tv != null) {
                                    applyAtPrefixSafely(tv)
                                    attachReapplyGuards(tv)
                                }
                            }
                        })
                        logger.info("AtPrefixProfiles: hooked mini-profile ${name}#${m.name}")
                    }
                }
            } catch (_: Throwable) {}
        }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        logger.info("AtPrefixProfiles stop")
    }

    private fun applyAtPrefix(tv: TextView) {
        val text = tv.text?.toString() ?: return
        if (text.isEmpty()) return
        if (text.startsWith("@")) return
        if (text.contains("#") || text.contains("\n")) return
        val current = tv.text
        if (current is android.text.Spannable) {
            val sb = android.text.SpannableStringBuilder()
                .append("@")
                .append(current)
            tv.text = sb
        } else {
            tv.text = "@" + text
        }
    }

    private fun applyAtPrefixSafely(tv: TextView) {
        try {
            applyAtPrefix(tv)
        } catch (_: Throwable) {
            val text = tv.text?.toString() ?: return
            if (text.isBlank() || text.startsWith("@")) return
            if (text.contains("#") || text.contains('\n')) return
            tv.post { tv.text = "@" + text }
        }
    }

    private fun findUsernameTextView(root: View): TextView? {
        val tv = findFirstTextView(root) { candidate ->
            val text = candidate.text?.toString() ?: return@findFirstTextView false
            if (text.isBlank()) return@findFirstTextView false
            val lenOk = text.length in 2..32
            val noAtYet = !text.startsWith("@")
            val notTag = !text.contains("#")
            lenOk && noAtYet && notTag
        }
        return tv
    }

    private fun findByIdName(root: View, idName: String, packageName: String? = null): TextView? {
        return try {
            val ctx = root.context
            val pkg = packageName ?: ctx.packageName
            val id = ctx.resources.getIdentifier(idName, "id", pkg)
            if (id != 0) root.findViewById<TextView>(id) else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun findByIdNames(root: View, idNames: List<String>, packageNames: List<String>): TextView? {
        for (pkg in packageNames) {
            for (idName in idNames) {
                val tv = findByIdName(root, idName, pkg)
                if (tv != null) return tv
            }
        }
        return null
    }

    private fun dumpTextViews(root: View, limit: Int) {
        var count = 0
        fun walk(v: View) {
            if (count >= limit) return
            if (v is TextView) {
                val txt = v.text?.toString() ?: ""
                val idStr = try { v.resources.getResourceName(v.id) } catch (_: Throwable) { v.id.toString() }
                logger.info("AtPrefixProfiles: tv id=$idStr text='${txt.take(80)}'")
                count++
            }
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) {
                    walk(v.getChildAt(i))
                    if (count >= limit) return
                }
            }
        }
        walk(root)
    }

    private fun attachReapplyGuards(tv: TextView) {
        try {
            tv.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                applyAtPrefixSafely(tv)
            }
        } catch (_: Throwable) {}
        try {
            tv.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    applyAtPrefixSafely(tv)
                }
            })
        } catch (_: Throwable) {}
    }

    private fun findFirstTextView(root: View, predicate: (TextView) -> Boolean): TextView? {
        if (root is TextView && predicate(root)) return root
        if (root !is ViewGroup) return null
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            val found = findFirstTextView(child, predicate)
            if (found != null) return found
        }
        return null
    }
}
