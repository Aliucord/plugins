import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "2.0.0"
description = "Shows all the currently typing users by long clicking the typing users bar."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.0.0
		* Released

		# 2.0.0
		* Use a separate screen instead of BottomSheet
		* Show currently and previously typing users
		* Fix FragmentManager crash
		""".trimIndent(),
    )
}
