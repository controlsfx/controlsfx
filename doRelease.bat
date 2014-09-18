@echo off
echo ControlsFX Release Tool
echo =======================
echo.

echo Step 1: In the root build file edit artifact_suffix to remove the -SNAPSHOT text.
echo.
pause

echo.
echo Step 2: Building projects...
echo.
call gradle -b clean assemble install

echo.
echo Success - all projects built!
pause

echo.
echo Step 3.1: Copy new javadocs from controlsfx/build/docs/javadoc to ../controlsfx-javadoc directory
echo.
pause

echo.
echo Step 3.2: Copying samples source code from controlsfx-samples/src/main/java to ../controlsfx-javadoc/samples-src directory
rmdir /S /Q ..\controlsfx-javadoc\samples-src
xcopy controlsfx-samples\src\main\java ..\controlsfx-javadoc\samples-src /E /I
echo.
pause

echo.
echo Step 4: Commit, tag and push the javadocs to the repo
echo.
pause

echo.
echo Step 5: Test that ControlsFX-samples can load the javadoc and source tab for all samples. If not, update the URLs in the samples and rebuild the jar files. 
echo.
pause

echo.
echo Step 6: Maven time!
echo Step 6.1: Pushing to Maven Central
echo.
call gradle -b controlsfx/build.gradle uploadPublished
echo.
call gradle -b fxsampler/build.gradle uploadPublished
echo.
echo Step 6.2: Go to Maven Central to publish the jars (https://oss.sonatype.org, then Staging Repositories, find release, select and 'close', then 'release')
echo.

echo Step 7: Edit the root build file to add back in the -SNAPSHOT text.
echo.
pause

echo Step 8: Tag the repo with the version number.
echo.
pause

echo Step 9: Create a zip file containing the controlsfx jar, the controlsfx-samples jar, the fxsample jar, and the license.txt file.
echo.
pause

echo Step 10: Push zip file to download location
echo.
pause

echo Step 11: Update root build file with version numbers to be the next version with -SNAPSHOT.
echo.
pause

echo Step 12: Update bitbucket readme.md and controlsfx.org to refer to new version number
echo.

echo Step 11: Post blog post to controlsfx.org
echo.