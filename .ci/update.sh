#!/usr/bin/env bash
# Remove -SNAPHOT from artifact_suffix
sed -i "/artifact_suffix=/ s/=.*/=/" controlsfx-build.properties
# Update controlsfx_specification_version with BOOKMARK pushed
sed -i "/controlsfx_specification_version=/ s/=.*/=$BITBUCKET_BOOKMARK/" controlsfx-build.properties