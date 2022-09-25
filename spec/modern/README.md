# Modern spec

There are 5 different important files relating to aliucord plugins for the modern (java/kotlin) discord app.

- [`updater.json`](updater.md): A simple file containing every plugin to quickly check for plugin updates
- [`index.json`](index.md): A detailed file containing useful information to show to the user, for use in manager
- [`{plugin}/metadata.json`](metadata.md): A file containing plugin-specific data, like changelog and filename
- `{plugin}/LICENSE`: The license file that the plugin is released under in its own repository
- `{plugin}/{prebuilt plugin file}`: The prebuilt plugin file that is downloaded on install or update. Can be named anything, the name is specified in metadata.json.

For reviewers, you can read [reviewing.md](reviewing.md) to get a basic idea of what should be done before approving a PR.
For plugin devs, you can read [contributing.md](contributing.md) to learn how to upload your plugins to this repo.