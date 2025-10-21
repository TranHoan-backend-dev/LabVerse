## Common Commit Types

The list of commonly used types and their meanings:

- feat: A new feature for the user or system.

  Example: feat: allow users to upload profile pictures

- fix: A bug fix that corrects an issue in the codebase.

  Example: fix: resolve alignment issue on the login button

- refactor: A code change that neither fixes a bug nor adds a feature.

  Example: refactor: simplify user service logic

- docs: Changes related to documentation only (e.g., updating README, adding API docs).

  Example: docs: update installation guide in README.md

- chore: Minor changes that don't affect production code (e.g., updating build scripts, package manager configs).

  Example: chore: add prettier configuration file

- style: Changes that do not alter the meaning of the code (e.g., white-space, formatting, missing semi-colons, CSS/UI changes).

  Example: style: format code according to project guidelines

- perf: A code change that improves performance.

  Example: perf: optimize database query for user dashboard

- vendor: Updates to third-party dependencies or packages.

  Example: vendor: upgrade react to version 18.3.0

## Naming branch

Using commit type like above

Regex: &lt;commit type&gt;/&lt;name&gt;-...-&lt;name&gt;

Example: feat/create-common-components