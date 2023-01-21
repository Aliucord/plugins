# {plugin}/manifest.json

While this file can differ slightly from plugin to plugin, it MUST have the required properties (with the correct type)
listed below.

- `name` -> A name, ONLY matching `[a-zA-Z]`
- `description` -> A **short** description
- `version` -> A [SemVer](https://semver.org/) version
- `authors` -> (optional) An array of `{ id?: "optional string id", name: "string name" }`
- `license` -> (optional) An SPDX license identifier
- `changelog` -> (optional | null) Markdown string changelog
- `url` -> (NOT ALLOWED) Update url for 3rd party plugins

Sample file (may differ):

```json
{
  "authors": [
    {
      "id": "780819226839220265",
      "name": "John"
    }
  ],
  "license": "MIT",
  "version": "1.0.0",
  "description": "Hides call buttons in the user popout menu.",
  "name": "HideCallButtons"
}
```
