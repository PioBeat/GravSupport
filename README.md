# Grav Support Plugin

This plugin for IntelliJ Community / Ultimate Edition and PhpStorm helps to increase the
development speed for Grav-based projects.

[Grav](https://getgrav.org/) is a flat-file CMS and it's open source.

Grav version support: 1.2.4 - 1.3.1

Tested with:
* IntelliJ IDEA Community 2017.2.1
* IntelliJ IDEA Ultimate 2017.2.1
* PhpStorm 2017.2.1

The plugin is available in the [JetBrains Plugins Repository](https://plugins.jetbrains.com/plugin/9971-grav-support)

## Install

**JetBrains Plugins Repository**

Open the settings menu <kbd>CTRL+ALT+S</kbd> or _File -> Settings_ and then
go to _Plugins_. Click on the button _Browse repositories_ and search for
**Grav Support**.
This way you will get notified if a new update is available!

**GitHub Release**

[Download the latest plugin](https://github.com/PioBeat/GravSupport/releases) and
install it via the settings menu: <kbd>CTRL+ALT+S</kbd> or _File -> Settings_
and then search for _Plugins -> Install plugins from disk_

**Configuration**

If you create a new Grav project / module you will have all features enabled by default.

If you're using PhpStorm and opening a previous project you may have to enable the plugin for the current project. The plugin
tries to detect a Grav project automatically and notifies the user accordingly. Otherwise
open the Settings menu (<kbd>CTRL+ALT+S</kbd>) and go to _Languages & Frameworks_ -> _Grav_ and
enable the plugin for the current project.

## Features
(see the changelog for a complete list)

<ul>
    <li>Create a Grav module (Community / Ultimate) or project (PhpStorm) using IntelliJ Wizard</li>
    <li>Ability to download and install Grav from the Module Wizard</li>
    <li>Special language file editor</li>
    <li>Tool window to configure basic config files of Grav</li>
    <li>Navigate to the related template file from a page content markdown file</li>
    <li>Create specific configuration files for a theme in the theme directory
        (<code>blueprints.yaml</code>, <code>THEME_NAME.yaml</code>)</li>
    <li>Recognition of basic Grav configuration files</li>
    <li>Create new theme (Tools menu or context menu on a module)</li>
    <li>Create SDK for Grav</li>
    <li>Twig intentions</li>
</ul>

## Screenshots

![New Project](.README_images/newproject.png)

![Tool Window](.README_images/toolwindow.png)


## Requirements

The plugin requires IDEA Community / Ultimate Edition 2017.2.* or PhpStorm 2017.2.1.
With Ultimate you have also PHP support.

**Additional**

You need a valid PHP installation on your machine.
Php should be found in the ``PATH``.

The plugin uses the scripts in the ``bin`` directory of Grav to offer
the functionality Grav supports via those scripts.

## Help

**Supported Grav versions**

Currently it's only possible to automatically download all listed Grav
version mentioned at the beginning of this Readme directly via the project wizard of IntelliJ.
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
- Start it on the [JetBrains Plugins Repository](https://plugins.jetbrains.com/plugin/9971-grav-support)
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
