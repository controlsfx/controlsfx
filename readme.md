ControlsFX
=====

ControlsFX is an open source project for JavaFX that aims to provide really high quality UI controls and other tools to complement the core JavaFX distribution. It has been developed for JavaFX 8.0 and beyond, and has a guiding principle of only accepting new controls / features when all existing code is at an acceptably high level, including thankless jobs like having [high quality javadoc documentation](http://docs.controlsfx.org). This ensure a high quality release is available at all times, with all experimental work being done in branches of the main code base.

## Features

ControlsFX includes so many features that we're now listing them on a separate page! Go to the <a href="http://fxexperience.com/controlsfx/features">ControlsFX features</a> page to see some of what is included in ControlsFX.

## Quick links

- [JavaDoc](http://docs.controlsfx.org)
- [Mailing List](https://groups.google.com/group/controlsfx-dev)
- [Bug / Feature Tracking](https://github.com/controlsfx/controlsfx/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc)

## Getting ControlsFX

- For users of Java 8, download [ControlsFX 8.40.14](http://fxexperience.com/downloads/controlsfx-8-40-14/).
- For users of Java 9, download [ControlsFX 9.0.0](http://fxexperience.com/downloads/controlsfx-9-0-0/).
- For users of Java 11 and later, download will be coming soon!

ControlsFX ships as a single zip file which contains the library jar as well as a samples jar. Documentation, which used to be included in the single zip file, is now [available online](http://docs.controlsfx.org). The JavaDoc jar is also available at [Maven Central](https://oss.sonatype.org/content/repositories/releases/org/controlsfx/controlsfx/).

## Maven Central

Latest version published in Maven Central (click for more details):
<a href="https://maven-badges.herokuapp.com/maven-central/org.controlsfx/controlsfx"><img src="http://img.shields.io/maven-central/v/org.controlsfx/controlsfx.svg?style=flat"></a>

## Getting Started / Contributing

If you **want to play** with the ControlsFX sample application, simply download the ControlsFX release and run the following command on the command prompt (be sure to replace the * with the actual version number of FXSampler and ControlsFX-samples):

`java -jar controlsfx-samples-*.jar`

If you think you have a feature **you can contribute**, a bug you want to fix, or have a bug you'd like to file, please direct it to the [issue tracker](https://github.com/controlsfx/controlsfx/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc). You can build ControlsFX locally like this:

`bash ./gradlew build check`

If you **want to use** ControlsFX, then you're probably wondering how to use the API that we've slaved over for hours! In your case, the best and definitive location for help is definitely [our javadocs](http://docs.controlsfx.org). Fear not, these are not a barren wasteland of undescribed functionality - we've poured our hearts into making these javadocs full to the brim of examples, commentary and explanation. If anything is unclear to you, [file a bug in our issue tracker](https://github.com/controlsfx/controlsfx/issues?q=is%3Aissue+is%3Aopen+sort%3Aupdated-desc) and we'll do our best to update the documentation straight away!

If you **have questions**, you should join the the [mailing list](https://groups.google.com/group/controlsfx-dev).

Another great place to explore is our **sample code**, all located in the controlsfx-samples directory of this source code repository. We've put a bunch of effort into making these examples something you can learn from.

## Release Versioning

ControlsFX has a slightly different approach to version numbers than other projects. We use the fairly traditional x.y.z numbering system, except in the case of ControlsFX, the x.y portion is used to represent the base JavaFX version required. In other words, ControlsFX 8.0.0 is the first release of ControlsFX to work on JavaFX 8.0 and above. ControlsFX 8.1.5 (if we ever release that version) is used to represent that the release will work on JavaFX 8.1 (and not JavaFX 8.0), and is the 6th release (remember: real programmers count from zero :-) ).

In other words, we do not differentiate between bug fix and feature releases - we will clarify what the release contains on a per-release basis. Also, we will always aim to require the minimal version of JavaFX possible, but we are also not going to hold back features / bug fixes / etc if we can resolve them by moving to a newer release. Prior to moving up to a new JavaFX version we will always tag the <a href="http://code.controlsfx.org">repository</a> and make available a download of the source and compiled code for the earlier version, for people who are unable to move to the newer JavaFX release.artifact_suffix

## Thanks To

| Company              | For..                          |
|----------------------|--------------------------------|
| <a href="http://gluonhq.com"><img width="200" src="http://fxexperience.com/wp-content/uploads/2016/08/Gluon_combined_logo_vertical.png"></a>| For substantial contributions towards project hosting and development of major features and improvements.|
|<img width="200" src="http://fxexperience.com/wp-content/uploads/2013/04/jetbrains.png">| For the <a href="https://www.jetbrains.com/idea">IntelliJ IDEA</a> licenses.|
