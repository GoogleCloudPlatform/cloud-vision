
# Google Cloud Vision API examples

This repo contains some [Google Cloud Vision
API](https://cloud.google.com/vision/) examples.

[![Build
Status](https://travis-ci.org/GoogleCloudPlatform/cloud-vision.svg?branch=master)](https://travis-ci.org/GoogleCloudPlatform/cloud-vision)

The samples are organized by language and mobile platform.

## Language Examples

### Landmark Detection Using Google Cloud Storage

This sample identifies a landmark within an image stored on
Google Cloud Storage.

- [Documentation and Java Code](java/landmark_detection/)
- [Documentation and Python Code](python/landmark_detection/)

### Face Detection

See the [face detection](https://cloud.google.com/vision/docs/face-tutorial) tutorial in the docs.

- [Python Code](python/face_detection)
- [Java Code](java/face_detection/)
- [Node Code](node/face_detection)

### Label Detection

See the [label detection](https://cloud.google.com/vision/docs/label-tutorial) tutorial in the docs.

- [Python Code](python/label)
- [Java Code](java/label/)

### Label Tagging Using Kubernetes

*Awwvision* is a [Kubernetes](https://github.com/kubernetes/kubernetes/) and
[Cloud Vision API](https://cloud.google.com/vision/) sample that uses the
Vision API to classify (label) images from Reddit's
[/r/aww](https://reddit.com/r/aww) subreddit, and display the labelled results
in a web application.

- [Documentation and Python Code](python/awwvision)

### Text Detection Using the Vision API

This sample uses `TEXT_DETECTION` Vision API requests to build an inverted
index from the stemmed words found in the images, and stores that index in a
[Redis](redis.io) database. The resulting index can be queried to find
images that match a given set of words, and to list text that was found in each
matching image.

For finding stopwords and doing stemming, the Python example uses the
[nltk](http://www.nltk.org/index.html) (Natural Language Toolkit) library.
The Java example uses the [OpenNLP](https://opennlp.apache.org/) library.

- [Documentation and Python Code](python/text)
- [Documentation and Java Code](java/text)

## Mobile Platform Examples

### Image Detection Using Android Device Photos

This simple single-activity sample that shows you how to make a call to the
Cloud Vision API with an image picked from your device’s gallery.

- [Documentation and Android Code](android)

### Image Detection Using iOS Device Photos

The Swift and Objective-C versions of this app use the Vision API to run label
and face detection on an image from the device's photo library. The resulting
labels and face metadata from the API response are displayed in the UI.

Check out the Swift or Objective-C READMEs for specific getting started
instructions.

- [Documentation (Objective-C)](ios/Objective-C/README.md)

- [Documentation (Swift)](ios/Swift/README.md)

- [iOS Sample Code](ios)
