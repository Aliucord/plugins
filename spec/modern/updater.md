# updater.json

This is a very simple file, used only by the aliucord updater, to quickly check if any installed plugins are outdated.

## General format
```json
{
    "plugin_name_here_name": {
        "version": "semver-compatible version here"
    },
    "old_plugin_name_here": {
        "newName": "plugin_name_here_name"
    }
}
```

## Properties

### name

The name of this plugin. This must be unique, and is pretty self-explanatory. This is used as the key for ONE OF the below properties. Look at the example below for a better explanation. When trying to find if an installed plugin is outdated, you should simply index this object with the name of the plugin.

### version (optional, if no newName)

The current version of this plugin. This MUST be semver compatible, and is compared to any installed versions of this plugin on the user's device. This property will be present only when the plugin name in the key is not an outdated name. For example, if "Translator" is an old name of "MessageTranslate", then 
```js
updaterJson["MessageTranslate"].version // "3.2.3"
// BUT when an old name is used:
updaterJson["Translator"].version // undefined
```

### newName (optional, if no version)

This property, if present, means that this plugin has a new name and should be renamed appropriately. When present, the updater should use this to find the version by looking up the updated name, and handle that appropriately. This is the opposite of `version`, and will only be present if an old name is used. For example, if "Translator" is an old name of "MessageTranslate", then 
```js
updaterJson["MessageTranslate"].newName // undefined
// BUT when an old name is used:
updaterJson["Translator"].version // "MessageTranslate"
```

## Example
```json
{
    "MessageTranslate": {
        "version": "3.2.3"
    },
    "Translator": {
        "newName": "MessageTranslate"
    },
    "AccountSwitcher": {
        "version": "0.3.6"
    }
}
```
