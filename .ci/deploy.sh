#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

# Configure GIT
git config --global user.name "Abhinay Agarwal"
git config --global user.email "abhinayagarwal@live.com"

openssl aes-256-cbc -K $encrypted_ad8c90635771_key -iv $encrypted_ad8c90635771_iv -in .ci/sonatype.gpg.enc -out sonatype.gpg -d
if [[ ! -s sonatype.gpg ]]
   then echo "Decryption failed."
   exit 1
fi

./gradlew publish -PsonatypeUsername=$SONATYPE_USERNAME -PsonatypePassword=$SONATYPE_PASSWORD \
-Psigning.keyId=$GPG_KEY_ID -Psigning.password=$GPG_KEY_PASSPHRASE -Psigning.secretKeyRingFile=/home/travis/build/controlsfx/controlsfx/sonatype.gpg \
-Dorg.gradle.internal.publish.checksums.insecure=true

# Update to next development version
newVersion=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))
sed -i "0,/^controlsfx_version = $TRAVIS_TAG/s//controlsfx_version = $newVersion-SNAPSHOT/" gradle.properties

branch="jfx-13"
if [[ $TRAVIS_TAG = 8* ]]; then
  branch="master"
elif [[ $TRAVIS_TAG = 11.0.* ]]; then
  branch="9.0.0"
fi
git commit gradle.properties -m "Upgrade version to $newVersion-SNAPSHOT"
git push https://abhinayagarwal:$GITHUB_PASSWORD@github.com/$TRAVIS_REPO_SLUG HEAD:$branch

# Update Javadoc
if [[ $branch = "jfx-13" ]]; then
  bash .ci/update-javadoc.sh "$TRAVIS_TAG"
fi