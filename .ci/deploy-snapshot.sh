#!/usr/bin/env bash

# Find project version
ver=$(./gradlew -q :controlsfx:getVersion | tail -n 1) 

# deploy if snapshot found
if [[ $ver == *"SNAPSHOT"* ]] 
then
    ./gradlew uploadPublished -PsonatypeUsername=$SONATYPE_USERNAME -PsonatypePassword=$SONATYPE_PASSWORD
fi