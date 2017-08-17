# Grav Support Plugin

This plugin for IntelliJ Community / Ultimate Edition and PhpStorm helps to increase the
development speed for Grav-based projects.

[Grav](https://getgrav.org/) is a flat-file CMS and it's open source.

Grav version support: 1.2.4 - 1.3.1

Tested with:
* IntelliJ IDEA Community 2017.2.1
* IntelliJ IDEA Ultimate 2017.2.1
* PhpStorm 2017.2.1

## Install

[Download the latest plugin](https://github.com/PioBeat/GravSupport/releases) and install it in the settings menu.
<kbd>CTRL+ALT+S</kbd> or _File -> Settings_
and then search for "Plugins" -> "Install plugins from disk"

## Features
(see the changelog for a complete list)

<ul>
        <li>Create SDK for Grav</li>
        <li>Create a Grav module or project (PhpStorm) using IntelliJ Wizard</li>
        <li>Ability to download and install Grav from the Module Wizard</li>
        <li>Create specific configuration files for a theme in the theme directory
        (blueprints.yaml, THEME_NAME.yaml)</li>
        <li>Recognition of basic Grav configuration files</li>
         <li>Create new theme (Tools menu or context menu on a module)</li>
         <li>Special localization file editor</li>
</ul>

## Requirements

The plugin requires IDEA Community / Ultimate Edition 2017.2.* or PhpStorm 2017.2.1.
With Ultimate you have also PHP support.

**Additional**

You need a valid PHP installation on your machine.
Php should be found in the ``PATH``.

The plugin uses the scripts in the ``bin`` directory of Grav to offer
the functionality Grav supports via those scripts.

Support for PhpStorm 2017.2.* is planned. Please see the Contributing
section.

## Help

**Supported Grav versions**

Currently it's only possible to automatically download _grav-admin-v1.3.1.zip_
directly via the project wizard of IntelliJ.
However, it's possible to use any other Grav installation by manually selecting them in
the wizard.

**"Grav plugin 'devtools' is not installed"**

If you get this error message: "Grav plugin 'devtools' is not installed"
after using the plugin and creating a new theme then you should follow these steps:

Move into your newly created Grav project and type into the console:

```
$ bin/gpm install devtools
```
References:

* [https://learn.getgrav.org/plugins/plugin-tutorial](https://learn.getgrav.org/plugins/plugin-tutorial)
* [https://github.com/getgrav/grav-plugin-devtools](https://github.com/getgrav/grav-plugin-devtools)



## Contribution

Support the plugin if you like it:
- Tell your friends who are using IntelliJ and Grav about this plugin
- Star it at GitHub
- Report bugs or feature requests or even fix / implement them
- Reach out to me directly at dominik@offbeat-pioneer.net

### Implement features or fix bugs
Fork this repository to your own GitHub account and then clone it to
your local device. Open a pull request with improvements.

Please read the [IntelliJ Platform SDK Documentation](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html)
on how to set up  the development environment.

### Reporting issue

If you find any issues, please report them directly by using the GitHub issue
tracker instead of review comments.


<!-- - Vote for it: Write your review and vote for it at the IntelliJ plugin repository. -->
