import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Quickly clone text channels in the context menu. (perms, name, topic, etc.)"

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.0.1
		* Update to new Discord

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
