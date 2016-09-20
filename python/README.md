# Cloud Vision API Python samples

This directory contains [Cloud Vision API](https://cloud.google.com/vision/) Python samples and utilities.

## Prerequisites

You must have Python and `pip` installed.

Some of the samples require additional setup, which their READMEs will specify.

## Samples

### Face Detection

See the [face detection](https://cloud.google.com/vision/docs/face-tutorial) tutorial in the docs.

[Python Code](https://github.com/GoogleCloudPlatform/python-docs-samples/tree/master/vision/api/face_detection)

### Label Detection

See the [label detection](https://cloud.google.com/vision/docs/label-tutorial) tutorial in the docs.

[Python Code](https://github.com/GoogleCloudPlatform/python-docs-samples/tree/master/vision/api/label)

### Label Tagging Using Kubernetes

*Awwvision* is a [Kubernetes](https://github.com/kubernetes/kubernetes/) and
[Cloud Vision API](https://cloud.google.com/vision/) sample that uses the
Vision API to classify (label) images from Reddit's
[/r/aww](https://reddit.com/r/aww) subreddit, and display the labelled results
in a web application.

[Documentation and Python Code](awwvision)

### Text Detection Using the Vision API

This sample uses `TEXT_DETECTION` Vision API requests to build an inverted
index from the stemmed words found in the images, and stores that index in a
[Redis](redis.io) database. The example uses the
[nltk](http://www.nltk.org/index.html) (Natural Language Toolkit) library for
finding stopwords and doing stemming. The resulting index can be queried to find
images that match a given set of words, and to list text that was found in each
matching image.

[Documentation and Python Code](text)
