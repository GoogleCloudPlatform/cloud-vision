# Cloud Vision Swift Sample

This app demonstrates how to use the [Cloud Vision API](https://cloud.google.com/vision/) to run label and face detection on an image.

## Prerequisites
- An API key for the Cloud Vision API (See
  [the docs][getting-started] to learn more)
- An OSX machine or emulator
- [Xcode 7][xcode]
- Install [CocoaPods][cocoapods] if you don't have it already by running the command `sudo gem install cocoapods`.
	- You'll need to have [RubyGems][rubygems] installed in order to install CocoaPods.

## Quickstart
- Clone this repo and `cd` into the `Swift` directory.
- In `ImagePickerViewController.swift`, replace `YOUR_API_KEY` with the API key obtained above.
- `cd` into the `Swift` directory and install the `SwiftyJSON` pod by running `pod install`.
- Open the project by running `open imagepicker.xcworkspace`.
- Build and run the app.


## Running the app

- As with all Google Cloud APIs, every call to the Vision API must be associated
  with a project within the [Google Cloud Console][cloud-console] that has the
  Vision API enabled. This is described in more detail in the [getting started
  doc][getting-started], but in brief:
  - Create a project (or use an existing one) in the [Cloud
    Console][cloud-console]
  - [Enable billing][billing] and the [Vision API][enable-vision].
  - Create an [API key][api-key], and save this for later.

- Clone this `cloud-vision` repository on GitHub. If you have [`git`][git] installed, you can do this by executing the following command:

        $ git clone https://github.com/GoogleCloudPlatform/cloud-vision.git

    This will download the repository of samples into the directory
    `cloud-vision`.

    Otherwise, GitHub offers an [auto-generated zip file][vision-zip] of the `master` branch, which you can download and extract. Either method will include the desired directory at
    `cloud-vision/ios/Swift`.

- `cd` into the `cloud-vision/ios/Swift` directory you just cloned. The app uses the `SwiftyJSON` pod to parse the JSON response. This pod is defined in the `Podfile`, and you can install it by running `pod install`.

- Run the command `open imagepicker.xcworkspace` to open this project in Xcode. Be sure to open the `xcworkspace` version of the project.

- In Xcode's Project Navigator, open the `ImagePickerViewController.swift` file within the `imagepicker` directory.

- Find the line where the `API_KEY` is set. Replace the string value with the API key obtained from the Cloud console above. This key is the credential used in the `createRequest` method to authenticate all requests to the Vision API. Calls to the API are thus associated with the project you created above, for access and billing purposes.

- You are now ready to build and run the project. In Xcode you can do this by clicking the 'Play' button in the top left. This will launch the app on the simulator or on the device you've selected.

- Click the `Choose an image to analyze` button. This calls the `loadImageButtonTapped` action to load the device's photo library.

- Select an image from your device. If you're using the simulator, you can drag and drop an image from your computer into the simulator using Finder.
	- This executes the `imagePickerController`, which saves the selected image and calls the `base64EncodeImage` function. This function base64 encodes the image and resizes it if it's too large to send to the API.
	- The `createRequest` method creates and executes a label and face detection request to the Cloud Vision API.
	- When the API responds, the `analyzeResults` function is called. This method constructs a string of the labels returned from the API. If there are faces detected in the photo, it analyzes the emotions detected. It then displays the label and face results in the UI by populating the `labelResults` and `faceResults` `UITextView` with the data returned from the API.

[vision-zip]: https://github.com/GoogleCloudPlatform/cloud-vision/archive/master.zip
[getting-started]: https://cloud.google.com/vision/docs/getting-started
[cloud-console]: https://console.cloud.google.com
[git]: https://git-scm.com/
[xcode]: https://developer.apple.com/xcode/
[billing]: https://console.cloud.google.com/billing?project=_
[enable-vision]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[api-key]: https://console.cloud.google.com/apis/credentials?project=_
[cocoapods]: https://cocoapods.org/
[rubygems]: https://rubygems.org/pages/download