# aliucord.yml

This file, unlike all of the other ones defined here, should be in the repository with your plugin. It defines two things: the command to build your plugin, and the output path where the plugin can be found after building. This allows CI to automatically build your plugin from the source code.

## General structure

```yml
buildCommand: Command that will build your plugin here
outputPath: Relative output path of the built plugin here
```

## Properties

> **Note** |
> In all of the below properties, if `{{plugin}}` is present, it will be replaced with the plugin's name when building. For example, `pnpm build {{plugin}}` would be replaced with `pnpm build MessageTranslate`.

### buildCommand

The command that should be used to build the plugin. This can be anything, as long as it ends up building the plugin.

### outputPath

The relative output path (from the repository root) that the built plugin will be located at.

## Example

```yml
buildCommand: pnpm build {{plugin}}
outputPath: dist/{{plugin}}.zip
```