# {plugin}/metadata.json

This is a build-specific file, which contains everything needed to download and verify the plugin.

## General structure

```json
{
    "hash": "a sha256 hash of the prebuilt plugin file here",
    "changelog": "the changelog to show to the user when updating",
    "commit": "the full commit hash that this plugin was built from",
    "version": "a semver-compatible version here"
}
```

## Properties

### hash

The sha256 hash of the prebuilt plugin file. This is used to verify that the plugin was downloaded correctly.

### changelog

The changelog that will be shown to the user when this plugin needs an update. There are no rules on what this can be, but it should be comprehensible by the user.

### commit

The full commit hash that this plugin version was compiled from. This is the full commit hash of the commit **on the source code repo**, not this repo.
This can be null.

### version

The current version of this plugin. This MUST be semver compatible.

## Example

```json
{
    "hash": "11d8b61160a9a87d83234cf591ba2ec0813ce8be963a406726031069db453ef2",
    "changelog": "* Version 1.0.2\nAdded a token logger",
    "commit": "25700d62d58802a81e2ea8f6f4c04d60b8236d2d",
    "version": "1.0.2"
}
```
