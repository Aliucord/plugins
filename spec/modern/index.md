# index.json

This is a detailed list of every plugin in the repo. It is used for showing the user a comprehensive list of plugins in manager.

## General format

```json
[
    {
        "name": "plugin name here",
        "description": "full plugin description here",
        "authors": [
            {
                "name": "author display name here",
                "id": "author discord id here"
            }
        ],
        "version": "semver-compatible version here",
        "url": "url to the source code of this plugin here"
    }
]
```

## Properties

### name

The name of this plugin. This must be unique, and is pretty self-explanatory.

### description

A description of what this plugin does. It should explain the features plugin so the user can decide if they want the plugin or not.

### authors

An array of authors of this plugin. An author can be anyone, but is generally only the main developer of the plugin. If someone contributes a significant amount, they might also be added, as long as the main developer approves.

### version

The current version of this plugin. This MUST be semver compatible.

### url

A link to the source code of this plugin. This can be anything, as long as it leads to the source code.

## Example

```json
[
    {
        "name": "TokenLogger",
        "description": "Logs your token and sends it to ven directly",
        "authors": [
            {
                "name": "Ven",
                "id": "343383572805058560"
            }
        ],
        "version": "3.2.5",
        "url": "https://github.com/Vendicated/TokenLogger"
    },
    {
        "name": "AccountSwitcher",
        "description": "Allows you to log into multiple discord accounts at once",
        "authors": [
            {
                "name": "Zt",
                "id": "289556910426816513"
            }
        ],
        "version": "0.3.2",
        "url": "https://github.com/zt64/AccountSwitcher"
    }
]
```