ControlsFX
=====

ControlsFX is an open source project for JavaFX that aims to provide really high quality UI controls and other tools to complement the core JavaFX distribution. It has been developed for JavaFX 8.0 and beyond, and has a guiding principle of only accepting new controls / features when all existing code is at an acceptably high level, including thankless jobs like having [high quality javadoc documentation](http://docs.controlsfx.org). This ensure a high quality release is available at all times, with all experimental work being done in branches of the main code base.

[![Maven Central](http://img.shields.io/maven-central/v/org.controlsfx/controlsfx.svg?color=%234DC71F)](https://search.maven.org/#search|ga|1|org.controlsfx.controlsfx)
[![BSD-3 license](https://img.shields.io/badge/license-BSD--3-%230778B9.svg)](https://opensource.org/licenses/BSD-3-Clause)

## Features

ControlsFX includes so many features that we're now listing them on a separate page! Go to the [ControlFX features](features/features.md) page to see some of what is included in ControlsFX.

## Quick links

- [JavaDoc](http://docs.controlsfx.org)
- [Mailing List](https://groups.google.com/group/controlsfx-dev)
- [Bug / Feature Tracking](https://github.com/controlsfx/controlsfx/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc)

## Getting ControlsFX

- For users of Java 8, download [ControlsFX 8.40.15](https://repo.maven.apache.org/maven2/org/controlsfx/controlsfx/8.40.15/controlsfx-8.40.15.jar).
- For users of Java 9 and above, download [ControlsFX 11.0.0](https://repo.maven.apache.org/maven2/org/controlsfx/controlsfx/11.0.0/controlsfx-11.0.0.jar).

## Getting Started

We have two important branches: `master` for JavaFX 8 and `9.0.0` for JavaFX 9+.

If you **want to play** with the ControlsFX sample application, clone the repository, checkout the branch of your choice and run:

`./gradlew run`

If you have a feature **you can contribute**, a bug you want to fix, or have a bug you'd like to file, please direct it to the [issue tracker](https://github.com/controlsfx/controlsfx/issues). You can build ControlsFX locally like this:

`./gradlew build check`

If you **want to use** ControlsFX, then you're probably wondering how to use the API that we've slaved over for hours! In your case, the best and definitive location for help is definitely [our javadocs](http://docs.controlsfx.org). Fear not, these are not a barren wasteland of undescribed functionality - we've poured our hearts into making these javadocs full to the brim of examples, commentary and explanation. If anything is unclear to you, [file a bug in our issue tracker](https://github.com/controlsfx/controlsfx/issues) and we'll do our best to update the documentation straight away!

If you **have questions**, you should join the the [mailing list](https://groups.google.com/group/controlsfx-dev).

Another great place to explore is our **sample code**, all located in the controlsfx-samples directory of this source code repository. We've put a bunch of effort into making these examples something you can learn from.

## Contributing

Please feel free to report issues. If you want to submit a bug fix or an enhancement request, please make sure to go through [Contributing to ControlsFX](https://github.com/controlsfx/controlsfx/wiki/Contributing-to-ControlsFX) before creating a PR.

## Thanks To

| Company              | For..                          |
|----------------------|--------------------------------|
| <a href="http://gluonhq.com"><img width="200" src="http://fxexperience.com/wp-content/uploads/2016/08/Gluon_combined_logo_vertical.png"></a>| For substantial contributions towards project hosting and development of major features and improvements.|
|<img width="200" src="http://fxexperience.com/wp-content/uploads/2013/04/jetbrains.png">| For the <a href="https://www.jetbrains.com/idea">IntelliJ IDEA</a> licenses.|
