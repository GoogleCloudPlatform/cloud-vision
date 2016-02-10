# Image detection with iOS device photos

This app demonstrates how to use the [Cloud Vision API](https://cloud.google.com/vision/) to run label and face detection on an image.

## Local Setup

In `ViewController.swift`, replace `YOUR_API_KEY` with the service account key you generated for your Cloud project.

To run this app locally clone the repo and run:

	pod install
	open imagepicker.xcworkspace

The app uses [SwiftyJSON](https://github.com/SwiftyJSON/SwiftyJSON) to parse the JSON returned from the HTTP request.



