version = "1.0.3" // Plugin version. Increment this to trigger the updater
description = "Forward messages to your channel of your liking." // Plugin description that will be shown to user

aliucord {
    // Changelog of your plugin
    changelog.set("""
        v1.0.3
        - Fixed forward icon having incorrect coloring due to wrong context
        
        v1.0.2
        - Fixed sharing being broken
        
        v1.0.1
        - Changed the icon with the forward icon (mirrored)
        - Internal data processing for IDs is now different
        
        v1.0.0
        - Initial release
    """.trimIndent())
    // Image or Gif that will be shown at the top of your changelog page
    // changelogMedia.set("https://cool.png")

    // Add additional authors to this plugin
    // author("Name", 0)
    // author("Name", 0)

    // Excludes this plugin from the updater, meaning it won't show up for users.
    // Set this if the plugin is unfinished
    excludeFromUpdaterJson.set(false)
}
