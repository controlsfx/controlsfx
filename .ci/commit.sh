#!/usr/bin/env bash
# Add back -SNAPHOT to artifact_suffix
sed -i "/artifact_suffix=/ s/=.*/=-SNAPSHOT/" controlsfx-build.properties
# Update version by 1
newVersion=${BITBUCKET_BOOKMARK%.*}.$((${BITBUCKET_BOOKMARK##*.} + 1))
sed -i "/controlsfx_specification_version=/ s/=.*/=$newVersion/" controlsfx-build.properties
# Commit back the new version
apt-get update && apt-get install mercurial -y
hg commit controlsfx-build.properties -m "Upgrading to next snapshot version" -u "ControlsFX Bot <controlsfxbot@jonathangiles.net>"
hg push https://$BITBUCKET_USERNAME:$BITBUCKET_PASSWORD@bitbucket.org/controlsfx/controlsfx
