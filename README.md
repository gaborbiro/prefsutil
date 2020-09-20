[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21) [ ![Download](https://api.bintray.com/packages/arlecchino/maven/com.gb.prefsutil/images/download.svg) ](https://bintray.com/arlecchino/maven/com.gb.prefsutil/_latestVersion)

# Prefsutil

Encrytped preferences wrapper for Android

Github: https://github.com/gaborbiro/prefsutil

Bitrise: https://app.bitrise.io/app/7e604b862529069d

## Publish

Publish to mavenLocal: `gradlew publishToMavenLocal`

To include a project from your local maven repo, just add `mavenLocal()` to your allprojects/repositories


Publish to bintray: `gradlew install bintrayUpload` (or just push to github)

## To use

Latest version is [ ![Download](https://api.bintray.com/packages/arlecchino/maven/com.gb.prefsutil/images/download.svg) ](https://bintray.com/arlecchino/maven/com.gb.prefsutil/_latestVersion)

````
dependencies {
    implementation 'com.gb.prefsutil:prefsutil:${latestVersion}'
}
````

Make sure com.android.tools.build:gradle is 3.3.2

Gradle wrapper is gradle-4.10.1-all.zip

compileSdkVersion 28

targetSdkVersion 28

in your app/lib build.gradle:

````
ext {
    _version = "1.0.1"
    _artifactId = 'some-id'
    _groupId = 'some-package'
}
````