import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.0"
description = "Allows editing messages with a sed-like substitiution command"

aliucord {
    author(Authors.Grzesiek11)

    changelog.set("1.0.0:\n- Initial release\n1.1.0:\n- Add advanced mode (see README for details)")
}
