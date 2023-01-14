# Reviewing

A few main people are allowed to review pull requests to this repository (which is how new plugins and plugin updates are submitted). Others can review, but their review will not affect the automated process.

**NOTE:** This process is for RN plugins only, as the Kt plugin store has not been made yet.

## Process

Because of the way building is done, multiple plugins can be either updated or added in one commit. However, you only need to check the source once, as it is shared between plugins (per repository).\
Each PR must be created by the Aliucord Bot, from some branch to the `data` branch. If the PR does not match that description, leave it alone.

- Check that each new `manifest.json` that it matches the required parts of the specification [here](./manifest.md)
  - Note that version bumps are not required! This indicates that the author is pushing out an unimportant change that does not need redownlading.
- There should be changes under `/repositories/<id>`, this is the source repository the plugin was built from. Look over ***every single file*** to make sure nothing malicious has been added, this includes:
  - ALL of the plugin source code
  - `package.json` Make absolute sure that the dependencies are official, and check that the scripts don't do anything strange. (lockfile can be ignored)
  - `.aliucordrc` Check the `buildAllPlugins` command, this should have `pnpm i --frozen-lockfile` included or the equivalent method for ignoring the lockfile.
  - Rollup config files in the root dir
  - And a quick glance over everything else

If all of the above checks out, then you can approve the pull request, and when enough verified people approve, it will be merged automatically.
