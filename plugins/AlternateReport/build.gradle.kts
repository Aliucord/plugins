import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Replaces the message report button with opening the report form."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
			# 1.0.1
			* Include server id in link

			# 1.0.0
			* Released
		""".trimIndent(),
    )
}
