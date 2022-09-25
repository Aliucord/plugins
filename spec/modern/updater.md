# updater.json

This is a very simple file, used only by the aliucord updater, to quickly check if any installed plugins are outdated.

## General format
```json
[
    {
        "name": "plugin name here",
        "version": "semver-compatible version here",
        "oldNames": ["old name of this plugin here"]
    }
]
```

## Properties

### name

The name of this plugin. This must be unique, and is pretty self-explanatory.

### version

The current version of this plugin. This MUST be semver compatible, and is compared to any installed versions of this plugin on the user's device.

### oldNames

This is an array of any old names that this plugin may have. This is added to allow a plugin to be renamed if necessary. If the updater finds an installed plugin in this array, it will handle that appropriately and use the new name of the plugin.

## Example
```json
[
    {
        "name": "MessageTranslate",
        "version": "0.4.2",
        "oldNames": ["Translator"]
    },
    { 
        "name": "AccountSwitcher",
        "version": "3.6.2",
        "oldNames": []
    }
]
```