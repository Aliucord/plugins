import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Allows changing the download path"

aliucord {
    author(Authors.Grzesiek11)

    changelog.set("1.0.0:\n- Initial release")
}
