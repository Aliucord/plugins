import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.3.1"
description = "Make Twitter embed images download as `jpg` instead of `jpg:large`."

aliucord {
    author(Authors.PikachuGabe)

    changelog.set("""
        # 1.3.1
        Bluesky actually stores the files as @jpeg instead of .jpeg so changing the URL doesn't work, so I can't fix that right now. 
        Will have to figure out how to change the file type after downloading instead of just changing the download URL.

        # 1.3.0
        Added bluesky support because I noticed it uses `@` instead of `.` when denoting file type. Not updating plugin name as that would break the updater.

        # 1.2.0
        Fixed the plugin. Again.
        Now downloads directly from twitter instead of through discord's external image stuff.

        # 1.1.0
        Fixed the plugin.
        Switched to regex for file renaming.
        Renamed plugin at Rushii's request.

        # 1.0.0
        Plugin release
    """)
}