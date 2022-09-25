# Reviewing

A few main people are allowed to review pull requests to this repository (which is how new plugins and plugin updates are submitted). Others can review, but their review will not count.

## Process

In order to verify that everything is set up correctly, everything below must be updated with the correct information:
- `updater.json`
- `index.json`
- `{plugin}/metadata.json`
- `{plugin}/{plugin filename}`

Additionally, `{plugin}/LICENSE` may also be updated if it has changed (or if the plugin is new) in the source code repository.

If all of the files are setup correctly, then both the plugin's source code and built file should be checked. The big things to check are below:
- The file hash specified in `metadata.json` should line up with the actual uploaded file
- The source code (make sure to use the commit specified in `metadata.json`) should be inspected for any malicious code
- The plugin should be manually built, to make sure the file hash lines up with the one that is uploaded.
    - This could be automated with CI in the future
- All versions and names should match in all of the updated files

If all of the above checks out, then you can approve the pull request, and when enough people approve, it will be merged.