# Cloud Vision Android Sample

So you want to use the Cloud Vision API from Android?
Then this sample is for you. It's a minimal single-activity
sample that shows you how to make a call to the Cloud Vision
API with an image picked from your device's gallery.

## Prerequisites
- An API key for the Cloud Vision API (See
  [the docs](https://cloud.google.com/vision/docs/getting-started) to learn more)
- An Android device running Android 5.0 or higher

## Setup
**Note:** The Cloud Vision API uses a generated client library. It will be available
soon via Gradle, but until then we must build it from source, and copy it
into our project. So, thanks for your understanding as an early adopter.

-  Open `MainActivity` and replace `CLOUD_VISION_API_KEY` with your
   Cloud Vision api key.
-  Go to the Cloud Vision library in `../java/google-api-services-vision`.
-  Build and package the library.

```
$ mvn package
```
-  Find the built library jar and copy it into the `libs` folder of the Android app.

```
$ cd target
$ mkdir -p ../../../android/CloudVision/app/libs
$ cp google-api-services-vision-v1-rev20160203-1.21.0.jar ../../../android/CloudVision/app/libs
```
- Go to the app's `app/build.gradle` file and fix the local dependency
   to match the file name of the jar file that you just copied into `libs`. The
   version number and revision change over time.

```
  dependencies {
      ...
      compile files('libs/google-api-services-vision-v1-rev20160203-1.21.0.jar')
      ...
  }
```
- Build and run the app from Android Studio. Recognize stuff.
- Profit!
