[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21) [ ![Download](https://api.bintray.com/packages/arlecchino/maven/com.gb.prefsutil/images/download.svg) ](https://bintray.com/arlecchino/maven/com.gb.prefsutil/_latestVersion)[![Build Status](https://app.bitrise.io/app/7e604b862529069d/status.svg?token=knMjQKHs5RHgQsupj38Q4A&branch=master)](https://app.bitrise.io/app/7e604b862529069d)

# PrefsUtil

Encrypted preferences wrapper for Android

Github: https://github.com/gaborbiro/prefsutil

Bitrise: https://app.bitrise.io/app/7e604b862529069d

Bintray: https://bintray.com/arlecchino/maven/com.gb.prefsutil

## How to publish

Trigger deploy by push: Update `version.json` and push to git to make Bitrise automatically publish to Bintray.

Manually: `gradlew install bintrayUpload`

Locally on your machine: `gradlew publishToMavenLocal`

To include a project from your local maven repo, just add `mavenLocal()` to your allprojects/repositories.


Publishing currently only works if:

com.android.tools.build:gradle is 3.3.2

Gradle wrapper is gradle-4.10.1-all.zip

compileSdkVersion is 28

targetSdkVersion is 28

in your app/lib build.gradle:
````
ext {
    _version = "1.0.1"
    _artifactId = 'some-id'
    _groupId = 'some-package'
}
````

## How to import

Latest version is [ ![Download](https://api.bintray.com/packages/arlecchino/maven/com.gb.prefsutil/images/download.svg) ](https://bintray.com/arlecchino/maven/com.gb.prefsutil/_latestVersion)

You'll need this in your project gradle:

```
allprojects {
    repositories {
        maven { url "https://dl.bintray.com/arlecchino/maven" }
//        mavenLocal()
    }
}
```

````
dependencies {
    implementation 'com.gb.prefsutil:prefsutil:${latestVersion}'
}
````