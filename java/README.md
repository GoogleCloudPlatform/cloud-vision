# Google Cloud Vision API Java examples

## Prerequisites

### Download Maven

This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Setup

* Create a project with the [Google Cloud Console][cloud-console], and enable
  the [Vision API][vision-api].
* Set up your environment with [Application Default Credentials][adc]. For
    example, from the Cloud Console, you might create a service account,
    download its json credentials file, then set the appropriate environment
    variable:

    ```bash
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
    ```

[cloud-console]: https://console.cloud.google.com
[vision-api]: https://console.cloud.google.com/apis/api/vision.googleapis.com/overview?project=_
[adc]: https://cloud.google.com/docs/authentication#developer_workflow

## Samples

### Label Detection

This sample annotates an image with labels based on its content.

- [Java Code](https://github.com/GoogleCloudPlatform/cloud-vision/tree/master/java/label/)

### Face Detection

This sample identifies faces within an image.

- [Quickstart Walkthrough](https://cloud.google.com/vision/docs/face-tutorial)
- [Java Code](https://github.com/GoogleCloudPlatform/cloud-vision/tree/master/java/face_detection/)

### Landmark Detection Using Google Cloud Storage

This sample identifies a landmark within an image stored on
Google Cloud Storage.

- [Documentation and Java Code](https://github.com/GoogleCloudPlatform/cloud-vision/tree/master/java/landmark_detection/)

