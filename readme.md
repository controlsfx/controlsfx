[
![Ohloh project report for ControlsFX](https://www.ohloh.net/p/controlsfx/widgets/project_thin_badge.gif)
](http://www.ohloh.net/p/controlsfx?ref=sample)

[![Build Status](https://drone.io/bitbucket.org/controlsfx/controlsfx/status.png)](https://drone.io/bitbucket.org/controlsfx/controlsfx/latest)

ControlsFX is an [open source project][1] for JavaFX that aims to provide really high quality UI controls and other tools to complement the core JavaFX distribution. It has been developed for JavaFX 8.0 and beyond, and has a guiding principle of only accepting new controls / features when all existing code is at an acceptably high level, including thankless jobs like having high quality javadoc documentation. This ensure a high quality release is available at all times, with all experimental work being done in branches of the main code base.


> **Important note: **ControlsFX will only work on JavaFX 8.0 b110 or later. If you are running on earlier versions of JDK 8, please [upgrade][8]. If you are still using JavaFX 2.x then unfortunately this library will not work for you.

## Getting Started

If you want to **play with** the ControlsFX sample application, either clone the ControlsFX repo from bitbucket and then run the [org.controlsfx.HelloControlsFX][10] application that resides within the [src/samples/java][11] directory, or run the samples jar file from the command line with the following command (or a variation depending on your operating system):

*java -cp controlsfx-8.0.2.jar:controlsfx-8.0.2-samples.jar org.controlsfx.HelloControlsFX*


If you think you have a feature **you can contribute**, a bug you want to fix, or have a bug youd like to file, please direct it to the [issue tracker over at the ControlsFX bitbucket website][12].

If you **want to use** ControlsFX, then youre probably wondering how to use the API that weve slaved over for hours! In your case, the best and definitive location for help is definitely [our javadocs][13]. Fear not, these are not a barren wasteland of undescribed functionality weve poured our hearts into making these javadocs full to the brim of examples, commentary and explanation. If anything is unclear to you, [file a bug in our issue tracker][12] and well do our best to update the documentation straight away!

If you *have questions*, you should join the the [mailing list][22].

Another great place to explore is our **sample code**, all located in the [src/samples directory][14] of our [code repository][15]. Weve put a bunch of effort into making these examples something you can learn from.

## Release Versioning

ControlsFX has a slightly different approach to version numbers than other projects. We use the fairly traditional x.y.z numbering system, except in the case of ControlsFX, the x.y portion is used to represent the base JavaFX version required. In other words, ControlsFX 8.0.0 is the first release of ControlsFX to work on JavaFX 8.0 and above. ControlsFX 8.1.5 (if we ever release that version) is used to represent that the release will work on JavaFX 8.1 (and not JavaFX 8.0), and is the 6th release (remember: real programmers count from zero ![:-\)][16] ).

In other words, we do not differentiate between bug fix and feature releases we will clarify what the release contains on a per-release basis. Also, we will always aim to require the minimal version of JavaFX possible, but we are also not going to hold back features / bug fixes / etc if we can resolve them by moving to a newer release. Prior to moving up to a new JavaFX version we will always tag the [repository][17] and make available a download of the source and compiled code for the earlier version, for people who are unable to move to the newer JavaFX release.

## Downloads 

The library can be downloaded from from [ControlsFX website][21]. 
It is possible to download the current release (8.0.2), or also current development snapshots from Maven Central using the following dependency settings:

###### Maven
    :::html
    <dependency>
       <groupId>org.controlsfx</groupId>
       <artifactId>controlsfx</artifactId>
       <version>8.0.3-SNAPSHOT</version>
    </dependency>

###### Gradle
    :::groovy
    dependencies {
       runtime 'org.controlsfx:controlsfx:8.0.3-SNAPSHOT'
    }
    
###### Ivy
    :::html 
    <dependency org="org.controlsfx" name="controlsfx" rev="8.0.3-SNAPSHOT"/>
    
###### Sbt
    :::scala 
    libraryDependencies += "org.controlsfx" % "controlsfx" % "8.0.3-SNAPSHOT"   

## License

ControlsFX is licensed under the [3-Clause BSD license][18]. We are not lawyers, but our interpretation of this license suggests to us that it is business friendly, requiring only the redistribution of the [3-clause BSD license we distribute with ControlsFX][19]. As always, I suggest you review the license with the appropriate people, rather than take the advice of software engineers. If this license is not suitable, please contact [Jonathan Giles][20] to discuss an alternative license.


   [1]: http://controlsfx.org
   [8]: http://jdk8.java.net/download.html
   [10]: http://code.controlsfx.org/src/ba2f89a26ff4b87ae04f80135c47d204d82efdee/src/samples/java/org/controlsfx/HelloControlsFX.java?at=default
   [11]: http://code.controlsfx.org/src/ba2f89a26ff4/src/samples/java?at=default
   [12]: http://issues.controlsfx.org
   [13]: http://docs.controlsfx.org
   [14]: http://code.controlsfx.org/src/ba2f89a26ff4b87ae04f80135c47d204d82efdee/src/samples/java/org/controlsfx?at=default
   [15]: http://code.controlsfx.org/src/ba2f89a26ff4?at=default
   [16]: http://cache.fxexperience.com/wp-includes/images/smilies/icon_smile.gif
   [17]: http://code.controlsfx.org
   [18]: http://opensource.org/licenses/BSD-3-Clause
   [19]: http://code.controlsfx.org/src/e01d9073145a352db1562baf6ea7297d5c37d6a1/license.txt?at=default
   [20]: mailto:jonathan%40jonathangiles.net
   [21]: http://www.controlsfx.org
   [22]: http://groups.controlsfx.org