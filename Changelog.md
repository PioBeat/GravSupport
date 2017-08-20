# Changelog

## Todo
- Project Wizard: Create new grav project from scratch
    - https://stackoverflow.com/questions/25951117/how-do-i-register-a-new-module-type-in-an-intellij-plugin
	- define release url in settings menu
- Manage versions of Grav downloads in Settings menu

- language file editor (languages directory in a theme is responsibly)
    - Option to display sorted or in given order
    - edit values directly in the table
    - remove entries via button
    - info message about duplicate key entries
    - jump to entry in language yaml file via context menu or shortcut
    - general improvements for displaying sequences and mappings
    - add missing key/value pairs in other language files

- Twig support and helpers
    - convert HTML comments in twig comments
    - convert css/js href attributes to grav specific links

- add more features to application settings
    - enable/disable if src directory should be created
    - specify download link for grav (currently hardcoded in a properties file)

- internationalizing the plugin, extract string resources to resource bundle

## [0.2] - 2017-08-20

### Added

- basic project settings dialog in IntelliJ settings menu for a project
- improvements for Grav "Plugin and Theme Language Translations" files:
language file editor supports now ``languages.yaml`` file in theme directory
(see [https://learn.getgrav.org/content/multi-language#plugin-and-theme-language-translations](https://learn.getgrav.org/content/multi-language#plugin-and-theme-language-translations)
to check what options you have for a multi-language website)
- change language in _add-dialog_ of the language file editor
- better viewing experience for language file editor by adding a horizontal scrollbar

### Bugfix
- smaller bugfixes regarding the language file editor
- solve problems opening the language files in the system directory
- actions now visible in PhpStorm
- install Devtools plugin when creating new project

## [0.1-beta3] - 2017-08-17

### Added
- language file editor (works only properly for theme language directory not inside the system directory)
    - Language selection in "Add new key value pair" dialog
    - Pretty print for sequence values
- PhpStorm support
    - create new Grav project (File -> New Project ...)

### Bugfix
- colors are now set correctly when values are missing in the language file editor

## [0.1-beta] - 2017-08-12

Initial project start

Grav version support: 1.3.1

### Features

- Create new theme from Tools Menu or when a module is selected (Right mouse button -> New -> Grav Create Theme)
A dialog opens and then a theme will be created with the details provided
 and the help of the plugin script (bin/plugin)

- Create ``blueprints.yaml`` and ``<theme-name>.yaml`` when right clicking on a
theme directory

- basic language file editor for a theme language directory.
The language directory with its language yaml file is responsible for this
    - Tabular overview of all keys for different languages possible
    (no editing yet in the table)
    - Comfortable switching between all languages through the tabs at
    the bottom
    - add a new key-value pair through a dialog

- Settings menu (application wide)
    - set default directory for Grav downloads

- Icons and recognition of grav specific config files

### Issues

- if the language file editor shows wrong results try to close
and reopen it.
If no language file editor is shown: this is because a language yaml
is opened. Close it and reopen the file.
If the language file editor is opened, manually made changes in the
languages files are not updated automatically in the table. Close and reopen
the editor.


