#!/usr/bin/env bash

REPO_SLUG=controlsfx/javadoc

cd $TRAVIS_BUILD_DIR
git clone https://github.com/$REPO_SLUG
cd javadoc

mkdir $1
cp -r $TRAVIS_BUILD_DIR/controlsfx/build/docs/javadoc/* $1

# If tag is not 8.x.x
if [[ $1 != 8* ]]
then
    # Update index page
    sed -i "s/url=.*\/index.html/url=$1\/index.html/g" index.html
    git add index.html
fi

git add $1
git commit -m "Add Javadoc for $1"
git push https://abhinayagarwal:$GITHUB_PASSWORD@github.com/$REPO_SLUG HEAD:master

# Re-deploy Javadoc Github pages
curl -u abhinayagarwal:$ACCESS_TOKEN -X POST https://api.github.com/repos/controlsfx/javadoc/pages/builds