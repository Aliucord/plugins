# Contributing

In order to submit plugins or plugin updates to this repo, please follow these steps:

Prerequisites:

- You know the basics of how to use Git
- You have a published repository with the source code of your plugin on GitHub
- You are using a permissive license that allows us to distribute your source code and builds

# First time publish

1. Add a [aliucord.json](./README.md#copied-source-repositories) to the root of your plugins repository.
2. Authorize the [plugin builder](https://github.com/apps/aliucord-plugins) on your repository (Do not select all your
   repos).
3. Add a release branch pointing to the HEAD of your default branch (if you haven't already)
4. A new PR will be created [here](https://github.com/Aliucord/plugins) once you push the branch to remote.
5. Once your code is reviewed, the built plugin will be published immediately.

# Updates & other plugins

If you are not using a monorepo to store multiple plugins at once, go back to the section above for your new
repository.f

Deploying updates or publishing new plugins is still relatively simple:

1. Update your release branch to point at the new source HEAD
2. Push to origin

Your code will once again be reviewed and deployed. If its an urgent update, then you can contact us on the Discord
server under #plugin-development.
