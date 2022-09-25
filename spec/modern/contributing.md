# Contributing

This file contains the (very speculative at the moment) instructions for uploading your plugin to this repo.

1. Install `@aliucord/cli` package as a devDependency in your plugin repository
2. Make sure the appropriate details are set up for your plugin manifest
3. Make sure you have an [aliucord.yml](aliucord.md) file set up correctly
4. Run `pnpm aliu publish` and follow the instructions
    - This will ask for many details, like the new version, the old names of this plugin (if any), the changelog for this version, and more
    - Once all the information is collected, it will ask for a github personal access token, and will use this to create a PR to this repo.