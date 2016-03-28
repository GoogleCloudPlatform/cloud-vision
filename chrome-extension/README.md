# Google Cloud Vision Chrome Extension example

This directory contains the code for the [Cloud Vision API]
(https://cloud.google.com/vision/) Chrome extension available [in the Chrome
webstore]
(https://chrome.google.com/webstore/detail/cloud-vision/nblmokgbialjjgfhfofbgfcghhbkejac).

This extension adds a right-click context menu to images in Chrome, with options
to run text detection, label detection or face detection on the image.

## Setup

To use this extension using your own API key, rename `config.json-EXAMPLE` to
`config.json` and specify your API key.

## Screenshots

Right-click an image and select "Cloud Vision" to show available options.
![Demonstration of the tooltip](images/screenshots/cv-tooltip-640x400.png)

Select "Text detection" to detect text in the image, and have detected text copied to the clipboard.
![Demonstration of text detection](images/screenshots/cv-text-640x400.png)

Select "Label detection" to detect labels for the image, displayed in a browser notification.
![Demonstration of label detection](images/screenshots/cv-label-640x400.png)

Select "Face detection" to open a new tab with detected faces highlighted.
![Demonstration of face detection](images/screenshots/cv-face-640x400.png)
