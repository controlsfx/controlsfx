#!/usr/bin/env bash

openssl aes-256-cbc -K $encrypted_ad8c90635771_key -iv $encrypted_ad8c90635771_iv -in .ci/sonatype.gpg.enc -out sonatype.gpg -d
./gradlew uploadPublished --info -PsonatypeUsername=$SONATYPE_USERNAME -PsonatypePassword=$SONATYPE_PASSWORD -Psigning.keyId=$GPG_KEY_ID -Psigning.password=$GPG_KEY_PASSPHRASE -Psigning.secretKeyRingFile=sonatype.gpg