Java face detection Vision API example. See the
[tutorial](https://cloud.google.com/vision/docs/face-tutorial).

This sample uses the [Maven][maven] build system, and requires the
`google-api-services-vision` Maven package be installed locally.

    cd ../google-api-services-vision
    mvn install

To build and run the sample, run the following from this directory:

    mvn clean compile assembly:single
    java -cp target/face-detection-1.0-SNAPSHOT-jar-with-dependencies.jar \
        com.google.cloud.vision.samples.facedetect.FaceDetectApp \
        ../../data/face_detection/face.jpg \
        output.jpg

[maven]: https://maven.apache.org
