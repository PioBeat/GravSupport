# Changelog

## [UNRELEASED]

### Added
- Language file editor
    - remove key in all languages (right click on the first column -> _Delete_)
    - if you add a key when a specific language is opened via the tabbedpane
     then the dialog is using this language as preset

### Changed
- used ``SingleRootFileViewProvider`` in ``LanguageFileViewProvider.java``
instead of ``MultiplePsiFilesPerDocumentFileViewProvider`` (Closes issue #3)

## [0.3.2] - 2017-08-28

- Simply switch to the template from a content page. The filename of
the <code>.md</code> file is evaluated as well as the <code>template</code>
variable in the header of the markdown file. You will find an Twig icon
on the gutter and provides navigation to the related template files.


## [0.3.1] - 2017-08-25
Minor update

### Bugifx
-  no duplicate Grav entries anymore in the _New Project Wizard_

## [0.3] - 2017-08-24

### Added
- Project Wizard
    - select Grav release version when downloading Grav in the project wizard
    - decide if you want to create a ``src/test`` directory or not within the
    project base dir
- **New** Twig intention
    - convert HTML ``src`` and ``href`` links into _theme_ resource links <br/>
    *Example* <br/>
    Before: ``<img src="assets/img/picture.png"/>``<br/>
    After: ``<img src="{{ url("theme://assets/img/picture.png") }}"/>``<br/>
- **New** IntelliJ tool window for displaying and configuring basic
Grav system files. Currently supported is only ``system/config/system.yaml``.
This window offers an overview of the possible settings of those
config files. <br/>
The relevant component of the tool window will be disabled if a settings
value couldn't be found in the config file. <br/> <br/>
**Hint** the tool window shows the settings only for the first opened project.
No multi project support for this version available

## [0.2] - 2017-08-20

### Added

- basic project settings dialog in IntelliJ settings menu for a project
- improvements for Grav "Plugin and Theme Language Translations" files:
language file editor supports now ``languages.yaml`` file in theme directory
(see [https://learn.getgrav.org/content/multi-language#plugin-and-theme-language-translations](https://learn.getgrav.org/content/multi-language#plugin-and-theme-language-translations)
to check what options you have for a multi-language website)
- change language in _add-dialog_ of the language file editor
- better viewing experience for language file editor by adding a horizontal scrollbar
- PhpStorm: include path of Grav is added to the PHP settings
- projects settings: added information about src/test directory

### Bugfix
- smaller bugfixes regarding the language file editor
- solve problems opening the language files in the system directory
- actions now visible in PhpStorm
- install Devtools plugin when creating new project
- ``GravProjectSettings`` is now correctly acquired via ``ServiceManager`` when
Grav Php project is created

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

## Issues

- if the language file editor shows wrong results try to close
and reopen it.
If no language file editor is shown: this is because a language yaml
is opened. Close it and reopen the file.
If the language file editor is opened, manually made changes in the
languages files are not updated automatically in the table. Close and reopen
the editor.


