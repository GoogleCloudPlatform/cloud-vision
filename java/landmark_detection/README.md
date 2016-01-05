Java Vision API example for detecting landmarks within an image stored in Google Cloud Storage. See [sample](https://cloud.google.com/vision/docs/gcs-sample).

This sample requires the `google-api-services-vision` Maven package be installed
locally.

    cd ../google-api-services-vision
    mvn install

To build and run the sample, run the following from this directory:

    mvn clean compile assembly:single
    java -cp target/landmark-detection-1.0-SNAPSHOT-jar-with-dependencies.jar \
        com.google.cloud.vision.samples.landmarkdetection.DetectLandmark \
        gs://your-bucket/your-image.jpg

