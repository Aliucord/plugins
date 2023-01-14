# Modern spec

## Source plugin repositories

There is 1 important file that is used for building that plugin's repository, `.aliucordrc`, which is a json file that configures the deployment build.

### .aliucordrc format:
```json
{
    "buildAllCommand": "pnpm i --frozen-lockfile && pnpm buildAll",
    "distFolder": "./dist",
    "ignoredPlugins": ["PluginName1", "PluginName2"]
}
```

- `buildAllCommand` -> A shell command that builds every single plugin
- `distFolder` -> A relative path from repo root to the target dist folder you will build plugins to
- `ignoredPlugins` -> Array of plugin names to ignore when deploying. Considering that 2 plugins cannot have the same name, it is vital to stop any personal "forked" plugins from deploying.

## This Plugin Repo

There are 5 different important items in this plugin repo as a whole:

- `full.json`: A file containing each plugin's manifest combined into an array. It is used for manager's plugin repo.
- [`updater.json`](updater.md): A basic versioning list with minimal information about each plugin for updating
- [`{plugin}/metadata.json`](metadata.md): A file containing build-specific update data, like hash and commit.
- [`{plugin}/manifest.json`](manifest.md): The copied manifest output file from the built plugin.
- `{plugin}/LICENSE`: The license file that the plugin is released under in its own repository
- `{plugin}/repository`: A symlink to the repository that is copied into this repository. This also determines the owner of this plugin. Other repositories will not be able to deploy plugins with names that are owned by another repository. [More below](#copied-source-repositories)
- `{plugin}/{plugin}.zip`: The built plugin that is downloaded on install or update.

For reviewers, you can read [reviewing.md](reviewing.md) to get a basic idea of what should be done before approving a PR.
For plugin devs, you can read [contributing.md](contributing.md) to learn how to upload your plugins to this repo.

### Copied source repositories

To not deal with calculating the correct diff url between the old commit & new (especially with force pushes being a thing), we instead just copy the entire repository (excluding `.git`) to `/repositories/<repo id>/` on every plugin update. Note that `<repo id>` is the integer representation returned by the API, NOT a human readable name. This is so that username changes will have no effect on the diff process. Instead, each associated plugin has a symlink in their folder pointing to their source repository. Copying the repository also allows for everything to be under one PR to review, instead of hopping around, as well as that we will always retain source code to plugins, even if the source repo gets removed.
