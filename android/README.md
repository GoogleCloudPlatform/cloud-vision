# Cloud Vision Android Sample

So you want to use the Google Cloud Vision API from Android?
Then this sample is for you. It's a minimal single-activity
sample that shows you how to make a call to the Cloud Vision
API with an image picked from your device's gallery.

## Prerequisites
- An API key for the Cloud Vision API (See
  [the docs][getting-started] to learn more)
- An Android device running Android 5.0 or higher
- [Android Studio][android-studio], with a recent version of the Android SDK.

## Quickstart
- Download the `CloudVision` directory from this repository.
- In Android Studio, open the `CloudVision` directory as an existing Android
  Studio project.
- Open `MainActivity.java` and set the constant `CLOUD_VISION_API_KEY` to the
  API key obtained above.
- Run the sample.

## Running the app

- As with all Google Cloud APIs, every call to the Vision API must be associated
  with a project within the [Google Cloud Console][cloud-console] that has the
  Vision API enabled. This is described in more detail in the [getting started
  doc][getting-started], but in brief:
  - Create a project (or use an existing one) in the [Cloud
    Console][cloud-console]
  - [Enable billing][billing] and the [Vision API][enable-vision].
  - Create an [API key][api-key], and save this for later.

- Download the `CloudVision` directory from this repository.

    The easiest way to do this from GitHub is to fetch the entire repository.
    If you have [`git`][git] installed, you can do this by executing the
    following command:

        $ git clone https://github.com/GoogleCloudPlatform/cloud-vision.git

    This will download the repository of samples into the directory
    `cloud-vision`.

    Otherwise, GitHub offers an [auto-generated zip file][vision-zip] of the
    `master` branch, which you can download and extract.

    Either method will include the desired directory at
    `cloud-vision/android/CloudVision`.

- Open Android Studio, and navigate to open an existing project. When prompted,
  open this project's root directory `cloud-vision/android/CloudVision`.

- Within Android Studio, open the `MainActivity` java file within the app, and
  look for where the constant `CLOUD_VISION_API_KEY` is set. Replace the string
  value with the api key obtained from the cloud console above.

  This constant is the credential used in the `callCloudVision` method to
  authenticate all requests to the Vision API. Calls to the API are thus
  associated with the project you created above, for access and billing
  purposes.

- You are now ready to build and run the project. As per usual for Android
  Studio projects, you can run the project by clicking the green 'Play' button,
  or going to `Run` | `Run 'app'`. The app should build and run on your
  connected Android device or emulator.

- You will be presented with the single-activity app.
  - Click the floating red action button to select the image to send to the API.
    - This executes the `onClickListener` that's set in `onCreate`, which
      creates an `Intent` for selecting the content to send to the API.
  - Select an image from your device.
    - Control is handed back to the `MainActivity` and is handled by
      `onActivityResult`, which does a little bit of processing on the selected
      image and hands it to the `callCloudVision` method.
    - The `callCloudVision` method creates and executes a label detection
      request to the Vision API using the `google-api-services-vision` java
      client library. This library is declared as a dependency in
      `app/build.gradle`, and is used to simplify making API calls to the Google
      Cloud Vision API.
    - Notice that the `CLOUD_VISION_API_KEY` is set as part of this request, to
      authenticate the request and associate it with your project.
  - When the API responds, the `convertResponseToString` method extracts the
    labels from the response object and constructs a `String` to display.
  - The `onPostExecute` callback fires, with the constructed result, and
    populates the `image_details` `TextView` with the labels returned by the
    Vision API.

[vision-zip]: https://github.com/GoogleCloudPlatform/cloud-vision/archive/master.zip
[getting-started]: https://cloud.google.com/vision/docs/getting-started
[cloud-console]: https://console.cloud.google.com
[git]: https://git-scm.com/
[android-studio]: http://developer.android.com/sdk/
[billing]: https://console.cloud.google.com/billing?project=_
[enable-vision]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[api-key]: https://console.cloud.google.com/apis/credentials?project=_
