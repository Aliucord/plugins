import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.5"
description = "Show typing indicators in the channel list."

aliucord {
    author(Authors.rushii)
    author(Authors.zt)

    changelog.set(
        """
		# 1.0.5
		* Remove compatibility for an older Discord version

		# 1.0.4
		* Update to Discord 124.12

		# 1.0.3
		* Remove log

		# 1.0.2
		* Improve performance

		# 1.0.1
		* Fix plugin

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
