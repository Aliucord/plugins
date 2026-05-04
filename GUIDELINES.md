# Plugin Guidelines

This document outlines the various rules and guidelines to developing an official Aliucord plugin,
submitting it to this official plugin repository, and maintaining it afterward.

After the centralization of plugin completes, the existing decentralized plugin repositories hosted
by the respective authors will become "deprecated". All official plugin development, review, and
distribution will occur through this repository. All plugins that have been centralized will be
forcefully updated to their central repository variant. Any plugins not built from this repository
will be marked as "unofficial" and the installation of such plugin will be gated behind a warning.

## Rules

All new and existing plugin must adhere to the following list of rules in order to be considered for distribution.
Note that all plugins are also subject to the discretion of maintainers and staff. Exceptions may be given or enforced
in special circumstances.

1. Plugins that includes malware, authentication stealing code, or other harmful behavior are prohibited.
2. Plugins that break Discord's Terms of Service (excluding the client-modding section) are prohibited.
3. Plugins that are used to advertise or create spam are prohibited.
4. Plugins that track or stalk other users are prohibited.
    - This excludes plugins that notify you of status changes to your friends list.
5. Joke/meme plugins that break Aliucord or do not have a reasonably useful purpose are not allowed. See rule #9 for
   clarification.
    - April Fools flashbangs are not allowed
6. Plugins whose behavior is already fully or partially implemented in an existing plugin (duplicate) are not allowed!
   Consider contributing to the existing plugin instead!
7. Plugins that abuse Discord's API to automate user actions in the style of a selfbot are prohibited. Examples include:
    - Mass deletion of messages, channels, roles, servers, etc.
    - Auto-reply AFK messages
    - Cloning servers
    - Animated custom user statuses
    - Auto forwarding messages
    - Mass DM'ing users
    - Mass pinging server members (`@everyone` bypasses)
    - Scraping messages, members, channels, servers, etc.
    - Sending automated friend requests (e.g. friends list importers)
    - Automating any user action in perpetuity
8. Plugins with limited user automation (as long as the user is actively present) may be allowed under prior
   consultation with staff. Examples include:
    - Splitting long messages into multiple messages (with a limit to prevent spam)
    - Uploading large attachments to external services and sending a link instead
9. Plugins that have a niche purpose must be tagged as "Fun". Examples include:
    - Transforming images with an external service (ex: petpet)
    - Sending a random image of XYZ with a slash command (ex: https://minky.materii.dev)
    - Sending info in chat from an external service with a slash command (this should be a Discord bot, not a plugin!)
10. All plugin submissions are subject to the discretion of staff to ensure safety, security, and quality for users.

## Submissions

TODO

## Maintenance

TODO
